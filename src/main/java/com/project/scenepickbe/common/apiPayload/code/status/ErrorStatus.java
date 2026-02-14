package com.project.scenepickbe.common.apiPayload.code.status;

import org.springframework.http.HttpStatus;

import com.project.scenepickbe.common.apiPayload.code.BaseErrorCode;
import com.project.scenepickbe.common.apiPayload.code.ErrorReasonDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

	// 가장 일반적인 응답
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
	_BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
	_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
	_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

	// JWT
	JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT4011", "만료된 토큰입니다."),
	JWT_INVALID(HttpStatus.UNAUTHORIZED, "JWT4012", "유효하지 않은 토큰입니다."),
	JWT_NOT_ACCESS(HttpStatus.UNAUTHORIZED, "JWT4013", "Access Token이 아닙니다."),
	JWT_NO_AUTH(HttpStatus.UNAUTHORIZED, "JWT4014", "권한 정보가 없는 토큰입니다."),

	// User
	USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4001", "사용자가 없습니다."),
	USERNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "USER4002", "이름은 필수 입니다."),
	USER_ID_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "USER4003", "이미 존재하는 아이디입니다."),
	USER_EMAIL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "USER4004", "이미 존재하는 이메일입니다."),
	USER_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "USER4005", "아이디 또는 비밀번호가 올바르지 않습니다."),

	// Content
	CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT4001", "해당 작품을 찾을 수 없습니다."),
	CONTENT_IMPORT_FAIL(HttpStatus.NOT_FOUND, "CONTENT4002", "TMDB 작품 등록에 실패했습니다."),

	// TMDB
	TMDB_API_FAIL(HttpStatus.BAD_GATEWAY, "TMDB5001", "TMDB API 호출에 실패했습니다."),

	// Paging
	CURSOR_INVALID(HttpStatus.BAD_REQUEST, "PAGING4001", "커서 파라미터 구성이 올바르지 않습니다."),
	INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "PAGING4002", "페이지 크기는 1보다 커야 합니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	@Override
	public ErrorReasonDTO getReason() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(false)
			.build();
	}

	@Override
	public ErrorReasonDTO getReasonHttpStatus() {
		return ErrorReasonDTO.builder()
			.message(message)
			.code(code)
			.isSuccess(false)
			.httpStatus(httpStatus)
			.build()
			;
	}
}
