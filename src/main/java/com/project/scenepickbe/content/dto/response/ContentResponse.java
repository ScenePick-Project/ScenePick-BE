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

		@Schema(description = "줄거리 요약")
		String synopsis,

		@Schema(description = "장르 목록", example = "[\"ACTION\", \"ROMANCE\"]")
		List<String> genreList
	) {
	}

	@Schema(name = "Person", description = "출연진 상세 정보")
	public record Person(
		@Schema(description = "출연진 ID")
		Long personId,

		@Schema(description = "활동명")
		String name,

		@Schema(description = "배역명")
		String charName,

		@Schema(description = "프로필 이미지 URL")
		String profileImageUrl
	) {
	}

	@Schema(name = "PersonList", description = "출연진 목록")
	public record PersonList(
		@Schema(description = "출연진 리스트")
		List<Person> personList
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
