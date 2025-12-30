package com.project.scenepickbe.content.vo;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("EpisodeVo")
public class EpisodeVo {
	private Long episodeId;
	private Long contentId;
	private Integer episodeNo;
	private String title;
	private String summary;
}
