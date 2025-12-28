package com.project.scenepickbe.content.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

public class ContentResponseDTO {

	@Data
	@Builder
	public static class BasicDTO {
		private Long contentId;
		private String title;
		private String posterImageUrl;
		private String synopsis;
		private List<String> genres;
	}

	@Data
	@Builder
	public static class PersonDTO {
		private Long personId;
		private String name;
		private String charName;
		private String profileImageUrl;

	}

	@Data
	@Builder
	public static class PersonListDTO {
		private List<PersonDTO> persons;
	}

	@Data
	@Builder
	public static class EpisodeDTO {
		private Long episodeId;
		private Integer episodeNo;
		private String title;
		private String summary;

	}

	@Data
	@Builder
	public static class EpisodeListDTO {
		private List<EpisodeDTO> episodes;
	}
}
