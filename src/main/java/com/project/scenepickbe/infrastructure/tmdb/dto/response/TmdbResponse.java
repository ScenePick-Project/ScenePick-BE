package com.project.scenepickbe.infrastructure.tmdb.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TmdbResponse", description = "TMDB API 응답 DTO")
public class TmdbResponse {

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "Genre", description = "TMDB 장르 정보")
	public record Genre(
		@Schema(description = "장르 ID")
		Integer id,

		@Schema(description = "장르 이름", example = "Action")
		String name
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "SeasonSummary", description = "TMDB 시즌 요약 정보")
	public record SeasonSummary(
		@JsonProperty("season_number")
		@Schema(description = "시즌 번호", example = "1")
		Integer seasonNumber,

		@Schema(description = "시즌 이름")
		String name
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "TvDetail", description = "TMDB TV 작품 상세 정보")
	public record TvDetail(

		@Schema(description = "TMDB 작품 ID")
		Long id,

		@Schema(description = "작품명")
		String name,

		@Schema(description = "작품 개요")
		String overview,

		@JsonProperty("poster_path")
		@Schema(description = "포스터 경로")
		String posterPath,

		@JsonProperty("genres")
		@Schema(description = "장르 목록")
		List<Genre> genreList,

		@JsonProperty("seasons")
		@Schema(description = "시즌 요약 목록")
		List<SeasonSummary> seasonSummaryList
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "MovieDetail", description = "TMDB 영화 상세 정보")
	public record MovieDetail(
		@Schema(description = "TMDB 작품 ID")
		Long id,

		@Schema(description = "영화 제목")
		String title,

		@Schema(description = "영화 개요")
		String overview,

		@JsonProperty("poster_path")
		@Schema(description = "포스터 경로")
		String posterPath,

		@JsonProperty("genres")
		@Schema(description = "장르 목록")
		List<Genre> genreList
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "Credit", description = "TMDB 개별 출연진 정보")
	public record Credit(
		@Schema(description = "활동명")
		String name,

		@Schema(description = "배역명")
		String character,

		@JsonProperty("profile_path")
		@Schema(description = "프로필 이미지 경로")
		String profilePath
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "CreditList", description = "TMDB 출연진 목록")
	public record CreditList(
		@JsonProperty("cast")
		@Schema(description = "출연진 목록")
		List<Credit> creditList
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "Episode", description = "TMDB 개별 에피소드 정보")
	public record Episode(
		@JsonProperty("episode_number")
		@Schema(description = "에피소드 번호", example = "1")
		Integer episodeNumber,

		@Schema(description = "에피소드 제목")
		String name,

		@Schema(description = "에피소드 개요")
		String overview,

		@JsonProperty("still_path")
		@Schema(description = "스틸컷 이미지 경로")
		String stillPath
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "SeasonDetail", description = "TMDB 시즌 상세 정보")
	public record SeasonDetail(
		@JsonProperty("season_number")
		@Schema(description = "시즌 번호", example = "1")
		Integer seasonNumber,

		@Schema(description = "시즌 이름")
		String name,

		@Schema(description = "시즌 개요")
		String overview,

		@JsonProperty("poster_path")
		@Schema(description = "시즌 포스터 경로")
		String posterPath,

		@JsonProperty("episodes")
		@Schema(description = "에피소드 목록")
		List<Episode> episodeList
	) {
	}

}
