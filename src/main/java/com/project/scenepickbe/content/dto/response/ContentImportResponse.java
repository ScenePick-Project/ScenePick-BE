package com.project.scenepickbe.content.dto.response;

import com.project.scenepickbe.content.enums.ContentType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContentImportResponse", description = "TMDB 연동 응답 DTO")
public class ContentImportResponse {

	@Schema(name = "Import", description = "TMDB 연동 결과")
	public record Import(
		@Schema(description = "작품 ID")
		Long contentId,

		@Schema(description = "작품 TMDB ID")
		Long tmdbId,

		@Schema(description = "작품 타입")
		ContentType contentType,

		@Schema(description = "가져오기 성공 여부")
		Boolean imported
	) {
	}

	@Schema(name = "Result", description = "작품 가져오기 응답")
	public record Result(
		@Schema(description = "TMDB 연동 결과")
		Import result,

		@Schema(description = "결과 메시지", example = "이미 등록된 작품입니다. force=true로 다시 가져올 수 있습니다.")
		String message
	) {
	}
}
