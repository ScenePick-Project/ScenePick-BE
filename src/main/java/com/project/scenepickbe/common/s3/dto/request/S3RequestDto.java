package com.project.scenepickbe.common.s3.dto.request;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "S3RequestDto", description = "S3 파일 업로드 관련 요청 DTO")
public class S3RequestDto {

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Presigned URL 발급 요청 DTO")
	public static class PresignedUrlToUploadRequest {

		@NotBlank(message = "파일명은 필수입니다.")
		@Length(min = 1, max = 100)
		private String fileName;

		@NotBlank(message = "컨텐츠 타입(MIME)은 필수입니다.")
		private String contentType;
	}

}
