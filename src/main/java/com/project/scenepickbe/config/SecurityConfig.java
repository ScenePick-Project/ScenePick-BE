package com.project.scenepickbe.config;

import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(request -> {
				CorsConfiguration corsConfiguration = new CorsConfiguration();
				corsConfiguration.setAllowedOrigins(List.of("http://localhost:5173"));
				corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
				corsConfiguration.setAllowCredentials(true);
				corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
				return corsConfiguration;
			}))
			.authorizeHttpRequests(
				auth -> auth
					.requestMatchers("/api/v1/**")
					.permitAll()
					// Swagger 경로 접근
					.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**")
					.permitAll()
					.anyRequest()
					.authenticated()
			);

		return http.build();
	}
}
