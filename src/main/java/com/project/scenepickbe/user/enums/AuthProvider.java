package com.project.scenepickbe.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
	LOCAL("일반 가입"),
	GOOGLE("구글"),
	KAKAO("카카오"),
	NAVER("네이버"),
	APPLE("애플");

	private final String description;

	// String(ex. google)을 넣으면 해당 Enum을 찾아주는 메서드
	public static AuthProvider fromString(String registrationId) {
		for (AuthProvider provider : AuthProvider.values()) {
			if (provider.name().equalsIgnoreCase(registrationId)) {
				return provider;
			}
		}
		return LOCAL; // 매칭되는 게 없으면 일반 가입으로 처리
	}
}
