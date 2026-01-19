package com.project.scenepickbe.common.s3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.apiPayload.ApiResponse;
import com.project.scenepickbe.common.CustomUserDetails;
import com.project.scenepickbe.common.s3.dto.request.S3RequestDto;
import com.project.scenepickbe.common.s3.dto.response.S3ResponseDto;
import com.project.scenepickbe.common.s3.enums.S3Domain;
import com.project.scenepickbe.common.s3.service.S3CommandService;
import com.project.scenepickbe.common.s3.service.S3QueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "S3", description = "S3 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
public class S3Controller {

	private final S3CommandService s3CommandService;
	private final S3QueryService s3QueryService;

	@Operation(summary = "업로드를 위해 Presigned URL 생성", description = "도메인별 업로드를 위한 Presigned URL을 생성합니다.")
	@PostMapping("/presigned/upload/{domain}")
	public ResponseEntity<ApiResponse<S3ResponseDto.PresignedUrlToUploadResponse>> getPresignedUrlToUpload(
		@PathVariable S3Domain domain,
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody S3RequestDto.PresignedUrlToUploadRequest request) {

		String testUserId = "testId1";

		// TODO 인증 로직 완성 후 userDetails.getUserId() 수정 필요
		return ResponseEntity.ok(
			ApiResponse.onSuccess(s3CommandService.getPresignedUrlToUpload(testUserId, domain, request)));
	}

	@Operation(summary = "다운로드를 위해 Presigned URL 생성", description = "파일 조회를 위하여 Presigned URL을 생성합니다.")
	@GetMapping("/presigned/download")
	public ResponseEntity<ApiResponse<S3ResponseDto.PresignedUrlToDownloadResponse>> getPresignedUrlToDownload(
		@RequestParam(value = "keyName") String keyName) {

		return ResponseEntity.ok(ApiResponse.onSuccess(s3QueryService.getPresignedToDownload(keyName)));
	}
}
