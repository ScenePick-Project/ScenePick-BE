package com.project.scenepickbe.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.apiPayload.ApiResponse;
import com.project.scenepickbe.content.service.ContentQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/contents")
@RequiredArgsConstructor
@Tag(name = "Content", description = "작품 관련 API")
public class ContentController {

	private final ContentQueryService contentQueryService;

	@Operation(summary = "작품 기본정보 조회", description = "기본정보를 조회합니다.")
	@GetMapping("/{contentId}")
	public ResponseEntity<ApiResponse<?>> getContentBasic(@PathVariable Long contentId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getBasicInfo(contentId)));
	}

	@Operation(summary = "작품 출연진 조회", description = "출연진 리스트를 조회합니다.")
	@GetMapping("/{contentId}/persons")
	public ResponseEntity<ApiResponse<?>> getContentPersonList(@PathVariable Long contentId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getPersons(contentId)));
	}

	@Operation(summary = "작품 에피소드 조회", description = "에피소드 리스트를 조회합니다.")
	@GetMapping("/{contentId}/episodes")
	public ResponseEntity<ApiResponse<?>> getContentEpisodeList(@PathVariable Long contentId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getEpisodes(contentId)));
	}
}
