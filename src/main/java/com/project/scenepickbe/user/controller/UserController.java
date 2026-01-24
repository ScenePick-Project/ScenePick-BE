package com.project.scenepickbe.user.controller;

import com.project.scenepickbe.apiPayload.ApiResponse;
import com.project.scenepickbe.common.jwt.dto.JwtToken;
import com.project.scenepickbe.user.dto.request.UserEmailCheckRequestDto;
import com.project.scenepickbe.user.dto.request.UserLoginRequestDto;
import com.project.scenepickbe.user.dto.request.UserSignUpRequestDto;
import com.project.scenepickbe.user.service.UserCommandService;
import com.project.scenepickbe.user.service.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관련 API")
public class UserController {

	private static final String ACCESS_COOKIE = "access_token";
	private static final String REFRESH_COOKIE = "refresh_token";

	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;

	@Value("${jwt.access-expiration-minutes}")
	private long accessMinutes;

	@Value("${jwt.refresh-expiration-days}")
	private long refreshDays;

	@Operation(summary = "회원가입", description = "회원가입을 합니다.")
	@PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> signup(@RequestBody @Valid UserSignUpRequestDto requestDto) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userCommandService.signup(requestDto)));
	}

	@Operation(summary = "아이디 중복 확인", description = "아이디 중복 검사를 합니다.")
	@GetMapping(value = "/check-id")
	public ResponseEntity<ApiResponse<?>> checkUserIdDuplicate(@RequestParam String userId) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userQueryService.checkUserIdDuplicate(userId)));
	}

	@Operation(summary = "이메일 중복 확인", description = "이메일 중복 검사를 합니다.")
	@PostMapping(value = "/check-email", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> checkEmailDuplicate(@RequestBody @Valid UserEmailCheckRequestDto requestDto) {
		return ResponseEntity.ok(ApiResponse.onSuccess(userQueryService.checkEmailDuplicate(requestDto)));
	}

	@Operation(summary = "로그인", description = "아이디/이메일로 로그인하고 토큰을 쿠키로 발급합니다.")
	@PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid UserLoginRequestDto requestDto) {
		JwtToken token = userCommandService.login(requestDto);

		ResponseCookie accessCookie = buildAccessCookie(token.getAccessToken());
		ResponseCookie refreshCookie = buildRefreshCookie(token.getRefreshToken());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
			.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
			.body(ApiResponse.onSuccess(null));
	}

	@Operation(summary = "토큰 재발급", description = "refresh_token 쿠키로 access/refresh 토큰을 재발급합니다.")
	@PostMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		JwtToken token = userCommandService.refresh(refreshToken);

		ResponseCookie accessCookie = buildAccessCookie(token.getAccessToken());
		ResponseCookie refreshCookie = buildRefreshCookie(token.getRefreshToken());

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
			.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
			.body(ApiResponse.onSuccess(null));
	}

	@Operation(summary = "로그아웃", description = "refresh를 폐기하고 쿠키를 삭제합니다.")
	@PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiResponse<?>> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
		userCommandService.logout(refreshToken);

		ResponseCookie deleteAccess = deleteAccessCookie();
		ResponseCookie deleteRefresh = deleteRefreshCookie();

		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
			.header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
			.body(ApiResponse.onSuccess(null));
	}

	// 쿠키 설정
	private ResponseCookie buildAccessCookie(String token) {
		return ResponseCookie.from(ACCESS_COOKIE, token)
			.httpOnly(true)
			.secure(false) // 로컬 http : false / 운영 https : true
			.sameSite("Lax")
			.path("/")
			.maxAge(Duration.ofMinutes(accessMinutes))
			.build();
	}

	private ResponseCookie buildRefreshCookie(String token) {
		return ResponseCookie.from(REFRESH_COOKIE, token)
			.httpOnly(true)
			.secure(false) // 로컬 http : false / 운영 https : true
			.sameSite("Lax")
			.path("/api/v1/user")
			.maxAge(Duration.ofDays(refreshDays))
			.build();
	}

	private ResponseCookie deleteAccessCookie() {
		return ResponseCookie.from(ACCESS_COOKIE, "")
			.httpOnly(true)
			.secure(false)
			.sameSite("Lax")
			.path("/")
			.maxAge(0)
			.build();
	}

	private ResponseCookie deleteRefreshCookie() {
		return ResponseCookie.from(REFRESH_COOKIE, "")
			.httpOnly(true)
			.secure(false)
			.sameSite("Lax")
			.path("/api/v1/user")
			.maxAge(0)
			.build();
	}
}
