package com.project.scenepickbe.common.config;

import com.project.scenepickbe.common.jwt.JwtAuthenticationEntryPoint;
import com.project.scenepickbe.common.jwt.JwtAuthenticationFilter;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.security.oauth.CustomerOauth2UserService;
import com.project.scenepickbe.common.security.oauth.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomerOauth2UserService customerOauth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtTokenProvider jwtTokenProvider,
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
	) throws Exception {
		http
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(request -> {
				CorsConfiguration corsConfiguration = new CorsConfiguration();
				corsConfiguration.setAllowedOrigins(List.of("http://localhost:5173"));
				corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
				corsConfiguration.setAllowCredentials(true);
				corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
				return corsConfiguration;
			}))
			.exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint))
			.authorizeHttpRequests(
				auth -> auth
					.requestMatchers(
						"/api/v1/user/signup",
						"/api/v1/user/refresh",
						"/api/v1/user/login",
						"/api/v1/contents/{contentId}",
						"/api/v1/contents/{contentId}/persons",
						"/api/v1/contents/{contentId}/episodes"
					)
					.permitAll()
					// Swagger 경로 접근
					.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**")
					.permitAll()
					.anyRequest()
					.authenticated()
			)

			// CustomerOauth2UserService 등록
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customerOauth2UserService)
				)
				.successHandler(oAuth2SuccessHandler)
			)
			// 로그아웃
			.logout(logout -> logout
				.logoutUrl("/api/v1/user/logout")
				.logoutSuccessHandler((request, response, authentication) -> {
					response.setStatus(HttpServletResponse.SC_OK); // 성공 시 200 OK만 반환
				})
			)

			// JWT 쿠키 필터 추가
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, jwtAuthenticationEntryPoint),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}
}
