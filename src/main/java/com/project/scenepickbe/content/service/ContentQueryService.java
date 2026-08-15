package com.project.scenepickbe.content.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.mapper.ContentDtoMapper;
import com.project.scenepickbe.content.vo.ContentVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentQueryService {
	private final ContentDao contentDao;
	private final ContentDtoMapper contentDtoMapper;

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

		Integer defaultSeasonNo = contentVo.getContentType() == ContentType.TV ? 1 : null;
		List<ContentResponse.Season> seasonList = contentVo.getContentType() == ContentType.TV
			? contentDtoMapper.toSeasons(contentDao.selectContentSeasons(contentId))
			: List.of();

		return contentDtoMapper.toBasic(
			contentVo,
			genreList,
			defaultSeasonNo,
			seasonList
		);
	}

	/**
	 * 작품 출연진 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 출연진 리스트 DTO
	 */
	public ContentResponse.CreditList getCredits(Long contentId) {
		return contentDtoMapper.toCreditList(contentDao.selectContentCredits(contentId));
	}

	/**
	 * 작품 시즌 에피소드 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @param seasonNo 시즌 번호
	 * @return 에피소드 리스트 DTO
	 */
	public ContentResponse.EpisodeList getEpisodes(Long contentId, Integer seasonNo) {
		return contentDtoMapper.toEpisodeList(contentDao.selectSeasonEpisodes(contentId, seasonNo));
	}

	/**
	 * 작품 시즌 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 시즌 리스트 DTO
	 */
	public ContentResponse.SeasonList getSeasons(Long contentId) {
		return contentDtoMapper.toSeasonList(contentDao.selectContentSeasons(contentId));
	}

	/**
	 * 작품 시즌 출연진 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @param seasonNo 시즌 번호
	 * @return 시즌 출연진 리스트 DTO
	 */
	public ContentResponse.CreditList getSeasonCredits(Long contentId, Integer seasonNo) {
		return contentDtoMapper.toCreditList(contentDao.selectSeasonCredits(contentId, seasonNo));
	}
}
