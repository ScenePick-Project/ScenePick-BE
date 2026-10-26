package com.project.scenepickbe.chat;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatSocketController {

	private final ChatService service;

	public record Send(String body) {
	}

	@MessageMapping("/chat/{contentId}/messages")
	public void send(@DestinationVariable long contentId, Send request, Principal principal) {
		if (principal == null) {
			throw new GeneralException(ErrorStatus._UNAUTHORIZED);
		}
		service.send(contentId, principal.getName(), request.body());
	}

	@MessageExceptionHandler
	@SendToUser(destinations = "/queue/chat/errors", broadcast = false)
	public ApiResponse<?> error(Exception exception) {
		if (exception instanceof GeneralException general) {
			var reason = general.getErrorReasonHttpStatus();
			return ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null);
		}
		if (exception instanceof org.springframework.messaging.converter.MessageConversionException) {
			return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST.getCode(), "잘못된 메시지 형식입니다.", null);
		}
		log.error("채팅 메시지 처리 실패: {}", exception.getClass().getSimpleName());
		return ApiResponse.onFailure(ErrorStatus._INTERNAL_SERVER_ERROR.getCode(), "메시지를 처리하지 못했습니다.", null);
	}
}
