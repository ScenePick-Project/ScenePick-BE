package com.project.scenepickbe.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.scenepickbe.apiPayload.code.ErrorReasonDTO;
import com.project.scenepickbe.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException {
		// 필터에 담은 에러
		ErrorReasonDTO reasonDTO = (ErrorReasonDTO) request.getAttribute("JWT_ERROR_REASON");

		// 토큰이 아예 없거나 필터에서 reason을 안 담는 경우의 기본값
		if (reasonDTO == null) {
			reasonDTO = ErrorStatus._UNAUTHORIZED.getReasonHttpStatus();
		}

		response.setStatus(reasonDTO.getHttpStatus().value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		response.getWriter().write(objectMapper.writeValueAsString(reasonDTO));
	}
}
