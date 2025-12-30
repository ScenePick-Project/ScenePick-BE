package com.project.scenepickbe.content.vo;

import java.util.List;

import org.apache.ibatis.type.Alias;

import com.project.scenepickbe.common.BaseVo;
import com.project.scenepickbe.content.enums.GenreType;

import lombok.Data;

@Data
@Alias("ContentVo")
public class ContentVo extends BaseVo {
	private Long contentId;
	private String title;
	private String posterImageUrl;
	private List<GenreType> genres;
	private String synopsis;

}
