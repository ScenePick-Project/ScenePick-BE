package com.project.scenepickbe.user.service;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.jwt.dto.JwtToken;
import com.project.scenepickbe.common.jwt.dto.RefreshPayload;
import com.project.scenepickbe.user.dao.RefreshTokenDao;
import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.dto.UserRequest;
import com.project.scenepickbe.user.dto.UserResponse;
import com.project.scenepickbe.user.enums.AuthProvider;
import com.project.scenepickbe.user.enums.Role;
import com.project.scenepickbe.user.vo.UserVo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
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

	@Transactional
	public void changePassword(String userId, UserRequest.PasswordChange request) {
		UserVo user = userDao.selectUserById(userId);
		if (user == null) {
			throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
		}
		if (user.getPassword() == null || user.getPassword().isBlank()
			|| (user.getProvider() != null
				&& user.getProvider() != AuthProvider.LOCAL)) {
			throw new GeneralException(ErrorStatus.USER_PASSWORD_UNAVAILABLE);
		}
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw new GeneralException(ErrorStatus.USER_PASSWORD_MISMATCH);
		}
		if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
			throw new GeneralException(ErrorStatus.USER_PASSWORD_UNCHANGED);
		}
		// BCrypt는 UTF-8 기준 72바이트까지만 저장할 수 있다.
		if (request.newPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}
		String encoded = passwordEncoder.encode(request.newPassword());
		if (userDao.updatePassword(userId, user.getPassword(), encoded) != 1) {
			throw new GeneralException(ErrorStatus.USER_PASSWORD_MISMATCH);
		}
		refreshTokenDao.revokeAllUserRefreshTokens(userId);
	}

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
		if (exists == 0)
			throw new RuntimeException("유효하지 않은 Refresh Token입니다.");

		UserVo userVo = userDao.selectUser(payload.getUserId());
		if (userVo == null) {
			throw new GeneralException(ErrorStatus.USER_LOGIN_FAILED);
		}

		// 회원 행 잠금 대기 중 비밀번호 변경으로 폐기됐을 수 있으므로 다시 확인한다.
		if (refreshTokenDao.existsActive(payload.getUserId(), payload.getJti()) == 0) {
			throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
		}
		refreshTokenDao.revokeUserRefreshToken(payload.getUserId(), payload.getJti());

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
		if (refreshToken == null || refreshToken.isBlank())
			return;
		try {
			RefreshPayload payload = jwtTokenProvider.parseRefreshToken(refreshToken);
			refreshTokenDao.revokeUserRefreshToken(payload.getUserId(), payload.getJti());
		} catch (Exception e) {
			log.warn("로그아웃 처리 중 예외 발생: {}", e.getMessage());
		}
	}

	/**
	 * 토큰에 담긴 회원정보
	 */
	public UserResponse.UserAuth getUserAuth(Authentication authentication) {
		// 인증 정보가 없거나 로그인하지 않은 익명 사용자일 경우 예외 처리
		if (authentication == null || !authentication.isAuthenticated()
			|| "anonymousUser".equals(authentication.getName())) {
			return null;
		}

		// 인증 정보가 확실히 있을 때만 정보 추출
		String userId = authentication.getName();
		String role = pickRole(authentication.getAuthorities());
		return new UserResponse.UserAuth(userId, role);
	}

	/**
	 * 대표 권한 선택 정책
	 */
	private String pickRole(Collection<? extends GrantedAuthority> authorities) {
		return authorities.stream()
			.map(GrantedAuthority::getAuthority) // "ROLE_ADMIN" 같은 값
			.findFirst()
			.orElseThrow(() -> new GeneralException(ErrorStatus.JWT_NO_AUTH)); // 권한 없으면 예외
	}
}
