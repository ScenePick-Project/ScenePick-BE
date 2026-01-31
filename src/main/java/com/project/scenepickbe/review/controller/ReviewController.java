package com.project.scenepickbe.review.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.review.dto.request.ReviewRequest;
import com.project.scenepickbe.review.dto.response.ReviewResponse;
import com.project.scenepickbe.review.service.ReviewCommandService;
import com.project.scenepickbe.review.service.ReviewQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {
	private final ReviewCommandService reviewCommandService;
	private final ReviewQueryService reviewQueryService;

	@Operation(summary = "특정 리뷰 단건 조회", description = "리뷰 하나를 조회합니다.")
	@DocSuccess(ReviewResponse.Review.class)
	@GetMapping("/reviews/{reviewId}")
	public ResponseEntity<ApiResponse<?>> getReview(@PathVariable Long reviewId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(reviewQueryService.getReview(reviewId)));
	}

	@Operation(summary = "특정 작품 리뷰목록 조회", description = "리뷰 목록을 조회합니다.")
	@DocSuccess(ReviewResponse.ReviewList.class)
	@GetMapping("/contents/{contentId}/reviews")
	public ResponseEntity<ApiResponse<?>> getReviewList(@PathVariable Long contentId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(reviewQueryService.getReviewList(contentId)));
	}

	@Operation(summary = "작품 리뷰 작성", description = "특정 작품의 리뷰를 등록합니다.")
	@DocSuccess(ReviewResponse.Created.class)
	@PostMapping("/contents/{contentId}/reviews")
	public ResponseEntity<ApiResponse<?>> createReview(@PathVariable Long contentId,
		@AuthenticationPrincipal User user,
		@RequestBody @Valid ReviewRequest.Create request) {
		return ResponseEntity.ok(ApiResponse.onSuccess(
			reviewCommandService.createReview(contentId, user.getUsername(), request)));
	}

	@Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<ApiResponse<?>> deleteReview(@PathVariable Long reviewId,
		@AuthenticationPrincipal User user) {
		reviewCommandService.deleteReview(reviewId, user.getUsername());
		return ResponseEntity.ok(ApiResponse.onSuccess(null));
	}
}
