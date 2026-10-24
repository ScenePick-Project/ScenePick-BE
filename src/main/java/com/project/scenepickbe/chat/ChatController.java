package com.project.scenepickbe.chat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contents/{contentId}/chat/messages")
@Tag(name = "Chat", description = "작품별 공개 채팅 내역. 송수신은 WebSocket/STOMP.")
public class ChatController {

	private final ChatService service;

	@GetMapping
	@Operation(summary = "작품 채팅 내역", description = "size 기본/최대 50. beforeId 이전 최신순, afterId 이후 오래된순. 두 커서는 동시 지정 불가.")
	public ApiResponse<ChatResponse.Page> list(@PathVariable long contentId,
		@RequestParam(required = false) Long beforeId, @RequestParam(required = false) Long afterId,
		@RequestParam(defaultValue = "50") int size) {
		return ApiResponse.onSuccess(service.list(contentId, beforeId, afterId, size));
	}
}
