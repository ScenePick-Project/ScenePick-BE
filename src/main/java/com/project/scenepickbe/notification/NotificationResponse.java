package com.project.scenepickbe.notification;

import java.time.LocalDateTime;

public class NotificationResponse {
	public record Item(long notificationId, String type, Long reviewId, Long contentId,
		String actorNickname, LocalDateTime createdAt, LocalDateTime readAt) {
	}
	public record Count(long count) {
	}
}
