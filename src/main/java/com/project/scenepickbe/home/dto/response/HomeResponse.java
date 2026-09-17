package com.project.scenepickbe.home.dto.response;

import java.util.List;

import com.project.scenepickbe.content.enums.ContentType;

import io.swagger.v3.oas.annotations.media.Schema;

public class HomeResponse {

	@Schema(name = "HomeRecommendations", description = "홈 랜덤 추천 목록")
	public record Recommendations(
		@Schema(description = "추천 방식", allowableValues = {"RANDOM"})
		String recommendationType,
		@Schema(description = "중복 없는 작품 최대 10개. 후보가 없으면 빈 배열")
		List<Content> contentList
	) {
	}

	@Schema(name = "HomeContent", description = "홈 작품 요약")
	public record Content(
		@Schema(description = "작품 ID")
		Long contentId,
		@Schema(description = "작품 제목")
		String title,
		@Schema(description = "포스터 이미지 URL")
		String posterImageUrl,
		@Schema(description = "작품 유형")
		ContentType contentType
	) {
	}
}
