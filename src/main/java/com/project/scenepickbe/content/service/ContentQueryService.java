package com.project.scenepickbe.content.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.content.converter.ContentConverter;
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

	/**
	 *  작품 기본 정보 조회
	 * @param contentId 작품 고유 ID
	 * @return 기본 정보 DTO
	 */
	public ContentResponseDTO.BasicDTO getBasicInfo(Long contentId) {
		ContentVo contentVo = contentDao.selectContentBasic(contentId);
		return ContentConverter.toBasicDTO(contentVo);
	}

	/**
	 *  작품 출연진 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 출연진 리스트 DTO
	 */
	public ContentResponseDTO.PersonListDTO getPersons(Long contentId) {
		List<PersonVo> personVoList = contentDao.selectContentPersons(contentId);
		return ContentConverter.toPersonListDTO(personVoList);
	}

	/**
	 *  작품 에피소드 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 에피소드 리스트 DTO
	 */
	public ContentResponseDTO.EpisodeListDTO getEpisodes(Long contentId) {
		List<EpisodeVo> episodeVoList = contentDao.selectContentEpisodes(contentId);
		return ContentConverter.toEpisodeListDTO(episodeVoList);
	}
}
