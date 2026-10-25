package com.project.scenepickbe.chat;

import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;

@Component
public class ChatStompInterceptor implements ExecutorChannelInterceptor {

	private static final Pattern TOPIC = Pattern.compile("/topic/contents/([1-9][0-9]{0,18})/chat");
	private static final Pattern SEND = Pattern.compile("/app/chat/([1-9][0-9]{0,18})/messages");
	private final JwtTokenProvider tokens;
	private final ObjectProvider<ChatService> service;
	private final ObjectProvider<MessageChannel> outbound;

	public ChatStompInterceptor(JwtTokenProvider tokens, ObjectProvider<ChatService> service,
		@Qualifier("clientOutboundChannel") ObjectProvider<MessageChannel> outbound) {
		this.tokens = tokens;
		this.service = service;
		this.outbound = outbound;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		var headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (headers == null || headers.getCommand() == null) {
			return message;
		}
		try {
			validate(headers);
		} catch (RuntimeException exception) {
			String code = exception instanceof GeneralException general
				? general.getErrorReasonHttpStatus().getCode() : "COMMON500";
			var error = StompHeaderAccessor.create(StompCommand.ERROR);
			error.setSessionId(headers.getSessionId());
			error.setMessage(code);
			error.setContentType(MimeTypeUtils.TEXT_PLAIN);
			// 순서 보존 채널은 예외를 흡수하므로 해당 연결에 ERROR를 직접 보내고 처리를 중단한다.
			outbound.getObject().send(MessageBuilder.createMessage(code.getBytes(StandardCharsets.UTF_8),
				error.getMessageHeaders()));
			return null;
		}
		return message;
	}

	private void validate(StompHeaderAccessor headers) {
		switch (headers.getCommand()) {
			case CONNECT, STOMP -> {
				String authorization = headers.getFirstNativeHeader("Authorization");
				if (authorization != null) {
					if (!authorization.startsWith("Bearer ")) {
						throw new GeneralException(ErrorStatus.JWT_INVALID);
					}
					headers.setUser(tokens.getAuthentication(authorization.substring(7)));
				}
			}
			case SUBSCRIBE -> {
				String destination = headers.getDestination();
				if (!"/user/queue/chat/errors".equals(destination)) {
					service.getObject().requireContent(contentId(TOPIC, destination));
				}
			}
			case SEND -> {
				contentId(SEND, headers.getDestination());
				if (!(headers.getUser() instanceof Authentication authentication)
					|| !(authentication.getCredentials() instanceof String token)) {
					throw new GeneralException(ErrorStatus._UNAUTHORIZED);
				}
				// 연결 후 만료된 토큰으로도 전송할 수 없도록 매번 검증한다.
				tokens.getAuthentication(token);
			}
			case UNSUBSCRIBE, DISCONNECT -> { }
			default -> throw new GeneralException(ErrorStatus._FORBIDDEN);
		}
	}

	private long contentId(Pattern pattern, String destination) {
		var match = pattern.matcher(destination == null ? "" : destination);
		if (!match.matches()) {
			throw new GeneralException(ErrorStatus._FORBIDDEN);
		}
		try {
			return Long.parseLong(match.group(1));
		} catch (NumberFormatException exception) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
	}

	@Override
	public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler,
		Exception exception) {
		var headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (exception != null || !(handler instanceof SimpleBrokerMessageHandler) || headers == null
			|| headers.getCommand() != StompCommand.SUBSCRIBE || headers.getReceipt() == null
			|| !TOPIC.matcher(headers.getDestination()).matches()) {
			return;
		}
		// Simple broker는 receipt를 지원하지 않는다. 공개 topic 등록 완료만 확인해 복구 조회의 경계를 만든다.
		var receipt = StompHeaderAccessor.create(StompCommand.RECEIPT);
		receipt.setSessionId(headers.getSessionId());
		receipt.setReceiptId(headers.getReceipt());
		outbound.getObject().send(MessageBuilder.createMessage(new byte[0], receipt.getMessageHeaders()));
	}
}
