package com.project.scenepickbe.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserSignUpResponseDto", description = "회원가입 정보 응답 DTO")
public class UserSignUpResponseDto {

	@Schema(description = "회원 ID")
	private String userId;

	@Schema(description = "회원가입일")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private Date createdAt;
}
