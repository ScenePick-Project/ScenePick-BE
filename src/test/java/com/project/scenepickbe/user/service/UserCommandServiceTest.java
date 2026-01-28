package com.project.scenepickbe.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.jwt.dto.JwtToken;
import com.project.scenepickbe.common.jwt.dto.RefreshPayload;
import com.project.scenepickbe.user.dao.RefreshTokenDao;
import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.dto.UserRequest;
import com.project.scenepickbe.user.enums.Role;
import com.project.scenepickbe.user.vo.UserVo;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

	@InjectMocks
	private UserCommandService userCommandService;

	@Mock
	private UserDao userDao;

	@Mock
	private RefreshTokenDao refreshTokenDao;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	private UserRequest.UserSignUp createRequestDto() {
		return new UserRequest.UserSignUp(
			"test1234",
			"test1234@example.com",
			"테스트",
			"password1234"
		);
	}

	@Test
	@DisplayName("회원가입 성공")
	void signupSuccess() {

		UserRequest.UserSignUp requestDto = createRequestDto();

		given(userDao.existsByUserId(requestDto.userId())).willReturn(false);
		given(userDao.existsByEmail(requestDto.email())).willReturn(false);

		given(passwordEncoder.encode(requestDto.password())).willReturn("encodedPw");

		userCommandService.signup(requestDto);

		verify(userDao, times(1)).insertUser(any(UserVo.class));
	}

	@Test
	@DisplayName("회원가입 실패 - 아이디 중복")
	void signupFailIdDuplicate() {
		UserRequest.UserSignUp requestDto = createRequestDto();

		given(userDao.existsByUserId(requestDto.userId())).willReturn(true);

		GeneralException exception = assertThrows(GeneralException.class, () -> {
			userCommandService.signup(requestDto);
		});

		assertThat(exception.getCode()).isEqualTo(ErrorStatus.USER_ID_ALREADY_EXIST);

		verify(userDao, times(0)).insertUser(any());
	}

	@Test
	@DisplayName("회원가입 실패 - 이메일 중복")
	void signupFailEmailDuplicate() {
		UserRequest.UserSignUp requestDto = createRequestDto();

		given(userDao.existsByUserId(requestDto.userId())).willReturn(false);
		given(userDao.existsByEmail(requestDto.email())).willReturn(true);

		GeneralException exception = assertThrows(GeneralException.class, () -> {
			userCommandService.signup(requestDto);
		});

		assertThat(exception.getCode()).isEqualTo(ErrorStatus.USER_EMAIL_ALREADY_EXIST);

		verify(userDao, times(0)).insertUser(any());
	}

	@Test
	@DisplayName("로그인 성공")
	void loginSuccess() {
		// given
		UserRequest.UserLogin requestDto = new UserRequest.UserLogin("testId@example.com", "pw");
		String rawPassword = "test1234";

		UserVo userVo = new UserVo();
		userVo.setUserId("testId");
		userVo.setEmail("testId@exmaple.com");
		userVo.setRole(Role.USER);
		userVo.setPassword(passwordEncoder.encode(rawPassword));

		given(userDao.selectUser("testId@example.com")).willReturn(userVo);
		given(passwordEncoder.matches(requestDto.password(), userVo.getPassword())).willReturn(true);

		JwtToken token = JwtToken.builder()
			.grantType("Bearer")
			.accessToken("access")
			.refreshToken("refresh")
			.refreshJti("jti-123")
			.build();

		given(jwtTokenProvider.generateToken(eq("testId"), anyList())).willReturn(token);

		RefreshPayload payload = new RefreshPayload("testId", "jti-123", Date.from(Instant.now().plusSeconds(3600)));
		given(jwtTokenProvider.parseRefreshToken("refresh")).willReturn(payload);

		// when
		JwtToken result = userCommandService.login(requestDto);

		// then
		assertThat(result).isSameAs(token);

		verify(jwtTokenProvider).generateToken(eq("testId"), anyList());
		verify(refreshTokenDao).insertUserRefreshToken(eq("testId"), eq("jti-123"), eq(payload.getExpiresAt()));
	}

	@Test
	@DisplayName("로그인 실패 - 아이디/이메일이 없는 경우")
	void loginFailUserNotFound() {
		// given
		UserRequest.UserLogin requestDto = new UserRequest.UserLogin("testId@example.com", "pw");
		when(userDao.selectUser("testId@example.com")).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> userCommandService.login(requestDto))
			.isInstanceOf(GeneralException.class)
			.satisfies(res -> {
				GeneralException e = (GeneralException)res;
				assertThat(e.getCode()).isEqualTo(ErrorStatus.USER_LOGIN_FAILED);
			});

		verify(userDao).selectUser("testId@example.com");
		verifyNoInteractions(passwordEncoder, jwtTokenProvider, refreshTokenDao);
	}

	@Test
	@DisplayName("로그인 실패 - 비밀번호가 일치하지 않는 경우")
	void loginFailInvalidPassword() {
		// given
		UserRequest.UserLogin requestDto = new UserRequest.UserLogin("testId@example.com", "wrong_pw");

		UserVo userVo = new UserVo();
		userVo.setUserId("testId");
		userVo.setEmail("testId@example.com");
		userVo.setRole(Role.USER);
		userVo.setPassword("test1234");

		given(userDao.selectUser("testId@example.com")).willReturn(userVo);

		given(passwordEncoder.matches(eq("wrong_pw"), eq("test1234"))).willReturn(false);

		// when & then
		assertThatThrownBy(() -> userCommandService.login(requestDto))
			.isInstanceOf(GeneralException.class)
			.satisfies(res -> {
				GeneralException e = (GeneralException)res;
				assertThat(e.getCode()).isEqualTo(ErrorStatus.USER_LOGIN_FAILED);
			});

		verify(userDao).selectUser("testId@example.com");
		verify(passwordEncoder).matches(anyString(), anyString());
		verifyNoInteractions(jwtTokenProvider, refreshTokenDao);
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	void refreshSuccess() {
		// given
		String oldRefreshToken = "old_refresh_token";
		RefreshPayload oldPayload = new RefreshPayload("testId", "old-jti", Date.from(Instant.now().plusSeconds(3600)));

		given(jwtTokenProvider.parseRefreshToken(oldRefreshToken)).willReturn(oldPayload);
		given(refreshTokenDao.existsActive("testId", "old-jti")).willReturn(1);

		UserVo userVo = new UserVo();
		userVo.setUserId("testId");
		userVo.setRole(Role.USER);
		given(userDao.selectUser("testId")).willReturn(userVo);

		// 새 토큰 생성 설정
		JwtToken newToken = JwtToken.builder()
			.accessToken("new_access")
			.refreshToken("new_refresh")
			.refreshJti("new-jti")
			.build();
		given(jwtTokenProvider.generateToken(eq("testId"), anyList())).willReturn(newToken);

		RefreshPayload newPayload = new RefreshPayload("testId", "new-jti", Date.from(Instant.now().plusSeconds(7200)));
		given(jwtTokenProvider.parseRefreshToken("new_refresh")).willReturn(newPayload);

		// when
		JwtToken result = userCommandService.refresh(oldRefreshToken);

		// then
		assertThat(result.getAccessToken()).isEqualTo("new_access");

		verify(refreshTokenDao).revokeUserRefreshToken("testId", "old-jti");
		verify(refreshTokenDao).insertUserRefreshToken(eq("testId"), eq("new-jti"), any());
	}

	@Test
	@DisplayName("토큰 재발급 실패 - DB에 해당 토큰 정보가 없는 경우")
	void refreshFailTokenNotFound() {
		// given
		String refreshToken = "invalid_refresh_token";
		RefreshPayload payload = new RefreshPayload("testId", "wrong-jti", Date.from(Instant.now().plusSeconds(3600)));

		given(jwtTokenProvider.parseRefreshToken(refreshToken)).willReturn(payload);

		given(refreshTokenDao.existsActive("testId", "wrong-jti")).willReturn(0);

		// when & then
		assertThatThrownBy(() -> userCommandService.refresh(refreshToken))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("유효하지 않은 Refresh Token입니다.");

		verify(refreshTokenDao, never()).revokeUserRefreshToken(anyString(), anyString());
		verify(userDao, never()).selectUser(anyString());
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logoutSuccess() {
		// given
		String refreshToken = "valid_refresh_token";
		RefreshPayload payload = new RefreshPayload("testId", "jti-123", Date.from(Instant.now().plusSeconds(3600)));

		given(jwtTokenProvider.parseRefreshToken(refreshToken)).willReturn(payload);

		// when
		userCommandService.logout(refreshToken);

		// then
		verify(jwtTokenProvider).parseRefreshToken(refreshToken);
		verify(refreshTokenDao).revokeUserRefreshToken("testId", "jti-123");
	}

	@Test
	@DisplayName("로그아웃 실패 - 토큰 파싱 중 예외가 발생해도 로그만 남기고 정상 종료")
	void logoutFail() {
		// given
		String invalidToken = "wrong_token";
		given(jwtTokenProvider.parseRefreshToken(invalidToken)).willThrow(new RuntimeException("Invalid Signature"));

		// when & then
		assertDoesNotThrow(() -> userCommandService.logout(invalidToken));

		verify(refreshTokenDao, never()).revokeUserRefreshToken(anyString(), anyString());
	}
}
