package com.project.scenepickbe.user.controller;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.jwt.CookieProvider;
import com.project.scenepickbe.common.jwt.dto.JwtToken;
import com.project.scenepickbe.common.swagger.DocSuccess;
import com.project.scenepickbe.user.dto.UserRequest;
import com.project.scenepickbe.user.dto.UserResponse;
import com.project.scenepickbe.user.service.UserCommandService;
import com.project.scenepickbe.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;
	private final CookieProvider cookieProvider;

	@Operation(summary = "회원가입", description = "회원가입을 합니다.")
	@DocSuccess(UserResponse.UserSignUp.class)
	@PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> signup(@RequestBody @Valid UserRequest.UserSignUp requestDto) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userCommandService.signup(requestDto)));
	}

	@Operation(summary = "아이디 중복 확인", description = "아이디 중복 검사를 합니다.")
	@GetMapping(value = "/check-id")
	public ResponseEntity<ApiResponse<?>> checkUserIdDuplicate(@RequestParam String userId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userQueryService.checkUserIdDuplicate(userId)));
	}

	@Operation(summary = "이메일 중복 확인", description = "이메일 중복 검사를 합니다.")
	@PostMapping(value = "/check-email", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> checkEmailDuplicate(
		@RequestBody @Valid UserRequest.UserEmailCheck requestDto) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userQueryService.checkEmailDuplicate(requestDto)));
	}

	@Operation(summary = "로그인", description = "아이디/이메일로 로그인하고 토큰을 쿠키로 발급합니다.")
	@PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid UserRequest.UserLogin requestDto) {
		JwtToken token = userCommandService.login(requestDto);

		ResponseCookie accessCookie = cookieProvider.createAccessCookie(token.getAccessToken());
		ResponseCookie refreshCookie = cookieProvider.createRefreshCookie(token.getRefreshToken());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
			.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
			.body(ApiResponse.onSuccess(token));
	}

	@Operation(summary = "토큰 재발급", description = "refresh_token 쿠키로 access/refresh 토큰을 재발급합니다.")
	@PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> refresh(
		@CookieValue(name = CookieProvider.REFRESH_COOKIE, required = false) String refreshToken) {
		JwtToken token = userCommandService.refresh(refreshToken);

		ResponseCookie accessCookie = cookieProvider.createAccessCookie(token.getAccessToken());
		ResponseCookie refreshCookie = cookieProvider.createRefreshCookie(token.getRefreshToken());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
			.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
			.body(ApiResponse.onSuccess(null));
	}

	@Operation(summary = "로그아웃", description = "refresh를 폐기하고 쿠키를 삭제합니다.")
	@PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> logout(
		@CookieValue(name = CookieProvider.REFRESH_COOKIE, required = false) String refreshToken) {
		userCommandService.logout(refreshToken);

		ResponseCookie deleteAccess = cookieProvider.deleteAccessCookie();
		ResponseCookie deleteRefresh = cookieProvider.deleteRefreshCookie();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
			.header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
			.body(ApiResponse.onSuccess(null));
	}

	@Operation(summary = "로그인 여부 판단", description = "로그인이 되었는지 확인합니다.")
	@DocSuccess(UserResponse.UserAuth.class)
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<?>> me(Authentication authentication) {
		UserResponse.UserAuth userAuth = userCommandService.getUserAuth(authentication);

		return ResponseEntity.ok(ApiResponse.onSuccess(userAuth));
	}
}
