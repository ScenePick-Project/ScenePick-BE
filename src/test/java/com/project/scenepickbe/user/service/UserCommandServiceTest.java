package com.project.scenepickbe.user.service;

import com.project.scenepickbe.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.dto.request.UserSignUpRequestDto;
import com.project.scenepickbe.user.vo.UserVo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

	@InjectMocks
	private UserCommandService userCommandService;

	@Mock
	private UserDao userDao;

	@Mock
	private PasswordEncoder passwordEncoder;

	private UserSignUpRequestDto createRequestDto() {
		return UserSignUpRequestDto.builder()
			.userId("test1234")
			.email("test1234@example.com")
			.password("password1234")
			.username("테스트")
			.phone("010-1234-5678")
			.build();
	}

	@Test
	@DisplayName("회원가입 성공")
	void signupSuccess() {

		UserSignUpRequestDto requestDto = createRequestDto();

		given(userDao.existsByUserId(requestDto.getUserId())).willReturn(false);
		given(userDao.existsByEmail(requestDto.getEmail())).willReturn(false);

		given(passwordEncoder.encode(requestDto.getPassword())).willReturn("encodedPw");

		userCommandService.signup(requestDto);

		verify(userDao, times(1)).insertUser(any(UserVo.class));
	}

	@Test
	@DisplayName("회원가입 실패 - 아이디 중복")
	void signupFailIdDuplicate() {
		UserSignUpRequestDto requestDto = createRequestDto();

		given(userDao.existsByUserId(requestDto.getUserId())).willReturn(true);

		GeneralException exception = assertThrows(GeneralException.class, () -> {
			userCommandService.signup(requestDto);
		});

		assertThat(exception.getCode()).isEqualTo(ErrorStatus.USER_ID_ALREADY_EXIST);

		verify(userDao, times(0)).insertUser(any());
	}

	@Test
	@DisplayName("회원가입 실패 - 이메일 중복")
	void signupFailEmailDuplicate() {
		UserSignUpRequestDto requestDto = createRequestDto();

		given(userDao.existsByUserId(requestDto.getUserId())).willReturn(false);
		given(userDao.existsByEmail(requestDto.getEmail())).willReturn(true);

		GeneralException exception = assertThrows(GeneralException.class, () -> {
			userCommandService.signup(requestDto);
		});

		assertThat(exception.getCode()).isEqualTo(ErrorStatus.USER_EMAIL_ALREADY_EXIST);

		verify(userDao, times(0)).insertUser(any());
	}
}
