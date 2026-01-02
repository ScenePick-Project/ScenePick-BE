package com.project.scenepickbe.content.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

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

}
