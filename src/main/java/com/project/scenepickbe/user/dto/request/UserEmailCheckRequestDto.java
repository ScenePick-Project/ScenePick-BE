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
@Schema(name = "UserEmailCheckRequestDto", description = "이메일 확인 DTO")
public class UserEmailCheckRequestDto {

	@Schema(description = "회원 이메일")
	private String email;
}
