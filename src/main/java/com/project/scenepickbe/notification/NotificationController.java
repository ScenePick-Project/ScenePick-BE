package com.project.scenepickbe.notification;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.mypage.MyPageRequest;
import com.project.scenepickbe.mypage.MyPageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me/notifications")
@Tag(name = "Notifications", description = "본인 알림. 401 인증 없음, 403 비활성 계정/타인 알림, 400 커서 오류. 삭제 리뷰의 reviewId/contentId는 null.")
public class NotificationController {
	private final NotificationService service;

	private String id(User user) {
		if (user == null) {
			throw new GeneralException(ErrorStatus._UNAUTHORIZED);
		}
		return user.getUsername();
	}

	@GetMapping
	@Operation(summary = "본인 알림 최신순 목록", description = "size 기본10 최대30. cursorCreatedAt/cursorId 쌍. 취소해도 알림 유지.")
	public ApiResponse<MyPageResponse.Page<NotificationResponse.Item>> list(
		@AuthenticationPrincipal(errorOnInvalidType = false) User user, @ParameterObject MyPageRequest.Page request) {
		return ApiResponse.onSuccess(service.list(id(user), request));
	}

	@GetMapping("/unread-count")
	@Operation(summary = "본인 미읽음 알림 개수")
	public ApiResponse<NotificationResponse.Count> unreadCount(
		@AuthenticationPrincipal(errorOnInvalidType = false) User user) {
		return ApiResponse.onSuccess(service.unreadCount(id(user)));
	}

	@PatchMapping("/{notificationId}/read")
	@Operation(summary = "본인 알림 읽음 처리", description = "멱등 처리. 타인 ID와 없는 ID 모두403.")
	public ApiResponse<?> read(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@PathVariable long notificationId) {
		service.read(id(user), notificationId);
		return ApiResponse.onSuccess(null);
	}
}
