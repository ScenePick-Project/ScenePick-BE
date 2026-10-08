package com.project.scenepickbe.mypage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MyPageResponse {
	public record Profile(String userId, String nickname, String bio, String imageKey, String imageUrl,
		long followingCount, long followerCount) { }
	public record Cursor(LocalDateTime createdAt, Long id) { }
	public record Page<T>(List<T> items, Cursor nextCursor, boolean hasNext) { }
	public record UserItem(String userId, String nickname) { }
	public record ReviewItem(Long reviewId, Long contentId, String title, String reviewBody,
		BigDecimal rating, LocalDateTime createdAt) { }
	public record TrackItem(String trackId, String trackName, String artist) { }
	public record RatingBucket(BigDecimal rating, long count) { }
	public record Ratings(long totalCount, List<RatingBucket> buckets) { }
}
