package com.project.scenepickbe.content.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContentResponse", description = "작품 응답 DTO")
public class ContentResponse {

	@Schema(name = "Basic", description = "작품 기본 정보")
	public record Basic(
		@Schema(description = "작품 ID")
		Long contentId,

		@Schema(description = "작품 제목")
		String title,

		@Schema(description = "포스터 이미지 URL")
		String posterImageUrl,

		@Schema(description = "줄거리 요약 (TV는 null)")
		String synopsis,

		@Schema(description = "장르 목록", example = "[\"ACTION\", \"ROMANCE\"]")
		List<String> genreList,

		@Schema(description = "기본 시즌 번호 (TV는 1, 영화는 null)")
		Integer defaultSeasonNo,

		@Schema(description = "시즌 리스트 (TV만 제공)")
		List<Season> seasonList
	) {
	}

	@Schema(name = "Credit", description = "출연진 상세 정보")
	public record Credit(
		@Schema(description = "출연진 ID")
		Long creditId,

		@Schema(description = "활동명")
		String name,

		@Schema(description = "배역명")
		String charName,

		@Schema(description = "프로필 이미지 URL")
		String profileImageUrl
	) {
	}

	@Schema(name = "CreditList", description = "출연진 목록")
	public record CreditList(
		@Schema(description = "출연진 리스트")
		List<Credit> creditList
	) {
	}

	@Schema(name = "Season", description = "시즌 정보")
	public record Season(
		@Schema(description = "시즌 ID")
		Long seasonId,

		@Schema(description = "시즌 번호")
		Integer seasonNo,

		@Schema(description = "시즌 이름")
		String name,

		@Schema(description = "시즌 개요")
		String overview,

		@Schema(description = "시즌 포스터 이미지 URL")
		String posterImageUrl
	) {
	}

	@Schema(name = "SeasonList", description = "시즌 목록")
	public record SeasonList(
		@Schema(description = "시즌 리스트")
		List<Season> seasonList
	) {
	}

	@Schema(name = "Episode", description = "에피소드 상세 정보")
	public record Episode(
		@Schema(description = "에피소드 ID")
		Long episodeId,

		@Schema(description = "회차")
		Integer episodeNo,

		@Schema(description = "회차 제목")
		String title,

		@Schema(description = "회차 요약")
		String summary,

		@Schema(description = "회차 썸네일 이미지 URL")
		String stillImageUrl
	) {
	}

	@Schema(name = "EpisodeList", description = "에피소드 목록")
	public record EpisodeList(
		@Schema(description = "에피소드 리스트")
		List<Episode> episodeList
	) {
	}
}
