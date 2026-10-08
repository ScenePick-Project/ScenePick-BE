package com.project.scenepickbe.mypage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.infrastructure.s3.service.S3QueryService;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

class ProfileImageServiceTest {
	private final S3Client s3 = mock(S3Client.class);
	private final ProfileImageService service = new ProfileImageService(s3, mock(S3QueryService.class));

	@Test
	void rejectsForeignAndMalformedKeysBeforeAccessingS3() {
		for (String key : new String[] {"profiles/other/photo.png", "profiles/me/", "profiles/me/../photo.png", "https://example.test/photo.png"}) {
			assertThatThrownBy(() -> service.validate("me", key)).isInstanceOf(GeneralException.class);
		}
		service.validate("me", null);
		verifyNoInteractions(s3);
	}

	@Test
	void validatesImageMimeSizeAndExistence() {
		ReflectionTestUtils.setField(service, "bucket", "test-bucket");
		for (String mime : new String[] {"image/png", "image/jpeg", "image/webp"}) {
			when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().contentType(mime).contentLength(5242880L).build());
			service.validate("me", "profiles/me/photo.png");
		}
		for (HeadObjectResponse object : new HeadObjectResponse[] {
			HeadObjectResponse.builder().contentLength(1L).build(),
			HeadObjectResponse.builder().contentType("text/html").contentLength(1L).build(),
			HeadObjectResponse.builder().contentType("image/png").contentLength(0L).build(),
			HeadObjectResponse.builder().contentType("image/png").contentLength(5242881L).build()
		}) {
			when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(object);
			assertThatThrownBy(() -> service.validate("me", "profiles/me/photo.png")).isInstanceOf(GeneralException.class);
		}
		when(s3.headObject(any(HeadObjectRequest.class))).thenThrow(S3Exception.builder().statusCode(404).build());
		assertThatThrownBy(() -> service.validate("me", "profiles/me/photo.png")).isInstanceOf(GeneralException.class);
		when(s3.headObject(any(HeadObjectRequest.class))).thenThrow(S3Exception.builder().statusCode(503).build());
		assertThatThrownBy(() -> service.validate("me", "profiles/me/photo.png")).isInstanceOf(S3Exception.class);
	}
}
