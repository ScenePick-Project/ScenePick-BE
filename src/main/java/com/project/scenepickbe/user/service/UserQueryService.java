package com.project.scenepickbe.user.service;

import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.dto.request.UserEmailCheckRequestDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

	private final UserDao userDao;
	private final ModelMapper modelMapper;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 아이디 중복 확인
	 *
	 * @param userId 아이디 정보
	 * @return boolean
	 */
	public boolean checkUserIdDuplicate(String userId) {
		return userDao.existsByUserId(userId);
	}

	/**
	 * 이메일 중복 확인
	 *
	 * @param requestDto 아이디 정보
	 * @return boolean
	 */
	public boolean checkEmailDuplicate(UserEmailCheckRequestDto requestDto) {
		return userDao.existsByEmail(requestDto.getEmail());
	}
}
