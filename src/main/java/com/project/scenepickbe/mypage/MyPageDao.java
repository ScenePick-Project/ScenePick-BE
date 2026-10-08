package com.project.scenepickbe.mypage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MyPageDao {
	MyPageRow profile(@Param("userId") String userId);
	int updateProfile(@Param("userId") String userId, @Param("request") MyPageRequest.ProfileUpdate request);
	List<MyPageRow> list(@Param("userId") String userId, @Param("kind") String kind,
		@Param("createdAt") LocalDateTime createdAt, @Param("id") Long id, @Param("limit") int limit);
	List<MyPageRow> ratings(@Param("userId") String userId);
	int rate(@Param("userId") String userId, @Param("reviewId") long reviewId, @Param("rating") BigDecimal rating);
	int follow(@Param("userId") String userId, @Param("targetId") String targetId);
	int unfollow(@Param("userId") String userId, @Param("targetId") String targetId);
	int bookmarkReview(@Param("userId") String userId, @Param("reviewId") long reviewId);
	int unbookmarkReview(@Param("userId") String userId, @Param("reviewId") long reviewId);
	int bookmarkTrack(@Param("userId") String userId, @Param("track") MyPageRequest.Track track);
	int unbookmarkTrack(@Param("userId") String userId, @Param("trackId") String trackId);
}
