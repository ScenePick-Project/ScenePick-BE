package com.project.scenepickbe.content.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("PersonVo")
public class PersonVo {
	private Long personId;
	private Long contentId;
	private String name;
	private String charName;
	private String profileImageUrl;
}
