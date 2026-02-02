package com.project.scenepickbe.content.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.enums.GenreType;
import com.project.scenepickbe.content.vo.ContentSeasonVo;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.CreditVo;
import com.project.scenepickbe.content.vo.EpisodeVo;

@Mapper
public interface ContentDao {

	/**
	 * 특정 작품 기본 정보 조회
	 */
	ContentVo selectContentBasic(Long contentId);

	/**
	 * 특정 작품에 속한 출연진 리스트 조회
	 */
	List<CreditVo> selectContentCredits(Long contentId);

	/**
	 * 특정 작품의 특정 시즌 에피소드 리스트 조회, 회차순 정렬
	 */
	List<EpisodeVo> selectSeasonEpisodes(@Param("contentId") Long contentId, @Param("seasonNo") Integer seasonNo);

	/**
	 * 특정 작품에 속한 시즌 리스트 조회, 시즌순 정렬
	 */
	List<ContentSeasonVo> selectContentSeasons(Long contentId);

	/**
	 * 특정 시즌에 속한 출연진 리스트 조회
	 */
	List<CreditVo> selectSeasonCredits(@Param("contentId") Long contentId,
		@Param("seasonNo") Integer seasonNo);

	/**
	 * TMDB ID로 작품 ID 조회
	 */
	Long selectContentIdByTmdb(@Param("tmdbId") Long tmdbId, @Param("contentType") ContentType contentType);

	/**
	 * 작품 ID와 시즌 번호로 시즌 ID 조회
	 */
	Long selectSeasonId(@Param("contentId") Long contentId, @Param("seasonNo") Integer seasonNo);

	/**
	 * 작품 기본 정보 등록
	 */
	int insertContent(ContentVo content);

	/**
	 * TMDB 기준 작품 기본 정보 갱신
	 */
	int updateContentByTmdb(ContentVo content);

	/**
	 * 작품 장르 초기화
	 */
	int deleteContentGenres(Long contentId);

	/**
	 * 작품 출연진 초기화
	 */
	int deleteContentCredits(Long contentId);

	/**
	 * 작품 에피소드 초기화
	 */
	int deleteContentEpisodes(Long contentId);

	/**
	 * 작품 시즌 초기화
	 */
	int deleteContentSeasons(Long contentId);

	/**
	 * 작품 장르 등록
	 */
	int insertContentGenre(@Param("contentId") Long contentId, @Param("genre") GenreType genre);

	/**
	 * 작품 출연진 등록
	 */
	int insertContentCredit(CreditVo credit);

	/**
	 * 작품 에피소드 등록
	 */
	int insertContentEpisode(EpisodeVo episode);

	/**
	 * 작품 시즌 등록
	 */
	int insertContentSeason(ContentSeasonVo season);

	/**
	 * 시즌 출연진 등록
	 */
	int insertSeasonCredit(CreditVo credit);

}
