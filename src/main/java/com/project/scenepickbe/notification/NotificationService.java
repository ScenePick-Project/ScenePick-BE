package com.project.scenepickbe.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.paging.CursorPaging;
import com.project.scenepickbe.mypage.MyPageDao;
import com.project.scenepickbe.mypage.MyPageRequest;
import com.project.scenepickbe.mypage.MyPageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
	private final NotificationDao dao;
	private final MyPageDao users;

	private void owner(String userId) {
		if (userId == null) {
			throw new GeneralException(ErrorStatus._UNAUTHORIZED);
		}
		if (users.profile(userId) == null) {
			throw new GeneralException(ErrorStatus._FORBIDDEN);
		}
	}

	@Transactional
	public void added(String actorId, long reviewId, String type) {
		if (!"REVIEW_LIKE".equals(type) && !"REVIEW_BOOKMARK".equals(type)) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
		dao.insert(actorId, reviewId, type);
	}

	public MyPageResponse.Page<NotificationResponse.Item> list(String userId, MyPageRequest.Page request) {
		owner(userId);
		CursorPaging.validateCursorPair(request.cursorCreatedAt(), request.cursorId());
		if (request.cursorId() != null && request.cursorId() <= 0) {
			throw new GeneralException(ErrorStatus.CURSOR_INVALID);
		}
		int size = CursorPaging.normalizeSize(request.size(), 10, 30);
		var rows = dao.list(userId, request.cursorCreatedAt(), request.cursorId(), CursorPaging.resolveLimit(size));
		var slice = CursorPaging.toSlice(rows, size,
			row -> new MyPageResponse.Cursor(row.createdAt(), row.notificationId()));
		return new MyPageResponse.Page<>(slice.items(), slice.nextCursor(), slice.hasNext());
	}

	public NotificationResponse.Count unreadCount(String userId) {
		owner(userId);
		return new NotificationResponse.Count(dao.unreadCount(userId));
	}

	@Transactional
	public void read(String userId, long id) {
		owner(userId);
		if (id <= 0 || dao.read(userId, id) == 0) {
			throw new GeneralException(ErrorStatus._FORBIDDEN);
		}
	}
}
