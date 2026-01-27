package com.project.scenepickbe.common.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(name = "JwtToken", description = "JwtToken")
public class JwtToken {
	private String grantType;
	private String accessToken;
	private String refreshToken;
	private String refreshJti;
}
