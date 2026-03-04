package com.project.scenepickbe.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.review.dto.response.ReviewLikeResponse;
import com.project.scenepickbe.review.service.ReviewLikeCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "ReviewLike", description = "리뷰 좋아요 관련 API")
public class ReviewLikeController {

	private final ReviewLikeCommandService reviewLikeCommandService;

	@Operation(summary = "리뷰 좋아요 토글", description = "리뷰 좋아요를 추가하거나 취소합니다. 이미 좋아요한 경우 취소되고, 좋아요하지 않은 경우 추가됩니다.")
	@DocSuccess(ReviewLikeResponse.Toggle.class)
	@PostMapping("/reviews/{reviewId}/like")
	public ResponseEntity<ApiResponse<?>> toggleReviewLike(
		@PathVariable Long reviewId,
		@AuthenticationPrincipal User user) {
		return ResponseEntity.ok(ApiResponse.onSuccess(
			reviewLikeCommandService.toggleReviewLike(reviewId, user.getUsername())));
	}
}
