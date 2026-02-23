package com.project.scenepickbe.common.security.oauth;

public interface OAuth2UserInfo {
	String getProvider();

	String getProviderId();

	String getEmail();

	String getName();
}
