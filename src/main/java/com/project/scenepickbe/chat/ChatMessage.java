package com.project.scenepickbe.chat;

import java.time.LocalDateTime;

public record ChatMessage(long messageId, long contentId, String userId, String nickname,
	String imageKey, String body, LocalDateTime createdAt) {
}
