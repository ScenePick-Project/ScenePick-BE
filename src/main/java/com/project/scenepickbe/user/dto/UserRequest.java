package com.project.scenepickbe.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "UserSignUpRequestDto", description = "회원가입 DTO")
	public record UserSignUp(
		@Schema(description = "회원 ID")
		@NotBlank(message = "아이디는 필수 입력입니다.")
		@Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
		String userId,

		@Schema(description = "회원 이메일")
		@NotBlank(message = "이메일은 필수 입력입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email,

		@Schema(description = "회원 이름")
		@NotBlank(message = "이름은 필수 입력입니다.")
		@Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
		String username,

		@Schema(description = "회원 비밀번호")
		@NotBlank(message = "비밀번호는 필수 입력입니다.")
		@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
		String password
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "UserEmailCheckRequestDto", description = "이메일 확인 DTO")
	public record UserEmailCheck(
		@Schema(description = "회원 이메일")
		@NotBlank(message = "이메일은 필수 입력입니다.")
		@Email(message = "이메일 형식이 올바르지 않습니다.")
		String email
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Schema(name = "UserLoginRequestDto", description = "로그인 DTO")
	public record UserLogin(
		@Schema(description = "회원 ID / 이메일")
		String loginId,

		@Schema(description = "회원 비밀번호")
		String password
	) {
	}
}
