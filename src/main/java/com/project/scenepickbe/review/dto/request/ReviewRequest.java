package com.project.scenepickbe.review.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "ReviewRequest", description = "리뷰 관련 요청")
public class ReviewRequest {

	@Schema(description = "리뷰 작성 요청")
	public record Create(

		@Schema(description = "리뷰 본문")
		@NotBlank(message = "리뷰 내용을 입력해주세요.")
		String reviewBody,

		@Schema(description = "스포일러 여부")
		@NotNull
		Boolean isSpoiler,

		@Schema(description = "트랙 ID")
		String trackId,

		@Schema(description = "유튜브 ID")
		String youtubeId,

		@Schema(description = "유튜브 시작 시간")
		Integer startTime,

		@Schema(description = "유튜브 종료 시간")
		Integer endTime
	) {
	}

	@Schema(description = "리뷰 페이징 조회 요청")
	public record Slice(
		@Schema(description = "한 번에 가져올 리뷰 개수, 1이상 이어야 합니다.", defaultValue = "10")
		@Positive
		Integer size,

		@Schema(description = "이전 페이지의 마지막 리뷰 생성 시각", example = "2026-02-12T10:20:30")
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime cursorCreatedAt,

		@Schema(description = "이전 페이지의 마지막 리뷰 ID", example = "12")
		Long cursorReviewId
	) {
	}
}
