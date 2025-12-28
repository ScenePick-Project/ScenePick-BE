package com.project.scenepickbe.content.converter;

import java.util.List;

import com.project.scenepickbe.content.dto.ContentResponseDTO;
import com.project.scenepickbe.content.enums.GenreType;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.EpisodeVo;
import com.project.scenepickbe.content.vo.PersonVo;

public class ContentConverter {

	public static ContentResponseDTO.BasicDTO toBasicDTO(ContentVo contentVo) {
		return ContentResponseDTO.BasicDTO.builder()
			.contentId(contentVo.getId())
			.title(contentVo.getTitle())
			.posterImageUrl(contentVo.getPosterImageUrl())
			.synopsis(contentVo.getSynopsis())
			.genres(contentVo.getGenres().stream()
				.map(GenreType::getValue)
				.toList())
			.build();
	}

	public static ContentResponseDTO.PersonDTO toPersonDTO(PersonVo personVo) {
		return ContentResponseDTO.PersonDTO.builder()
			.personId(personVo.getId())
			.name(personVo.getName())
			.charName(personVo.getCharName())
			.profileImageUrl(personVo.getProfileImageUrl())
			.build();
	}

	public static ContentResponseDTO.PersonListDTO toPersonListDTO(List<PersonVo> personVoList) {
		List<ContentResponseDTO.PersonDTO> list = personVoList.stream()
			.map(ContentConverter::toPersonDTO)
			.toList();

		return ContentResponseDTO.PersonListDTO.builder()
			.persons(list)
			.build();
	}

	public static ContentResponseDTO.EpisodeDTO toEpisodeDTO(EpisodeVo episodeVo) {
		return ContentResponseDTO.EpisodeDTO.builder()
			.episodeId(episodeVo.getId())
			.episodeNo(episodeVo.getEpisodeNo())
			.title(episodeVo.getTitle())
			.summary(episodeVo.getSummary())
			.build();
	}

	public static ContentResponseDTO.EpisodeListDTO toEpisodeListDTO(List<EpisodeVo> episodeVoList) {
		List<ContentResponseDTO.EpisodeDTO> list = episodeVoList.stream()
			.map(ContentConverter::toEpisodeDTO)
			.toList();

		return ContentResponseDTO.EpisodeListDTO.builder()
			.episodes(list)
			.build();
	}

}
