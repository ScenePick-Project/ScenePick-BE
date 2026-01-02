package com.project.scenepickbe.content.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("EpisodeVo")
public class EpisodeVo {

	/** 에피소드 고유 식별자 (PK) */
	private Long episodeId;

	/** 연결된 작품의 고유 식별자 (FK) */
	private Long contentId;

	/** 회차 */
	private Integer episodeNo;

	/** 회차별 제목 */
	private String title;

	/** 회차 줄거리 요약 */
	private String summary;
}
