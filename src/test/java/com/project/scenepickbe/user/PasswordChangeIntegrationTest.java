package com.project.scenepickbe.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.modelmapper.ModelMapper;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.scenepickbe.common.apiPayload.code.exception.ExceptionAdvice;
import com.project.scenepickbe.common.config.SecurityConfig;
import com.project.scenepickbe.common.jwt.CookieProvider;
import com.project.scenepickbe.common.jwt.JwtAuthenticationEntryPoint;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.security.oauth.CustomerOauth2UserService;
import com.project.scenepickbe.common.security.oauth.OAuth2SuccessHandler;
import com.project.scenepickbe.user.controller.UserController;
import com.project.scenepickbe.user.dao.RefreshTokenDao;
import com.project.scenepickbe.user.dao.UserDao;
import com.project.scenepickbe.user.dto.UserRequest;
import com.project.scenepickbe.user.enums.AuthProvider;
import com.project.scenepickbe.user.enums.Role;
import com.project.scenepickbe.user.service.UserCommandService;
import com.project.scenepickbe.user.service.UserQueryService;
import com.project.scenepickbe.user.vo.UserVo;

import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;

@SpringJUnitConfig(PasswordChangeIntegrationTest.TestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = {"jwt.access-expiration-minutes=5", "jwt.refresh-expiration-days=1"})
@Testcontainers
class PasswordChangeIntegrationTest {
	@Container
	static final OracleContainer ORACLE = new OracleContainer(DockerImageName.parse(
		"gvenzl/oracle-xe@sha256:c2682a4216b0fe65537912c4a049c7e5c15a85bb7c94bb8137d5f2d2eef60603"))
		.withUsername("password_test").withPassword("password_test_only")
		.withStartupTimeout(Duration.ofMinutes(3));

	@Autowired
	WebApplicationContext context;
	@Autowired
	DataSource dataSource;
	@Autowired
	UserCommandService command;
	@Autowired
	UserDao users;
	@Autowired
	JwtTokenProvider jwt;
	@MockitoSpyBean
	RefreshTokenDao refreshTokens;
	private MockMvc mvc;
	private JdbcTemplate db;

