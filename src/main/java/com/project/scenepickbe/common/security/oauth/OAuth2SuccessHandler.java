package com.project.scenepickbe.common.security.oauth;

import com.project.scenepickbe.common.jwt.CookieProvider;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.jwt.dto.JwtToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final CookieProvider cookieProvider;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		// OAuth2User로부터 인증된 정보를 갖고 옴
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		// 고유값 추출
		String userId = oAuth2User.getAttribute("sub");

		// JwtTokenProvider로 우리 서비스 전용 토큰 발급
		JwtToken jwtToken = jwtTokenProvider.generateToken(userId, authentication.getAuthorities());

		ResponseCookie accessCookie = cookieProvider.createAccessCookie(jwtToken.getAccessToken());
		ResponseCookie refreshCookie = cookieProvider.createRefreshCookie(jwtToken.getRefreshToken());

		response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

		// 리다이렉트 URL에서 토큰 파라미터 제거 (깨끗한 URL로 이동)
		String targetUrl = "http://localhost:5173/oauth/callback";

		// 이동
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
