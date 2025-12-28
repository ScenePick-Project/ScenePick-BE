package com.project.scenepickbe.content.vo;

import lombok.Data;

@Data
public class EpisodeVo {
	private Long id;
	private Long contentId;
	private Integer episodeNo;
	private String title;
	private String summary;
}
