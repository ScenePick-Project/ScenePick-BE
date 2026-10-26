package com.project.scenepickbe.chat;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

	public record Message(String messageId, long contentId, String userId, String nickname,
		String profileImageUrl, String body, LocalDateTime createdAt) {
	}

	public record Page(List<Message> items, String nextCursor, boolean hasNext) {
	}
}
