package com.project.scenepickbe.moment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.project.scenepickbe.moment.vo.MomentVo;

import io.swagger.v3.oas.annotations.media.Schema;

public class MomentResponse {
	@Schema(name = "MomentCreated", description = "모먼트 생성 결과")
	public record Created(
		@Schema(description = "생성된 모먼트 ID")
		Long momentId
	) {
	}

	@Schema(name = "MomentDetail", description = "본인 모먼트 상세")
	public record Detail(
		@Schema(description = "모먼트 ID")
		Long momentId,
		@Schema(description = "작품 ID")
		Long contentId,
		@Schema(description = "유튜브 영상 ID")
		String youtubeId,
		@Schema(description = "시작 시점 (정수 초)")
		Integer startTime,
		@Schema(description = "종료 시점 (정수 초)")
		Integer endTime,
		@Schema(description = "개인 메모. 없으면 null")
		String memo,
		@Schema(description = "저장 시각")
		LocalDateTime createdAt,
		@Schema(description = "수정 시각")
		LocalDateTime updatedAt
	) {
		public static Detail from(MomentVo moment) {
			return new Detail(moment.getMomentId(), moment.getContentId(), moment.getYoutubeId(),
				moment.getStartTime(), moment.getEndTime(), moment.getMemo(), moment.getCreatedAt(),
				moment.getUpdatedAt());
		}
	}

	@Schema(name = "MomentCursor", description = "다음 모먼트 페이지 커서")
	public record Cursor(
		@Schema(description = "마지막 모먼트의 저장 시각")
		LocalDateTime createdAt,
		@Schema(description = "마지막 모먼트 ID")
		Long momentId
	) {
	}

	@Schema(name = "MomentSlice", description = "내 모먼트 목록과 다음 페이지 정보")
	public record Slice(
		@Schema(description = "내 모먼트 목록")
		List<Detail> momentList,
		@Schema(description = "다음 페이지 커서. 마지막 페이지면 null")
		Cursor nextCursor,
		@Schema(description = "다음 페이지 존재 여부")
		boolean hasNext
	) {
	}
}