	@BeforeEach
	void setUp() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context)
			.addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
		db = new JdbcTemplate(dataSource);
		db.update("DELETE FROM USER_REFRESH_TOKEN");
		db.update("DELETE FROM USERS");
		mvc.perform(post("/api/v1/user/signup").contentType(MediaType.APPLICATION_JSON)
			.content("""
				{"userId":"owner","email":"owner@example.com","username":"회원","password":"old-password"}
				""")).andExpect(status().isOk());
	}

	@Test
	void changesPasswordAndExpiresBothCookies() throws Exception {
		mvc.perform(patch("/api/v1/user/password").cookie(login("owner", "old-password"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"currentPassword\":\"old-password\",\"newPassword\":\"new-password\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(cookie().maxAge("access_token", 0))
			.andExpect(cookie().maxAge("refresh_token", 0));
		login("owner", "new-password");
		mvc.perform(post("/api/v1/user/login").contentType(MediaType.APPLICATION_JSON)
			.content("{\"loginId\":\"owner\",\"password\":\"old-password\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void revokesEveryDeviceButKeepsOtherMembersAndExistingAccessTokens() throws Exception {
		var first = command.login(new UserRequest.UserLogin("owner", "old-password"));
		var second = command.login(new UserRequest.UserLogin("owner", "old-password"));
		users.insertUser(UserVo.builder().userId("other").email("other@example.com").username("다른회원")
			.password(context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class)
				.encode("other-password")).role(Role.USER).build());
		var other = command.login(new UserRequest.UserLogin("other", "other-password"));
		mvc.perform(change("old-password", "new-password").cookie(new Cookie("access_token", first.getAccessToken())))
			.andExpect(status().isOk()).andExpect(jsonPath("$.result").doesNotExist());
		assertThatThrownBy(() -> command.refresh(first.getRefreshToken())).isInstanceOf(RuntimeException.class);
		assertThatThrownBy(() -> command.refresh(second.getRefreshToken())).isInstanceOf(RuntimeException.class);
		assertThat(command.refresh(other.getRefreshToken()).getAccessToken()).isNotBlank();
		mvc.perform(get("/api/v1/user/me").cookie(new Cookie("access_token", first.getAccessToken())))
			.andExpect(jsonPath("$.result.userId").value("owner"));
	}

	@Test
	void wrongCurrentPasswordAndUnchangedPasswordLeaveCredentialsAndRefreshValid() throws Exception {
		var session = command.login(new UserRequest.UserLogin("owner", "old-password"));
		var cookie = new Cookie("access_token", session.getAccessToken());
		mvc.perform(change("wrong-password", "new-password").cookie(cookie))
			.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("USER4006"))
			.andExpect(cookie().doesNotExist("access_token"));
		mvc.perform(change("old-password", "old-password").cookie(cookie))
			.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("USER4007"));
		login("owner", "old-password");
		assertThat(command.refresh(session.getRefreshToken()).getAccessToken()).isNotBlank();
	}

	@ParameterizedTest
	@ValueSource(strings = {"{}", "{\"currentPassword\":\"old-password\"}",
		"{\"currentPassword\":\" \",\"newPassword\":\"new-password\"}",
		"{\"currentPassword\":\"old-password\",\"newPassword\":\"short\"}",
		"{\"currentPassword\":\"old-password\",\"newPassword\":null}"})
	void rejectsInvalidInputWithoutChangingPassword(String body) throws Exception {
		mvc.perform(patch("/api/v1/user/password").cookie(login("owner", "old-password"))
			.contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON400"));
		login("owner", "old-password");
	}

	@Test
	void rejectsAnonymousAndInvalidTokens() throws Exception {
		mvc.perform(change("old-password", "new-password")).andExpect(status().isUnauthorized());
		mvc.perform(change("old-password", "new-password").cookie(new Cookie("access_token", "invalid")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void refusesSocialOnlyMembers() throws Exception {
		for (AuthProvider provider : List.of(AuthProvider.NAVER, AuthProvider.KAKAO, AuthProvider.GOOGLE)) {
			String id = provider.name().toLowerCase();
			users.insertUser(UserVo.builder().userId(id).email(id + "@example.com").username("소셜회원")
				.role(Role.USER).provider(provider).providerId("test-" + id).build());
			mvc.perform(change("old-password", "new-password").cookie(access(id)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("USER4008"));
		}
	}

	@Test
	void usesAuthenticatedIdEvenWhenBodyNamesAnotherMember() throws Exception {
		users.insertUser(UserVo.builder().userId("other").email("other@example.com").username("다른회원")
			.password(context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class)
				.encode("other-password")).role(Role.USER).build());
		mvc.perform(patch("/api/v1/user/password").cookie(access("owner")).contentType(MediaType.APPLICATION_JSON)
			.content("{\"userId\":\"other\",\"currentPassword\":\"old-password\",\"newPassword\":\"new-password\"}"))
			.andExpect(status().isOk());
		login("other", "other-password");
		login("owner", "new-password");
	}

	@Test
	void rejectsDeletedAndMissingMembers() throws Exception {
		db.update("UPDATE USERS SET DEL_YN='Y' WHERE USER_ID='owner'");
		for (String id : List.of("owner", "missing")) {
			mvc.perform(change("old-password", "new-password").cookie(access(id)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("USER4001"));
		}
	}

	@Test
	void rollsBackPasswordWhenRevocationFails() throws Exception {
		var session = command.login(new UserRequest.UserLogin("owner", "old-password"));
		doThrow(new org.springframework.dao.DataAccessResourceFailureException("test revocation failure"))
			.when(refreshTokens).revokeAllUserRefreshTokens("owner");
		try {
			assertThatThrownBy(() -> command.changePassword("owner",
				new UserRequest.PasswordChange("old-password", "new-password")))
				.isInstanceOf(org.springframework.dao.DataAccessResourceFailureException.class);
		} finally {
			reset(refreshTokens);
		}
		login("owner", "old-password");
		assertThat(command.refresh(session.getRefreshToken()).getAccessToken()).isNotBlank();
	}

	private MockHttpServletRequestBuilder change(String current, String next) {
		return patch("/api/v1/user/password").contentType(MediaType.APPLICATION_JSON)
			.content("{\"currentPassword\":\"" + current + "\",\"newPassword\":\"" + next + "\"}");
	}

	private Cookie access(String userId) {
		return new Cookie("access_token", jwt.generateToken(userId,
			List.of(new SimpleGrantedAuthority("ROLE_USER"))).getAccessToken());
	}

	@Test
	void concurrentRefreshCannotEscapePasswordChangeRevocation() throws Exception {
		var session = command.login(new UserRequest.UserLogin("owner", "old-password"));
		var observed = new java.util.concurrent.CountDownLatch(1);
		doAnswer(invocation -> {
			Object result = new org.mybatis.spring.SqlSessionTemplate(context.getBean(SqlSessionFactory.class))
				.getMapper(RefreshTokenDao.class).existsActive("owner", session.getRefreshJti());
			observed.countDown();
			return result;
		}).when(refreshTokens).existsActive("owner", session.getRefreshJti());
		var transaction = new org.springframework.transaction.support.TransactionTemplate(
			context.getBean(org.springframework.transaction.PlatformTransactionManager.class));
		try (var executor = java.util.concurrent.Executors.newSingleThreadExecutor()) {
			java.util.concurrent.Future<?> future = transaction.execute(status -> {
				command.changePassword("owner", new UserRequest.PasswordChange("old-password", "new-password"));
				var refresh = executor.submit(() -> command.refresh(session.getRefreshToken()));
				try {
					assertThat(observed.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException(e);
				}
				return refresh;
			});
			assertThatThrownBy(() -> future.get(10, java.util.concurrent.TimeUnit.SECONDS))
				.isInstanceOf(java.util.concurrent.ExecutionException.class)
				.hasCauseInstanceOf(RuntimeException.class);
		} finally {
			reset(refreshTokens);
		}
		login("owner", "new-password");
	}

	@Test
	void rejectsPasswordsBeyondBcryptByteLimit() throws Exception {
		mvc.perform(change("old-password", "가".repeat(25)).cookie(access("owner")))
			.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON400"));
		login("owner", "old-password");
	}

	private Cookie login(String userId, String password) throws Exception {
		var result = mvc.perform(post("/api/v1/user/login").contentType(MediaType.APPLICATION_JSON)
			.content("{\"loginId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
			.andExpect(status().isOk()).andReturn();
		return result.getResponse().getCookie("access_token");
	}

	@Configuration
	@EnableWebMvc
	@EnableTransactionManagement
	@MapperScan("com.project.scenepickbe.user.dao")
	@Import({UserController.class, UserCommandService.class, UserQueryService.class, SecurityConfig.class,
		ExceptionAdvice.class, JwtAuthenticationEntryPoint.class, CookieProvider.class})
	static class TestConfig {

		@Bean
		DataSource dataSource() {
			return new DriverManagerDataSource(ORACLE.getJdbcUrl(), ORACLE.getUsername(), ORACLE.getPassword());
		}

		@Bean(initMethod = "migrate")
		Flyway flyway(DataSource dataSource) {
			return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();
		}

		@Bean
		@DependsOn("flyway")
		SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
			var factory = new SqlSessionFactoryBean();
			factory.setDataSource(dataSource);
			factory.setTypeAliasesPackage("com.project.scenepickbe.user.vo");
			var resources = new PathMatchingResourcePatternResolver();
			factory.setMapperLocations(resources.getResource("classpath:mybatis/mapper/UserMapper.xml"),
				resources.getResource("classpath:mybatis/mapper/RefreshTokenMapper.xml"));
			var configuration = new org.apache.ibatis.session.Configuration();
			configuration.setMapUnderscoreToCamelCase(true);
			factory.setConfiguration(configuration);
			return factory.getObject();
		}

		@Bean
		DataSourceTransactionManager transactionManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}

		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		ModelMapper modelMapper() {
			return new ModelMapper();
		}

		@Bean
		JwtTokenProvider jwtTokenProvider() {
			return new JwtTokenProvider(Base64.getEncoder().encodeToString(
				"password-test-only-signing-key-not-production".getBytes(StandardCharsets.UTF_8)), 5, 1);
		}

		@Bean
		CustomerOauth2UserService oauth2UserService(UserDao dao) {
			return new CustomerOauth2UserService(dao);
		}

		@Bean
		OAuth2SuccessHandler oauth2SuccessHandler(JwtTokenProvider tokens, CookieProvider cookies) {
			return new OAuth2SuccessHandler(tokens, cookies);
		}

		@Bean
		ClientRegistrationRepository clientRegistrationRepository() {
			return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId("test")
				.clientId("test-client").clientSecret("test-only")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("http://localhost/login/oauth2/code/test")
				.authorizationUri("http://localhost/oauth/authorize").tokenUri("http://localhost/oauth/token")
				.userInfoUri("http://localhost/oauth/userinfo").userNameAttributeName("id").clientName("test").build());
		}
	}
}
