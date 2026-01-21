package com.project.scenepickbe.common.s3.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "S3ResponseDto", description = "S3 파일 관련 응답 DTO")
public class S3ResponseDto {

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "S3 업로드 URL 응답 DTO")
	public static class PresignedUrlToUploadResponse {
		private String keyName;
		private String url;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	@Schema(description = "S3 다운로드 URL 응답 DTO")
	public static class PresignedUrlToDownloadResponse {
		private String url;
	}

}
