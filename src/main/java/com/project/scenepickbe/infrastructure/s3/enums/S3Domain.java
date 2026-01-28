package com.project.scenepickbe.infrastructure.s3.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3Domain {
	PROFILES("profiles"),
	THUMBNAILS("thumbnails");

	private final String folderName;
}
