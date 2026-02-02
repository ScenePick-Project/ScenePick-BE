package com.project.scenepickbe.content.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("CreditVo")
public class CreditVo {

	/** 출연진 고유 식별자 (PK) */
	private Long creditId;

	/** 연결된 작품의 고유 식별자 (FK) */
	private Long contentId;

	/** 연결된 시즌의 고유 식별자 (FK) */
	private Long seasonId;

	/** 활동명 */
	private String name;

	/** 배역명 */
	private String charName;

	/** 프로필 이미지 URL */
	private String profileImageUrl;
}
