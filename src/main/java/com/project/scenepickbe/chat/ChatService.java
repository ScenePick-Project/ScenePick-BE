package com.project.scenepickbe.chat;

import java.util.stream.IntStream;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.infrastructure.s3.service.S3QueryService;

@Service
public class ChatService {

	private final ChatDao dao;
	private final S3QueryService images;
	private final SimpMessagingTemplate broker;
	private final TransactionTemplate transaction;
	private final Object[] roomLocks = IntStream.range(0, 64).mapToObj(i -> new Object()).toArray();

	public ChatService(ChatDao dao, S3QueryService images, SimpMessagingTemplate broker,
		PlatformTransactionManager transactionManager) {
		this.dao = dao;
		this.images = images;
		this.broker = broker;
		this.transaction = new TransactionTemplate(transactionManager);
		this.transaction.setTimeout(10);
	}

	public void send(long contentId, String userId, String body) {
		if (body == null || body.isBlank()) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
		String text = body.strip();
		if (text.codePointCount(0, text.length()) > 500 || text.indexOf('\0') >= 0) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
		// shortcut: 단일 서버에서 commit 순서와 방송 순서를 유지한다. 다중 서버는 broker 재설계가 필요하다.
		synchronized (roomLocks[Math.floorMod(Long.hashCode(contentId), roomLocks.length)]) {
			ChatResponse.Message saved = transaction.execute(status -> {
				if (userId == null || dao.lockActiveUser(userId) == null) {
					throw new GeneralException(ErrorStatus._FORBIDDEN);
				}
				if (dao.lockContent(contentId) == null) {
					throw new GeneralException(ErrorStatus.CONTENT_NOT_FOUND);
				}
				if (dao.sentRecently(userId)) {
					throw new GeneralException(ErrorStatus.CHAT_RATE_LIMITED);
				}
				var draft = new ChatInsert(contentId, userId, text);
				dao.insert(draft);
				return response(dao.find(draft.getMessageId()));
			});
			broker.convertAndSend("/topic/contents/" + contentId + "/chat", saved);
		}
	}

	public ChatResponse.Page list(long contentId, Long beforeId, Long afterId, int size) {
		if (size < 1 || size > 50 || (beforeId != null && beforeId <= 0)
			|| (afterId != null && afterId < 0) || (beforeId != null && afterId != null)) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
		requireContent(contentId);
		var rows = dao.list(contentId, beforeId, afterId, size + 1);
		boolean hasNext = rows.size() > size;
		var items = rows.stream().limit(size).map(this::response).toList();
		String cursor = items.isEmpty() ? null : items.getLast().messageId();
		return new ChatResponse.Page(items, cursor, hasNext);
	}

	public void requireContent(long contentId) {
		if (contentId <= 0 || !dao.contentExists(contentId)) {
			throw new GeneralException(ErrorStatus.CONTENT_NOT_FOUND);
		}
	}

	private ChatResponse.Message response(ChatMessage row) {
		String imageUrl = row.imageKey() == null ? null : images.getPresignedToDownload(row.imageKey()).url();
		return new ChatResponse.Message(Long.toString(row.messageId()), row.contentId(), row.userId(),
			row.nickname(), imageUrl, row.body(), row.createdAt());
	}
}
