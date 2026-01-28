package com.project.scenepickbe.content.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.project.scenepickbe.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentResponse;
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
	public ContentResponse.Basic getBasicInfo(Long contentId) {
		ContentVo contentVo = contentDao.selectContentBasic(contentId);

		if (contentVo == null) {
			throw new GeneralException(ErrorStatus.CONTENT_NOT_FOUND);
		}

		List<String> genreList = contentVo.getGenreList() == null
			? List.of()
			: contentVo.getGenreList().stream()
			.map(Enum::name)
			.collect(Collectors.toList());

		ContentResponse.Basic mapped = modelMapper.map(contentVo, ContentResponse.Basic.class);

		return new ContentResponse.Basic(
			mapped.contentId(),
			mapped.title(),
			mapped.posterImageUrl(),
			mapped.synopsis(),
			genreList
		);
	}

	/**
	 * 작품 출연진 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 출연진 리스트 DTO
	 */
	public ContentResponse.PersonList getPersons(Long contentId) {
		List<PersonVo> personVoList = contentDao.selectContentPersons(contentId);

		List<ContentResponse.Person> personDtoList = personVoList.stream()
			.map(vo -> modelMapper.map(vo, ContentResponse.Person.class))
			.collect(Collectors.toList());

		return new ContentResponse.PersonList(personDtoList);
	}

	/**
	 * 작품 에피소드 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 에피소드 리스트 DTO
	 */
	public ContentResponse.EpisodeList getEpisodes(Long contentId) {
		List<EpisodeVo> episodeVoList = contentDao.selectContentEpisodes(contentId);

		List<ContentResponse.Episode> episodeDtoList = episodeVoList.stream()
			.map(vo -> modelMapper.map(vo, ContentResponse.Episode.class))
			.collect(Collectors.toList());

		return new ContentResponse.EpisodeList(episodeDtoList);
	}
}
