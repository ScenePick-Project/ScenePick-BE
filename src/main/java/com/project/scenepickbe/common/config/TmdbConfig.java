package com.project.scenepickbe.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import com.project.scenepickbe.infrastructure.tmdb.TmdbProperties;

@Configuration
@EnableConfigurationProperties(TmdbProperties.class)
public class TmdbConfig {

	@Bean
	public RestClient tmdbRestClient(TmdbProperties tmdbProperties) {
		return RestClient.builder()
			.baseUrl(tmdbProperties.getBaseUrl())
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tmdbProperties.getApiToken())
			.defaultHeader(HttpHeaders.ACCEPT, "application/json")
			.build();
	}
}
