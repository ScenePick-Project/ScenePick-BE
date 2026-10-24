package com.project.scenepickbe.chat;

import lombok.Data;

@Data
public class ChatInsert {

	private Long messageId;
	private final long contentId;
	private final String userId;
	private final String body;
}
