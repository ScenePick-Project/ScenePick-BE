package com.project.scenepickbe.common;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public abstract class BaseVo {
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
