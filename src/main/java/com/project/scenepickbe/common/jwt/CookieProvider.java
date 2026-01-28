package com.project.scenepickbe.common.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieProvider {

	public static final String ACCESS_COOKIE = "access_token";
	public static final String REFRESH_COOKIE = "refresh_token";

	@Value("${jwt.access-expiration-minutes}")
	private long accessMinutes;

	@Value("${jwt.refresh-expiration-days}")
	private long refreshDays;

	// 쿠키 설정
	public ResponseCookie createAccessCookie(String token) {
		return ResponseCookie.from(ACCESS_COOKIE, token)
			.httpOnly(true)
			.secure(false) // 로컬 http : false / 운영 https : true
			.sameSite("Lax")
			.path("/")
			.maxAge(Duration.ofMinutes(accessMinutes))
			.build();
	}

	public ResponseCookie createRefreshCookie(String token) {
		return ResponseCookie.from(REFRESH_COOKIE, token)
			.httpOnly(true)
			.secure(false) // 로컬 http : false / 운영 https : true
			.sameSite("Lax")
			.path("/api/v1/user")
			.maxAge(Duration.ofDays(refreshDays))
			.build();
	}

	public ResponseCookie deleteAccessCookie() {
		return ResponseCookie.from(ACCESS_COOKIE, "")
			.httpOnly(true)
			.secure(false)
			.sameSite("Lax")
			.path("/")
			.maxAge(0)
			.build();
	}

	public ResponseCookie deleteRefreshCookie() {
		return ResponseCookie.from(REFRESH_COOKIE, "")
			.httpOnly(true)
			.secure(false)
			.sameSite("Lax")
			.path("/api/v1/user")
			.maxAge(0)
			.build();
	}
}
