package com.project.scenepickbe.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReviewLikeResponse", description = "리뷰 좋아요 응답 DTO")
public class ReviewLikeResponse {

	@Schema(name = "ReviewLikeToggle", description = "리뷰 좋아요 토글 응답")
	public record Toggle(
		@Schema(description = "리뷰 ID")
		Long reviewId,

		@Schema(description = "좋아요 여부")
		Boolean isLiked,

		@Schema(description = "좋아요 수")
		Integer likeCount
	) {
	}
}
