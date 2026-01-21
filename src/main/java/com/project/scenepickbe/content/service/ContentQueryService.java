package com.project.scenepickbe.content.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project.scenepickbe.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.ContentResponseDto;
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
	 * 작품 기본 정보 조회
	 * @param contentId 작품 고유 ID
	 * @return 기본 정보 DTO
	 */
	public ContentResponseDto.BasicDTO getBasicInfo(Long contentId) {
		ContentVo contentVo = contentDao.selectContentBasic(contentId);

		if (contentVo == null) {
			throw new GeneralException(ErrorStatus.CONTENT_NOT_FOUND);
		}

		return modelMapper.map(contentVo, ContentResponseDto.BasicDTO.class);
	}

	/**
	 * 작품 출연진 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 출연진 리스트 DTO
	 */
	public ContentResponseDto.PersonListDTO getPersons(Long contentId) {
		List<PersonVo> personVoList = contentDao.selectContentPersons(contentId);

		List<ContentResponseDto.PersonDTO> personDtos = personVoList.stream()
			.map(vo -> modelMapper.map(vo, ContentResponseDto.PersonDTO.class))
			.collect(Collectors.toList());

		return ContentResponseDto.PersonListDTO.builder()
			.persons(personDtos)
			.build();
	}

	/**
	 * 작품 에피소드 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 에피소드 리스트 DTO
	 */
	public ContentResponseDto.EpisodeListDTO getEpisodes(Long contentId) {
		List<EpisodeVo> episodeVoList = contentDao.selectContentEpisodes(contentId);

		List<ContentResponseDto.EpisodeDTO> episodeDtos = episodeVoList.stream()
			.map(vo -> modelMapper.map(vo, ContentResponseDto.EpisodeDTO.class))
			.collect(Collectors.toList());

		return ContentResponseDto.EpisodeListDTO.builder()
			.episodes(episodeDtos)
			.build();
	}
}
