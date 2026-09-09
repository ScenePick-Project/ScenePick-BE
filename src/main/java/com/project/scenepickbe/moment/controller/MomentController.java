package com.project.scenepickbe.moment.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.moment.dto.request.MomentRequest;
import com.project.scenepickbe.moment.dto.response.MomentResponse;
import com.project.scenepickbe.moment.service.MomentCommandService;
import com.project.scenepickbe.moment.service.MomentQueryService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@ApiResponses({
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
		description = "COMMON400: 잘못된 입력·시간 구간 / PAGING4001: 커서 쌍 불일치",
		content = @Content(schema = @Schema(implementation = ApiResponse.class))),
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
		description = "COMMON401: 로그인 필요",
		content = @Content(schema = @Schema(implementation = ApiResponse.class))),
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
		description = "CONTENT4001: 작품 없음 / MOMENT4001: 본인 모먼트 없음 또는 삭제됨",
		content = @Content(schema = @Schema(implementation = ApiResponse.class)))
})
@Tag(name = "Moment", description = "본인만 접근할 수 있는 개인 모먼트 API")
public class MomentController {
	private final MomentCommandService momentCommandService;
	private final MomentQueryService momentQueryService;

	@GetMapping("/moments/{momentId}")
	@Operation(summary = "내 모먼트 단건 조회", description = "본인이 저장한 모먼트만 조회합니다.")
	@DocSuccess(MomentResponse.Detail.class)
	public ResponseEntity<ApiResponse<?>> getMoment(
		@PathVariable Long momentId,
		@AuthenticationPrincipal(errorOnInvalidType = false) User user) {
		return ResponseEntity.ok(ApiResponse.onSuccess(momentQueryService.getMoment(momentId, userId(user))));
	}

	@GetMapping("/me/moments")
	@Operation(summary = "내 모먼트 목록 조회", description = "작품·영상 필터와 최신 저장순 커서를 지원합니다.")
	@DocSuccess(MomentResponse.Slice.class)
	public ResponseEntity<ApiResponse<?>> getMoments(
		@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@Valid @ParameterObject MomentRequest.Slice request) {
		return ResponseEntity.ok(ApiResponse.onSuccess(momentQueryService.getMoments(userId(user), request)));
	}

	@PostMapping("/contents/{contentId}/moments")
	@Operation(summary = "내 모먼트 저장", description = "리뷰 없이 영상 구간과 선택 메모를 저장합니다.")
	@DocSuccess(MomentResponse.Created.class)
	public ResponseEntity<ApiResponse<?>> createMoment(
		@PathVariable Long contentId,
		@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@RequestBody @Valid MomentRequest.Create request) {
		return ResponseEntity.ok(ApiResponse.onSuccess(
			momentCommandService.createMoment(contentId, userId(user), request)));
	}

	@PatchMapping("/moments/{momentId}")
	@Operation(summary = "내 모먼트 부분 수정",
		description = "생략한 필드는 유지합니다. memo는 null 또는 빈 문자열로 지웁니다. 최종 시간 구간을 검증합니다.")
	@DocSuccess(MomentResponse.Detail.class)
	public ResponseEntity<ApiResponse<?>> updateMoment(
		@PathVariable Long momentId,
		@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@RequestBody @Valid MomentRequest.Update request) {
		return ResponseEntity.ok(ApiResponse.onSuccess(
			momentCommandService.updateMoment(momentId, userId(user), request)));
	}

	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
		description = "삭제 성공. result는 null입니다.",
		content = @Content(schema = @Schema(implementation = ApiResponse.class)))
	@DeleteMapping("/moments/{momentId}")
	@Operation(summary = "내 모먼트 삭제", description = "본인 모먼트를 소프트 삭제하여 목록과 상세에서 제외합니다.")
	public ResponseEntity<ApiResponse<?>> deleteMoment(
		@PathVariable Long momentId,
		@AuthenticationPrincipal(errorOnInvalidType = false) User user) {
		momentCommandService.deleteMoment(momentId, userId(user));
		return ResponseEntity.ok(ApiResponse.onSuccess(null));
	}

	private String userId(User user) {
		if (user == null) {
			throw new GeneralException(ErrorStatus._UNAUTHORIZED);
		}
		return user.getUsername();
	}
}
