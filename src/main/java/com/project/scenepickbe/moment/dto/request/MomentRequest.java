package com.project.scenepickbe.moment.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class MomentRequest {
	@Schema(name = "MomentCreate", description = "개인 모먼트 저장 요청")
	public record Create(
		@Schema(description = "유튜브 영상 ID", example = "abcdefghijk")
		@NotBlank
		@Pattern(regexp = "[A-Za-z0-9_-]{11}")
		String youtubeId,
		@Schema(description = "시작 시점 (정수 초)", example = "12")
		@NotNull
		@JsonDeserialize(using = MomentTimeDeserializer.class)
		Integer startTime,
		@Schema(description = "종료 시점 (정수 초)", example = "18")
		@NotNull
		@JsonDeserialize(using = MomentTimeDeserializer.class)
		Integer endTime,
		@Schema(description = "선택 입력 개인 메모")
		String memo
	) {
	}

	@Schema(name = "MomentSliceRequest", description = "내 모먼트 최신순 커서 조회")
	public record Slice(
		@Schema(description = "선택 작품 필터") @Positive Long contentId,
		@Schema(description = "선택 유튜브 영상 필터")
		@Pattern(regexp = "[A-Za-z0-9_-]{11}")
		String youtubeId,
		@Schema(description = "페이지 크기. 최대 30으로 보정", defaultValue = "10")
		@Positive
		Integer size,
		@Schema(description = "직전 페이지 커서의 저장 시각. cursorMomentId와 함께 전달")
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime cursorCreatedAt,
		@Schema(description = "직전 페이지 커서의 모먼트 ID. cursorCreatedAt과 함께 전달")
		@Positive
		Long cursorMomentId
	) {
	}

	@Schema(name = "MomentUpdate", description = "생략한 필드는 유지합니다. memo는 null 또는 빈 문자열로 지웁니다.")
	@lombok.Getter
	public static class Update {
		@Schema(description = "변경할 시작 시점 (정수 초). 명시적 null은 허용하지 않습니다.")
		@JsonSetter(nulls = Nulls.FAIL)
		@JsonDeserialize(using = MomentTimeDeserializer.class)
		private Integer startTime;

		@Schema(description = "변경할 종료 시점 (정수 초). 명시적 null은 허용하지 않습니다.")
		@JsonSetter(nulls = Nulls.FAIL)
		@JsonDeserialize(using = MomentTimeDeserializer.class)
		private Integer endTime;

		@Schema(description = "변경할 개인 메모. null 또는 빈 문자열이면 삭제")
		private String memo;

		@JsonIgnore
		@Schema(hidden = true)
		private boolean memoProvided;

		public void setStartTime(Integer startTime) {
			this.startTime = startTime;
		}

		public void setEndTime(Integer endTime) {
			this.endTime = endTime;
		}

		public void setMemo(String memo) {
			this.memo = memo;
			this.memoProvided = true;
		}
	}
}
