package com.project.scenepickbe.common.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public static final String ACCESS_TOKEN_COOKIE = "access_token";

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		// 쿠키에서 access token 꺼내기
		String accessToken = resolveCookie(request, ACCESS_TOKEN_COOKIE);

		// access token이 있으면 인증 세팅 시도
		if (StringUtils.hasText(accessToken)) {
			try {
				Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (RuntimeException e) {
				// 토큰이 만료/위조/refresh 등으로 인증 실패한 경우
				log.debug("JWT 인증 실패: {}", e.getMessage());
				SecurityContextHolder.clearContext();
			}
		}

		// 다음 필터로 진행
		filterChain.doFilter(request, response);
	}

	private String resolveCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) return null;

		for (Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
