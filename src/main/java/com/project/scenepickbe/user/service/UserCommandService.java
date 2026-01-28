package com.project.scenepickbe.user.service;

import com.project.scenepickbe.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.jwt.dto.JwtToken;
import com.project.scenepickbe.common.jwt.dto.RefreshPayload;
import com.project.scenepickbe.user.dao.RefreshTokenDao;
import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.dto.UserRequest;
import com.project.scenepickbe.user.dto.UserResponse;
import com.project.scenepickbe.user.enums.Role;
import com.project.scenepickbe.user.vo.UserVo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserDao userDao;
	private final RefreshTokenDao refreshTokenDao;
	private final JwtTokenProvider jwtTokenProvider;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입
	 *
	 * @param requestDto 회원가입 정보
	 * @return UserSignUpResponseDto
	 */
	@Transactional
	public UserResponse.UserSignUp signup(UserRequest.UserSignUp requestDto) {

		if (userDao.existsByUserId(requestDto.userId())) {
			throw new GeneralException(ErrorStatus.USER_ID_ALREADY_EXIST);
		}

		if (userDao.existsByEmail(requestDto.email())) {
			throw new GeneralException(ErrorStatus.USER_EMAIL_ALREADY_EXIST);
		}

		UserVo userVo = UserVo.builder()
			.userId(requestDto.userId())
			.email(requestDto.email())
			.password(passwordEncoder.encode(requestDto.password()))
			.username(requestDto.username())
			.role(Role.USER)
			.build();

		userDao.insertUser(userVo);

		return new UserResponse.UserSignUp(userVo.getUserId());
	}

	/**
	 * 로그인
	 *
	 * @param requestDto 로그인 정보
	 * @return JwtToken
	 */
	@Transactional
	public JwtToken login(UserRequest.UserLogin requestDto) {
		UserVo userVo = userDao.selectUser(requestDto.loginId());

		// 회원정보를 찾지 못한 경우
		if (userVo == null) {
			throw new GeneralException(ErrorStatus.USER_LOGIN_FAILED);
		}

		// 비밀번호가 알맞지 않은 경우
		if (!passwordEncoder.matches(requestDto.password(), userVo.getPassword())) {
			throw new GeneralException(ErrorStatus.USER_LOGIN_FAILED);
		}

		String authority = "ROLE_" + userVo.getRole();

		JwtToken token = jwtTokenProvider.generateToken(
			userVo.getUserId(),
			List.of(new SimpleGrantedAuthority(authority))
		);

		// REFRESH 저장
		RefreshPayload payload = jwtTokenProvider.parseRefreshToken(token.getRefreshToken());
		refreshTokenDao.insertUserRefreshToken(userVo.getUserId(), token.getRefreshJti(), payload.getExpiresAt());

		return token;
	}

	/**
	 * Refresh 토큰을 이용해 새로운 액세스 토큰을 발급
	 *
	 * @param refreshToken 기존에 발급된 리프레시 토큰
	 * @return JwtToken
	 */
	@Transactional
	public JwtToken refresh(String refreshToken) {
		RefreshPayload payload = jwtTokenProvider.parseRefreshToken(refreshToken);

		int exists = refreshTokenDao.existsActive(payload.getUserId(), payload.getJti());
		if (exists == 0) throw new RuntimeException("유효하지 않은 Refresh Token입니다.");

		refreshTokenDao.revokeUserRefreshToken(payload.getUserId(), payload.getJti());

		UserVo userVo = userDao.selectUser(payload.getUserId());
		if (userVo == null) {
			throw new GeneralException(ErrorStatus.USER_LOGIN_FAILED);
		}

		String authority = "ROLE_" + userVo.getRole();

		JwtToken newToken = jwtTokenProvider.generateToken(
			userVo.getUserId(),
			List.of(new SimpleGrantedAuthority(authority))
		);

		RefreshPayload newPayload = jwtTokenProvider.parseRefreshToken(newToken.getRefreshToken());
		refreshTokenDao.insertUserRefreshToken(userVo.getUserId(), newToken.getRefreshJti(), newPayload.getExpiresAt());

		return newToken;
	}

	/**
	 * 로그아웃
	 *
	 * @param refreshToken 기존에 발급된 리프레시 토큰
	 * @return
	 */
	@Transactional
	public void logout(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) return;
		try {
			RefreshPayload payload = jwtTokenProvider.parseRefreshToken(refreshToken);
			refreshTokenDao.revokeUserRefreshToken(payload.getUserId(), payload.getJti());
		} catch (Exception e) {
			log.warn("로그아웃 처리 중 예외 발생: {}", e.getMessage());
		}
	}
}
