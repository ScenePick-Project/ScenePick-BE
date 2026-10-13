package com.project.scenepickbe.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NotificationDao {
	int insert(@Param("actorId") String actorId, @Param("reviewId") long reviewId, @Param("type") String type);
	List<NotificationResponse.Item> list(@Param("userId") String userId,
		@Param("createdAt") LocalDateTime createdAt, @Param("id") Long id, @Param("limit") int limit);
	long unreadCount(@Param("userId") String userId);
	int read(@Param("userId") String userId, @Param("id") long id);
}
