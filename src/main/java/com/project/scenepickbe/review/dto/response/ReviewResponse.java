package com.project.scenepickbe.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ReviewResponse", description = "리뷰 응답 DTO")
public class ReviewResponse {

	@Schema(name = "Created", description = "리뷰 생성 응답")
	public record Created(
		@Schema(description = "리뷰 ID")
		Long reviewId) {
	}

	@Schema(name = "Review", description = "리뷰 조회 응답")
	public record Review(
		@Schema(description = "리뷰 ID")
		Long reviewId,

		@Schema(description = "리뷰 작품")
		Long contentId,

		@Schema(description = "리뷰 작성자")
		String userId,

		@Schema(description = "리뷰 본문")
		String reviewBody,

		@Schema(description = "첨부 영상 시작 시간")
		Integer startTime,

		@Schema(description = "첨부 영상 종료 시간")
		Integer endTime,

		@Schema(description = "첨부 영상 유튜브 ID")
		String youtubeId,

		@Schema(description = "리뷰 스포일러 여부")
		Boolean isSpoiler,

		@Schema(description = "리뷰 트랙 ID")
		String trackId
	) {
	}

	@Schema(name = "ReviewList", description = "리뷰 목록")
	public record ReviewList(
		@Schema(description = "리뷰 목록")
		List<Review> reviewList
	) {
	}

	@Schema(name = "ReviewCursor", description = "리뷰 커서")
	public record Cursor(
		@Schema(description = "마지막 리뷰 생성 시각")
		LocalDateTime createdAt,
		@Schema(description = "마지막 리뷰 ID")
		Long reviewId
	) {
	}

	@Schema(name = "SliceList", description = "리뷰 슬라이스 목록")
	public record SliceList(
		@Schema(description = "리뷰 목록")
		List<Review> reviewList,
		@Schema(description = "다음 페이지 커서")
		Cursor nextCursor,
		@Schema(description = "다음 페이지 존재 여부")
		Boolean hasNext
	) {
	}
}
