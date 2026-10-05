package com.project.scenepickbe.mypage;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.infrastructure.s3.service.S3QueryService;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class ProfileImageService {
	private final S3Client s3Client;
	private final S3QueryService s3QueryService;
	@Value("${spring.cloud.aws.s3.bucket}") private String bucket;

	public void validate(String userId, String key) {
		if (key == null) return;
		String prefix = "profiles/" + userId + "/";
		if (!key.startsWith(prefix) || key.length() > 1000 || key.length() == prefix.length()
			|| key.substring(prefix.length()).contains("/") || key.contains("\\")
			|| key.contains("..") || key.chars().anyMatch(Character::isISOControl)) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
		try {
			var object = s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
			if (object.contentType() == null || !Set.of("image/png", "image/jpeg", "image/webp").contains(object.contentType())
				|| object.contentLength() == null || object.contentLength() <= 0 || object.contentLength() > 5 * 1024 * 1024) {
				throw new GeneralException(ErrorStatus._BAD_REQUEST);
			}
		} catch (S3Exception exception) {
			if (exception.statusCode() == 404) throw new GeneralException(ErrorStatus._BAD_REQUEST);
			throw exception;
		}
	}
	public String url(String key) {
		return key == null ? null : s3QueryService.getPresignedToDownload(key).url();
	}
}
