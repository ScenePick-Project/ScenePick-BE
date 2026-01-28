package com.project.scenepickbe.infrastructure.s3.dto.request;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "S3Request", description = "S3 파일 업로드 관련 요청")
public class S3Request {

	@Schema(description = "Presigned URL 발급 요청")
	public record PresignedUrlToUpload(
		@NotBlank(message = "파일명은 필수입니다.")
		@Length(min = 1, max = 100)
		String fileName,
		@NotBlank(message = "컨텐츠 타입(MIME)은 필수입니다.")
		String contentType
	) {
	}

}
