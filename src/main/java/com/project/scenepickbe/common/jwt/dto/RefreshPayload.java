package com.project.scenepickbe.common.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Getter
@RequiredArgsConstructor
@Schema(name = "RefreshPayload", description = "Refresh 토큰 파싱 결과를 담는 DTO")
public class RefreshPayload {
	private final String userId;
	private final String jti;
	private final Date expiresAt;
}
