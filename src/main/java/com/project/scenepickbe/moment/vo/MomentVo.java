package com.project.scenepickbe.moment.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MomentVo {
	private Long momentId;
	private String userId;
	private Long contentId;
	private String youtubeId;
	private Integer startTime;
	private Integer endTime;
	private String memo;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
