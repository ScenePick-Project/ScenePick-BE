package com.project.scenepickbe.infrastructure.s3.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.scenepickbe.infrastructure.s3.dto.response.S3Response;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@RequiredArgsConstructor
@Service
public class S3QueryService {

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	private final S3Presigner s3Presigner;
	private static final long EXPIRATION_TIME = 10;

	/**
	 * 조회/다운로드용 Presigned URL 발급
	 * @param keyName DB에 저장되어 있는 파일의 전체 경로
	 * @return 해당 파일을 브라우저에서 볼 수 있는 임시 URL 응답 객체
	 */
	public S3Response.PresignedUrlToDownload getPresignedToDownload(String keyName) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucket)
			.key(keyName)
			.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(EXPIRATION_TIME))
			.getObjectRequest(getObjectRequest)
			.build();

		return new S3Response.PresignedUrlToDownload(
			s3Presigner.presignGetObject(presignRequest).url().toString()
		);
	}
}
