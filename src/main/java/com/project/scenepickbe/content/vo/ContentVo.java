package com.project.scenepickbe.content.vo;

import java.util.List;

import org.apache.ibatis.type.Alias;

import com.project.scenepickbe.common.BaseVo;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.enums.GenreType;

import lombok.Data;

@Data
@Alias("ContentVo")
public class ContentVo extends BaseVo {

	/** 작품 고유 식별자 (PK) */
	private Long contentId;

	/** TMDB 작품 ID */
	private Long tmdbId;

	/** 작품 타입 (TV/MOVIE) */
	private ContentType contentType;

	/** 작품 제목 */
	private String title;

	/** 포스터 이미지 URL */
	private String posterImageUrl;

	/** 장르 리스트 */
	private List<GenreType> genreList;

	/** 줄거리 요약 */
	private String synopsis;

}
