package com.project.scenepickbe.common.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint entryPoint;

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
			} catch (GeneralException e) {
				SecurityContextHolder.clearContext();

				request.setAttribute("JWT_ERROR_REASON", e.getErrorReasonHttpStatus());

				entryPoint.commence(
					request,
					response,
					new org.springframework.security.core.AuthenticationException("JWT 인증 실패", e) {
					}
				);

				return;
			}
		}

		// 다음 필터로 진행
		filterChain.doFilter(request, response);
	}

	private String resolveCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return null;

		for (Cookie cookie : cookies) {
			if (cookieName.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
