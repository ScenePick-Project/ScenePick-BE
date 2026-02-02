package com.project.scenepickbe.content.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.vo.ContentSeasonVo;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.CreditVo;
import com.project.scenepickbe.content.vo.EpisodeVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentQueryService {
	private final ContentDao contentDao;

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
			? contentDao.selectContentSeasons(contentId).stream()
			.map(season -> new ContentResponse.Season(
				season.getSeasonId(),
				season.getSeasonNo(),
				season.getName(),
				season.getOverview(),
				season.getPosterImageUrl()
			))
			.collect(Collectors.toList())
			: List.of();

		return new ContentResponse.Basic(
			contentVo.getContentId(),
			contentVo.getTitle(),
			contentVo.getPosterImageUrl(),
			contentVo.getSynopsis(),
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
		List<CreditVo> creditVoList = contentDao.selectContentCredits(contentId);

		List<ContentResponse.Credit> creditDtoList = creditVoList.stream()
			.map(vo -> new ContentResponse.Credit(
				vo.getCreditId(),
				vo.getName(),
				vo.getCharName(),
				vo.getProfileImageUrl()
			))
			.collect(Collectors.toList());

		return new ContentResponse.CreditList(creditDtoList);
	}

	/**
	 * 작품 시즌 에피소드 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @param seasonNo 시즌 번호
	 * @return 에피소드 리스트 DTO
	 */
	public ContentResponse.EpisodeList getEpisodes(Long contentId, Integer seasonNo) {
		List<EpisodeVo> episodeVoList = contentDao.selectSeasonEpisodes(contentId, seasonNo);

		List<ContentResponse.Episode> episodeList = episodeVoList.stream()
			.map(vo -> new ContentResponse.Episode(
				vo.getEpisodeId(),
				vo.getEpisodeNo(),
				vo.getTitle(),
				vo.getSummary(),
				vo.getStillImageUrl()
			))
			.collect(Collectors.toList());

		return new ContentResponse.EpisodeList(episodeList);
	}

	/**
	 * 작품 시즌 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @return 시즌 리스트 DTO
	 */
	public ContentResponse.SeasonList getSeasons(Long contentId) {
		List<ContentSeasonVo> seasonVoList = contentDao.selectContentSeasons(contentId);

		List<ContentResponse.Season> seasonList = seasonVoList.stream()
			.map(season -> new ContentResponse.Season(
				season.getSeasonId(),
				season.getSeasonNo(),
				season.getName(),
				season.getOverview(),
				season.getPosterImageUrl()
			))
			.collect(Collectors.toList());

		return new ContentResponse.SeasonList(seasonList);
	}

	/**
	 * 작품 시즌 출연진 리스트 조회
	 * @param contentId 작품 고유 ID
	 * @param seasonNo 시즌 번호
	 * @return 시즌 출연진 리스트 DTO
	 */
	public ContentResponse.CreditList getSeasonCredits(Long contentId, Integer seasonNo) {
		List<CreditVo> seasonCreditVoList = contentDao.selectSeasonCredits(contentId, seasonNo);

		List<ContentResponse.Credit> seasonCreditList = seasonCreditVoList.stream()
			.map(credit -> new ContentResponse.Credit(
				credit.getCreditId(),
				credit.getName(),
				credit.getCharName(),
				credit.getProfileImageUrl()
			))
			.collect(Collectors.toList());

		return new ContentResponse.CreditList(seasonCreditList);
	}
}
