package com.project.scenepickbe.mypage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyPageRow {
	private Long id;
	private LocalDateTime createdAt;
	private String userId;
	private String nickname;
	private String bio;
	private String imageKey;
	private long followingCount;
	private long followerCount;
	private Long reviewId;
	private Long contentId;
	private String title;
	private String reviewBody;
	private BigDecimal rating;
	private String trackId;
	private String trackName;
	private String artist;
	private long count;
}
