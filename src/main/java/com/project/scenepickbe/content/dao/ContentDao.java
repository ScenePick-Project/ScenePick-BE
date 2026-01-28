package com.project.scenepickbe.content.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.enums.GenreType;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.EpisodeVo;
import com.project.scenepickbe.content.vo.PersonVo;

@Mapper
public interface ContentDao {

	/**
	 * 특정 작품 기본 정보 조회
	 */
	ContentVo selectContentBasic(Long contentId);

	/**
	 * 특정 작품에 속한 출연진 리스트 조회
	 */
	List<PersonVo> selectContentPersons(Long contentId);

	/**
	 * 특정 작품에 속한 에피소드 리스트 조회, 회차순 정렬
	 */
	List<EpisodeVo> selectContentEpisodes(Long contentId);

	/**
	 * TMDB ID로 작품 ID 조회
	 */
	Long selectContentIdByTmdb(@Param("tmdbId") Long tmdbId, @Param("contentType") ContentType contentType);

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
	int deleteContentPersons(Long contentId);

	/**
	 * 작품 에피소드 초기화
	 */
	int deleteContentEpisodes(Long contentId);

	/**
	 * 작품 장르 등록
	 */
	int insertContentGenre(@Param("contentId") Long contentId, @Param("genre") GenreType genre);

	/**
	 * 작품 출연진 등록
	 */
	int insertContentPerson(PersonVo person);

	/**
	 * 작품 에피소드 등록
	 */
	int insertContentEpisode(EpisodeVo episode);

}
