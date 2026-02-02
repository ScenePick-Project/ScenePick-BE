package com.project.scenepickbe.content.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("ContentSeasonVo")
public class ContentSeasonVo {

	/** 시즌 고유 식별자 (PK) */
	private Long seasonId;

	/** 연결된 작품의 고유 식별자 (FK) */
	private Long contentId;

	/** 시즌 번호 */
	private Integer seasonNo;

	/** 시즌 이름 */
	private String name;

	/** 시즌 개요 */
	private String overview;

	/** 시즌 포스터 이미지 URL */
	private String posterImageUrl;
}
