package com.project.scenepickbe.common.apiPayload.code.exception;

import com.project.scenepickbe.common.apiPayload.code.BaseErrorCode;
import com.project.scenepickbe.common.apiPayload.code.ErrorReasonDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

	private BaseErrorCode code;

	public ErrorReasonDTO getErrorReason() {
		return this.code.getReason();
	}

	public ErrorReasonDTO getErrorReasonHttpStatus() {
		return this.code.getReasonHttpStatus();
	}
}

