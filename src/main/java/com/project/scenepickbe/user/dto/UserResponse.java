package com.project.scenepickbe.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponse {

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "UserSignUpResponseDto", description = "회원가입 정보 응답 DTO")
	public record UserSignUp(

		@Schema(description = "회원 ID")
		String userId
	) {
	}
}
