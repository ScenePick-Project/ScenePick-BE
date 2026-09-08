package com.project.scenepickbe.moment.controller;

import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;

@RestControllerAdvice(assignableTypes = MomentController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MomentExceptionAdvice {
	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	public ResponseEntity<ApiResponse<?>> malformedInput() {
		return ResponseEntity.badRequest().body(ApiResponse.onFailure(
			ErrorStatus._BAD_REQUEST.getCode(), ErrorStatus._BAD_REQUEST.getMessage(), null));
	}
}
