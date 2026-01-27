package com.project.scenepickbe.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserLoginRequestDto", description = "로그인 DTO")
public class UserLoginRequestDto {

	@Schema(description = "회원 ID / 이메일")
	private String loginId;

	@Schema(description = "회원 비밀번호")
	private String password;
}
