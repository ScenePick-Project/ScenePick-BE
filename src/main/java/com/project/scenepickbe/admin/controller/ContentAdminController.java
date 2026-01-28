package com.project.scenepickbe.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.apiPayload.ApiResponse;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.service.ContentCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Content Admin", description = "작품 관리자 API")
@RestController
@RequestMapping("/api/v1/admin/contents")
@RequiredArgsConstructor
public class ContentAdminController {
	private final ContentCommandService contentCommandService;

	@Operation(summary = "특정 TMDB ID를 가진 하나의 작품 정보를 가져옵니다.")
	@DocSuccess(ContentImportResponse.Import.class)
	@PostMapping("/import/tmdb")
	public ResponseEntity<ApiResponse<?>> importFromTmdb(@RequestParam ContentType type, @RequestParam Long tmdbId,
		@RequestParam(defaultValue = "false") boolean force) {
		ContentImportResponse.Result result = contentCommandService.importFromTmdb(type, tmdbId, force);
		return ResponseEntity.ok(ApiResponse.onSuccess(result.result(), result.message()));
	}
}
