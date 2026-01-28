package com.project.scenepickbe.infrastructure.s3.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "S3Response", description = "S3 파일 관련 응답")
public class S3Response {

	@Schema(description = "S3 업로드 URL 응답")
	public record PresignedUrlToUpload(
		String keyName,
		String url
	) {
	}

	@Schema(description = "S3 다운로드 URL 응답")
	public record PresignedUrlToDownload(
		String url
	) {
	}

}
