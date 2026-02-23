package com.project.scenepickbe.common.security.oauth;

import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.enums.AuthProvider;
import com.project.scenepickbe.user.enums.Role;
import com.project.scenepickbe.user.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomerOauth2UserService extends DefaultOAuth2UserService {

	private final UserDao userDao;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2USer = super.loadUser(userRequest);

		// 제공자 정보
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// 고유 식별값과 이메일 추출
		String providerId = oAuth2USer.getAttribute("sub");
		String email = oAuth2USer.getAttribute("email");
		String name = oAuth2USer.getAttribute("name");

		// DB 조회(providerId는 중복되지 않는 고유값)
		UserVo userVo = userDao.selectUserByProvider(registrationId.toUpperCase(), providerId);

		if (userVo == null) {
			// 이메일이 없을 경우 대비
			String userEmail = (email != null) ? email : (providerId + "@scenepick.com");

			// userId 생성
			String generatedUserId = registrationId.toLowerCase() + "_" + providerId.substring(0, 8);

			// 회원가입 정보가 없는 경우 신규 저장
			userVo = userVo.builder()
				.userId(generatedUserId)
				.email(userEmail)
				.username(name)
				.provider(AuthProvider.fromString(registrationId.toUpperCase()))
				.providerId(providerId)
				.role(Role.USER)
				.build();

			userDao.insertUser(userVo);
		}

		return oAuth2USer;
	}
}
