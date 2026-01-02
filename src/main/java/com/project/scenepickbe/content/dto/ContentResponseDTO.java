package com.project.scenepickbe.content.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "ContentResponseDTO", description = "작품 응답 정보 DTO")
public class ContentResponseDTO {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(name = "BasicDTO", description = "작품 기본 정보 응답 DTO")
	public static class BasicDTO {

		@Schema(description = "작품 ID")
		private Long contentId;

		@Schema(description = "작품 제목")
		private String title;

		@Schema(description = "포스터 이미지 URL")
		private String posterImageUrl;

		@Schema(description = "줄거리 요약")
		private String synopsis;

		@Schema(description = "장르 목록", example = "[\"ACTION\", \"ROMANCE\"]")
		private List<String> genres;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(name = "PersonDTO", description = "출연진 상세 정보 DTO")
	public static class PersonDTO {

		@Schema(description = "출연진 ID")
		private Long personId;

		@Schema(description = "활동명")
		private String name;

		@Schema(description = "배역명")
		private String charName;

		@Schema(description = "프로필 이미지 URL")
		private String profileImageUrl;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(name = "PersonListDTO", description = "출연진 목록 응답 DTO")
	public static class PersonListDTO {

		@Schema(description = "출연진 리스트")
		private List<PersonDTO> persons;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(name = "EpisodeDTO", description = "에피소드 상세 정보 DTO")
	public static class EpisodeDTO {

		@Schema(description = "에피소드 ID")
		private Long episodeId;

		@Schema(description = "회차")
		private Integer episodeNo;

		@Schema(description = "회차 제목")
		private String title;

		@Schema(description = "회차 요약")
		private String summary;

	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(name = "EpisodeListDTO", description = "에피소드 목록 응답 DTO")
	public static class EpisodeListDTO {

		@Schema(description = "에피소드 리스트")
		private List<EpisodeDTO> episodes;
	}
}
