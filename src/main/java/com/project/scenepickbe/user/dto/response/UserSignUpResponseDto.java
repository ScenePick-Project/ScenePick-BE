package com.project.scenepickbe.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserSignUpResponseDto", description = "회원가입 정보 응답 DTO")
public class UserSignUpResponseDto {

	@Schema(description = "회원 ID")
	private String userId;
}
