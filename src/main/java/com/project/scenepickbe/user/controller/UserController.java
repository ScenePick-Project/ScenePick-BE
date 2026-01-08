package com.project.scenepickbe.user.controller;

import com.project.scenepickbe.apiPayload.ApiResponse;
import com.project.scenepickbe.user.dto.request.UserEmailCheckRequestDto;
import com.project.scenepickbe.user.dto.request.UserSignUpRequestDto;
import com.project.scenepickbe.user.service.UserCommandService;
import com.project.scenepickbe.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;

	@Operation(summary = "회원가입", description = "회원가입을 합니다.")
	@PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> signup(@RequestBody UserSignUpRequestDto requestDto) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userCommandService.signup(requestDto)));
	}

	@Operation(summary = "아이디 중복 확인", description = "아이디 중복 검사를 합니다.")
	@GetMapping(value = "/check-id")
	public ResponseEntity<ApiResponse<?>> checkUserIdDuplicate(@RequestParam String userId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userQueryService.checkUserIdDuplicate(userId)));
	}

	@Operation(summary = "회원가입", description = "회원가입을 합니다.")
	@PostMapping(value = "/check-email", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> checkEmailDuplicate(@RequestBody @Valid UserEmailCheckRequestDto requestDto) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userQueryService.checkEmailDuplicate(requestDto)));
	}

}
