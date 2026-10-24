package com.project.scenepickbe.chat;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatDao {

	boolean contentExists(long contentId);

	Long lockContent(long contentId);

	String lockActiveUser(String userId);

	boolean sentRecently(String userId);

	int insert(ChatInsert message);

	ChatMessage find(long messageId);

	List<ChatMessage> list(@Param("contentId") long contentId, @Param("beforeId") Long beforeId,
		@Param("afterId") Long afterId, @Param("limit") int limit);
}
