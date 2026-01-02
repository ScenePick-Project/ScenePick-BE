package com.project.scenepickbe.content.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project.scenepickbe.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.ContentResponseDTO;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.EpisodeVo;
import com.project.scenepickbe.content.vo.PersonVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentQueryService {
	private final ContentDao contentDao;
	private final ModelMapper modelMapper;

	/**
	 *  작품 기본 정보 조회
	 * @param contentId 작품 고유 ID
	 * @return 기본 정보 DTO
	 */
	public ContentResponseDTO.BasicDTO getBasicInfo(Long contentId) {
		ContentVo contentVo = contentDao.selectContentBasic(contentId);

		if (contentVo == null) {
			throw new GeneralException(ErrorStatus.CONTENT_NOT_FOUND);
		}

		return modelMapper.map(contentVo, ContentResponseDTO.BasicDTO.class);
	}

	/**
	 *  작품 출연진 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 출연진 리스트 DTO
	 */
	public ContentResponseDTO.PersonListDTO getPersons(Long contentId) {
		List<PersonVo> personVoList = contentDao.selectContentPersons(contentId);

		List<ContentResponseDTO.PersonDTO> personDtos = personVoList.stream()
			.map(vo -> modelMapper.map(vo, ContentResponseDTO.PersonDTO.class))
			.collect(Collectors.toList());

		return ContentResponseDTO.PersonListDTO.builder()
			.persons(personDtos)
			.build();
	}

	/**
	 *  작품 에피소드 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 에피소드 리스트 DTO
	 */
	public ContentResponseDTO.EpisodeListDTO getEpisodes(Long contentId) {
		List<EpisodeVo> episodeVoList = contentDao.selectContentEpisodes(contentId);

		List<ContentResponseDTO.EpisodeDTO> episodeDtos = episodeVoList.stream()
			.map(vo -> modelMapper.map(vo, ContentResponseDTO.EpisodeDTO.class))
			.collect(Collectors.toList());

		return ContentResponseDTO.EpisodeListDTO.builder()
			.episodes(episodeDtos)
			.build();
	}
}
