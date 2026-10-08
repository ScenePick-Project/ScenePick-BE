package com.project.scenepickbe.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.service.ContentCommandService;
import com.project.scenepickbe.content.service.RandomContentImportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Content Admin", description = "작품 관리자 API")
@RestController
@RequestMapping("/api/v1/admin/contents")
@RequiredArgsConstructor
public class ContentAdminController {
	private final ContentCommandService contentCommandService;
	private final RandomContentImportService randomContentImportService;

	@Operation(summary = "특정 TMDB ID를 가진 하나의 작품 정보를 가져옵니다.")
	@DocSuccess(ContentImportResponse.Import.class)
	@PostMapping("/import/tmdb")
	public ResponseEntity<ApiResponse<?>> importFromTmdb(@RequestParam ContentType type, @RequestParam Long tmdbId,
		@RequestParam(defaultValue = "false") boolean force) {
		ContentImportResponse.Result result = contentCommandService.importFromTmdb(type, tmdbId, force);
		return ResponseEntity.ok(ApiResponse.onSuccess(result.result(), result.message()));
	}

	@Operation(summary = "TMDB 인기 작품을 무작위로 여러 건 가져옵니다.", description = """
		TMDB 인기 영화와 TV 작품 목록을 합쳐 무작위로 선택해 DB에 저장합니다.
		기본 10건이며, 한 요청당 최대 20건까지 처리합니다. 이미 등록된 작품은 건너뜁니다.
		""")
	@DocSuccess(ContentImportResponse.Batch.class)
	@PostMapping("/import/tmdb/random")
	public ResponseEntity<ApiResponse<ContentImportResponse.Batch>> importRandomFromTmdb(
		@RequestParam(defaultValue = "10") int count
	) {
		if (count < 1 || count > 20) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
		return ResponseEntity.ok(ApiResponse.onSuccess(randomContentImportService.importRandomPopularContents(count)));
	}
}
