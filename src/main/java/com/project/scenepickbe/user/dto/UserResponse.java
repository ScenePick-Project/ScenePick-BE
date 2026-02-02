package com.project.scenepickbe.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponse {

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "UserSignUpResponse", description = "회원가입 정보 응답")
	public record UserSignUp(

		@Schema(description = "회원 ID")
		String userId
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "UserAuth", description = "토큰에 담긴 회원정보")
	public record UserAuth(
		@Schema(description = "회원 ID")
		String userId,

		@Schema(description = "권한")
		String role
	) {
	}
}
