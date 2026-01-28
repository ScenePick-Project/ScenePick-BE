package com.project.scenepickbe.infrastructure.s3.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.scenepickbe.infrastructure.s3.dto.request.S3Request;
import com.project.scenepickbe.infrastructure.s3.dto.response.S3Response;
import com.project.scenepickbe.infrastructure.s3.enums.S3Domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3CommandService {

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	private final S3Presigner s3Presigner;
	private static final long EXPIRATION_TIME = 10;

	/**
	 * 업로드용 Presigned URL 발급
	 * @param userId   사용자 아이디
	 * @param domain   파일 도메인
	 * @param request  업로드할 fileName과 contentType을 담은 DTO
	 * @return Presigned URL과 keyName을 포함한 응답
	 */
	public S3Response.PresignedUrlToUpload getPresignedUrlToUpload(
		String userId, S3Domain domain, S3Request.PresignedUrlToUpload request) {

		String keyName = createKeyName(domain, userId, request.fileName());

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(keyName)
			.contentType(request.contentType())
			.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(EXPIRATION_TIME))
			.putObjectRequest(putObjectRequest)
			.build();

		String url = s3Presigner.presignPutObject(presignRequest).url().toString();

		log.info("[S3 Upload] 발급 완료 - ID: {}, 경로: {}", userId, keyName);

		return new S3Response.PresignedUrlToUpload(keyName, url);
	}

	/**
	 * S3 객체 키 생성
	 * @param domain   파일 도메인
	 * @param userId   사용자 아이디
	 * @param fileName 원본 파일 이름
	 * @return 생성된 S3 KeyName
	 */
	private String createKeyName(S3Domain domain, String userId, String fileName) {
		String uuid = UUID.randomUUID().toString();
		return String.format("%s/%s/%s_%s", domain.getFolderName(), userId, uuid, fileName);
	}
}
