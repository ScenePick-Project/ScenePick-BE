package com.project.scenepickbe.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserEmailCheckRequestDto", description = "이메일 확인 DTO")
public class UserEmailCheckRequestDto {

	@Schema(description = "회원 이메일")
	@NotBlank(message = "이메일은 필수 입력입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	private String email;
}
