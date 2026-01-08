package com.project.scenepickbe.user.service;

import com.project.scenepickbe.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.dto.request.UserSignUpRequestDto;
import com.project.scenepickbe.user.dto.response.UserSignUpResponseDto;
import com.project.scenepickbe.user.enums.Role;
import com.project.scenepickbe.user.vo.UserVo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserDao userDao;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 회원가입
	 *
	 * @param requestDto 회원가입 정보
	 * @return UserSignUpResponseDto
	 */
	@Transactional
	public UserSignUpResponseDto signup(UserSignUpRequestDto requestDto) {

		if (userDao.existsByUserId(requestDto.getUserId())) {
			throw new GeneralException(ErrorStatus.USER_ID_ALREADY_EXIST);
		}

		if (userDao.existsByEmail(requestDto.getEmail())) {
			throw new GeneralException(ErrorStatus.USER_EMAIL_ALREADY_EXIST);
		}

		UserVo userVo = UserVo.builder()
			.userId(requestDto.getUserId())
			.email(requestDto.getEmail())
			.password(passwordEncoder.encode(requestDto.getPassword()))
			.username(requestDto.getUsername())
			.role(Role.USER)
			.build();

		userDao.insertUser(userVo);

		return UserSignUpResponseDto.builder()
			.userId(userVo.getUserId())
			.build();
	}
}
