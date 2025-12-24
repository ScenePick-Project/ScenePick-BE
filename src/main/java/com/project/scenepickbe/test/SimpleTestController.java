package com.project.scenepickbe.test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/simple/test")
@RequiredArgsConstructor
public class SimpleTestController {

	private final SimpleTestService simpleTestService;

	@GetMapping("/list")
	public ResponseEntity<?> getSimpleTestList() {
		SimpleTestListResDto resDto = simpleTestService.getSimpleTestList();

		return new ResponseEntity<>(resDto, HttpStatus.OK);
	}
}
