package com.project.scenepickbe.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserSignUpRequestDto", description = "회원가입 DTO")
public class UserSignUpRequestDto {

	@Schema(description = "회원 ID")
	@NotBlank(message = "아이디는 필수 입력입니다.")
	@Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
	private String userId;

	@Schema(description = "회원 이메일")
	@NotBlank(message = "이메일은 필수 입력입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;

	@Schema(description = "회원 이름")
	@NotBlank(message = "이름은 필수 입력입니다.")
	@Size(min = 2, max = 20, message = "이름은 2~20자여야 합니다.")
	private String username;

	@Schema(description = "회원 비밀번호")
	@NotBlank(message = "비밀번호는 필수 입력입니다.")
	@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	private String password;

	@Schema(description = "회원 전화번호")
	@NotBlank(message = "전화번호는 필수 입력입니다.")
	private String phone;

}
