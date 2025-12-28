package com.project.scenepickbe.content.vo;

import java.util.List;

import com.project.scenepickbe.common.BaseVo;
import com.project.scenepickbe.content.enums.GenreType;

import lombok.Data;

@Data
public class ContentVo extends BaseVo {
	private Long id;
	private String title;
	private String posterImageUrl;
	private List<GenreType> genres;
	private String synopsis;

}
