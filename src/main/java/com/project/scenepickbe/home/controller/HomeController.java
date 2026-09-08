package com.project.scenepickbe.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.home.dto.response.HomeResponse;
import com.project.scenepickbe.home.service.HomeQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 화면 조회 API")
public class HomeController {

	private final HomeQueryService homeQueryService;

	@Operation(summary = "홈 랜덤 추천 작품 조회", description = """
		로그인한 사용자에게 포스터가 있는 영화/TV 작품을 중복 없이 최대 10개 제공합니다.
		후보가 10개 미만이면 모두 반환하며, 없으면 contentList는 빈 배열입니다.
		개인화하지 않은 RANDOM 추천이며 요청마다 다른 결과를 보장하지 않습니다.
		""")
	@SecurityRequirement(name = "cookieAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
	@DocSuccess(HomeResponse.Recommendations.class)
	@GetMapping("/recommendations")
	public ResponseEntity<ApiResponse<HomeResponse.Recommendations>> getRecommendations() {
		return ResponseEntity.ok(ApiResponse.onSuccess(homeQueryService.getRecommendations()));
	}
}
