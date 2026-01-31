package com.project.scenepickbe.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
}
