package com.project.scenepickbe.content.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.content.dto.response.ContentResponse;
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
	@DocSuccess(ContentResponse.Basic.class)
	@GetMapping("/{contentId}")
	public ResponseEntity<ApiResponse<?>> getContentBasic(@PathVariable Long contentId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getBasicInfo(contentId)));
	}

	@Operation(summary = "작품 출연진 조회", description = "영화 출연진 리스트를 조회합니다.")
	@GetMapping("/{contentId}/credits")
	@DocSuccess(ContentResponse.CreditList.class)
	public ResponseEntity<ApiResponse<?>> getContentCreditList(@PathVariable Long contentId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getCredits(contentId)));
	}

	@Operation(summary = "작품 시즌 에피소드 조회", description = "시즌 에피소드 리스트를 조회합니다.")
	@GetMapping("/{contentId}/seasons/{seasonNo}/episodes")
	@DocSuccess(ContentResponse.EpisodeList.class)
	public ResponseEntity<ApiResponse<?>> getContentSeasonEpisodeList(@PathVariable Long contentId,
		@PathVariable Integer seasonNo) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getEpisodes(contentId, seasonNo)));
	}

	@Operation(summary = "작품 시즌 조회", description = "시즌 리스트를 조회합니다.")
	@GetMapping("/{contentId}/seasons")
	@DocSuccess(ContentResponse.SeasonList.class)
	public ResponseEntity<ApiResponse<?>> getContentSeasonList(@PathVariable Long contentId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getSeasons(contentId)));
	}

	@Operation(summary = "작품 시즌 출연진 조회", description = "TV 시즌별 출연진 리스트를 조회합니다.")
	@GetMapping("/{contentId}/seasons/{seasonNo}/credits")
	@DocSuccess(ContentResponse.CreditList.class)
	public ResponseEntity<ApiResponse<?>> getContentSeasonCreditList(@PathVariable Long contentId,
		@PathVariable Integer seasonNo) {
		return ResponseEntity.ok(ApiResponse.onSuccess(contentQueryService.getSeasonCredits(contentId, seasonNo)));
	}
}
