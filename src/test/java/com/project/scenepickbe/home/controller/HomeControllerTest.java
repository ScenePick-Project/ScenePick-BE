package com.project.scenepickbe.home.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.scenepickbe.admin.controller.ContentAdminController;
import com.project.scenepickbe.common.apiPayload.code.exception.ExceptionAdvice;
import com.project.scenepickbe.common.config.SecurityConfig;
import com.project.scenepickbe.common.jwt.CookieProvider;
import com.project.scenepickbe.common.jwt.JwtAuthenticationEntryPoint;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.security.oauth.CustomerOauth2UserService;
import com.project.scenepickbe.common.security.oauth.OAuth2SuccessHandler;
import com.project.scenepickbe.content.controller.ContentController;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.service.ContentCommandService;
import com.project.scenepickbe.content.service.ContentQueryService;
import com.project.scenepickbe.content.service.RandomContentImportService;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.home.service.HomeQueryService;
import com.project.scenepickbe.review.controller.ReviewController;
import com.project.scenepickbe.review.controller.ReviewLikeController;
import com.project.scenepickbe.review.service.ReviewCommandService;
import com.project.scenepickbe.review.service.ReviewLikeCommandService;
import com.project.scenepickbe.review.service.ReviewQueryService;
import com.project.scenepickbe.user.dao.UserDao;

import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;

@SpringJUnitConfig(HomeControllerTest.TestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = {"jwt.access-expiration-minutes=5", "jwt.refresh-expiration-days=1"})
class HomeControllerTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private ContentDao contentDao;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private ReviewQueryService reviewQueryService;

	@Autowired
	private RandomContentImportService randomContentImportService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		reset(contentDao, reviewQueryService, randomContentImportService);
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.addFilters(context.getBean("springSecurityFilterChain", Filter.class))
			.build();
	}

	@Test
	void authenticatedRequestReturnsEmptyListInSuccessEnvelope() throws Exception {
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/home/recommendations").cookie(accessCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.code").value("COMMON200"))
			.andExpect(jsonPath("$.message").value("성공입니다."))
			.andExpect(jsonPath("$.result.recommendationType").value("RANDOM"))
			.andExpect(jsonPath("$.result.contentList").isEmpty());
	}

	@Test
	void unauthenticatedRequestReturnsRecommendations() throws Exception {
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/home/recommendations"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.result.recommendationType").value("RANDOM"))
			.andExpect(jsonPath("$.result.contentList").isEmpty());
	}

	@Test
	void invalidAccessCookieDoesNotBlockPublicRecommendations() throws Exception {
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/home/recommendations").cookie(new Cookie("access_token", "invalid")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true));
	}

	@Test
	void authenticatedRequestReturnsStoredSummaryFields() throws Exception {
		ContentVo content = new ContentVo();
		content.setContentId(17L);
		content.setTitle("추천 드라마");
		content.setPosterImageUrl("https://example.com/tv.jpg");
		content.setContentType(ContentType.TV);
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of(content));

		mockMvc.perform(get("/api/v1/home/recommendations").cookie(accessCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.contentList.length()").value(1))
			.andExpect(jsonPath("$.result.contentList[0].contentId").value(17))
			.andExpect(jsonPath("$.result.contentList[0].title").value("추천 드라마"))
			.andExpect(jsonPath("$.result.contentList[0].posterImageUrl").value("https://example.com/tv.jpg"))
			.andExpect(jsonPath("$.result.contentList[0].contentType").value("TV"));
	}

	@Test
	void databaseFailureIsNotReportedAsAnEmptySuccess() throws Exception {
		when(contentDao.selectRandomContentsForHome())
			.thenThrow(new DataAccessResourceFailureException("test database unavailable"));

		mockMvc.perform(get("/api/v1/home/recommendations").cookie(accessCookie()))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.isSuccess").value(false))
			.andExpect(jsonPath("$.code").value("COMMON500"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"valid", "expired", "invalid"})
	void publicRecommendationsIgnoreCookieAndBearerCredentials(String credential) throws Exception {
		String token = switch (credential) {
			case "valid" -> accessCookie().getValue();
			case "expired" -> new JwtTokenProvider(testSecret(), -1, 1)
				.generateToken("home-test-user", List.of(new SimpleGrantedAuthority("ROLE_USER")))
				.getAccessToken();
			default -> "invalid";
		};
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/home/recommendations").cookie(new Cookie("access_token", token)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.recommendationType").value("RANDOM"));
		mockMvc.perform(get("/api/v1/home/recommendations").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.contentList").isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/api/v1/home/recommendations/extra",
		"/api/v1/home/other", "/api/v1/home/recommendations/"})
	void otherPathsRemainProtected(String path) throws Exception {
		mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
		mockMvc.perform(get(path).cookie(new Cookie("access_token", "invalid")))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(get(path).header("Authorization", "Bearer invalid"))
			.andExpect(status().isUnauthorized());
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"/api/v1/contents/17",
		"/api/v1/contents/17/credits",
		"/api/v1/contents/17/seasons",
		"/api/v1/contents/17/seasons/1/episodes",
		"/api/v1/contents/17/seasons/1/credits",
		"/api/v1/contents/17/reviews",
		"/api/v1/reviews/1"
	})
	void publicContentAndReviewReadsIgnoreInvalidCredentials(String path) throws Exception {
		mockMvc.perform(get(path).cookie(new Cookie("access_token", "invalid")))
			.andExpect(status().isOk());
		mockMvc.perform(get(path).header("Authorization", "Bearer invalid"))
			.andExpect(status().isOk());
	}

	@Test
	void authenticatedReviewReadsRetainCurrentUser() throws Exception {
		mockMvc.perform(get("/api/v1/reviews/1").cookie(accessCookie()))
			.andExpect(status().isOk());

		verify(reviewQueryService).getReview(1L, "home-test-user");
	}

	@Test
	void reviewWritesRemainProtected() throws Exception {
		mockMvc.perform(post("/api/v1/contents/17/reviews"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(delete("/api/v1/reviews/1"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/v1/reviews/1/like"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void randomTmdbImportRemainsProtectedAndAcceptsAuthenticatedRequests() throws Exception {
		ContentImportResponse.Batch result = new ContentImportResponse.Batch(1, 1, 0,
			List.of(new ContentImportResponse.Result(
				new ContentImportResponse.Import(17L, 100L, ContentType.MOVIE, true), "가져왔습니다.")));
		when(randomContentImportService.importRandomPopularContents(1)).thenReturn(result);

		mockMvc.perform(post("/api/v1/admin/contents/import/tmdb/random").param("count", "1"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/v1/admin/contents/import/tmdb/random").param("count", "1")
			.cookie(accessCookie()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.requestedCount").value(1))
			.andExpect(jsonPath("$.result.processedCount").value(1));
		mockMvc.perform(post("/api/v1/admin/contents/import/tmdb/random").param("count", "21")
			.cookie(accessCookie()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("COMMON400"));

		verify(randomContentImportService).importRandomPopularContents(1);
	}

	@ParameterizedTest
	@ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"})
	void otherMethodsRemainProtected(String method) throws Exception {
		mockMvc.perform(request(HttpMethod.valueOf(method), "/api/v1/home/recommendations"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(request(HttpMethod.valueOf(method), "/api/v1/home/recommendations")
			.cookie(new Cookie("access_token", "invalid")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void publicRecommendationsWorkUnderAContextPath() throws Exception {
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of());

		mockMvc.perform(get("/scene/api/v1/home/recommendations").contextPath("/scene")
			.cookie(new Cookie("access_token", "invalid")))
			.andExpect(status().isOk());
	}

	private static String testSecret() {
		return Base64.getEncoder().encodeToString(
			"home-test-only-signing-key-not-for-production".getBytes(StandardCharsets.UTF_8));
	}

	private Cookie accessCookie() {
		String token = jwtTokenProvider.generateToken("home-test-user",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))).getAccessToken();
		return new Cookie("access_token", token);
	}

	@Configuration
	@EnableWebMvc
	@Import({HomeController.class, HomeQueryService.class, ContentController.class, ContentAdminController.class,
		ReviewController.class,
		ReviewLikeController.class, SecurityConfig.class, ExceptionAdvice.class,
		JwtAuthenticationEntryPoint.class, CookieProvider.class})
	static class TestConfig {

		@Bean
		ContentDao contentDao() {
			return mock(ContentDao.class);
		}

		@Bean
		ContentQueryService contentQueryService() {
			return mock(ContentQueryService.class);
		}

		@Bean
		ContentCommandService contentCommandService() {
			return mock(ContentCommandService.class);
		}

		@Bean
		RandomContentImportService randomContentImportService() {
			return mock(RandomContentImportService.class);
		}

		@Bean
		ReviewCommandService reviewCommandService() {
			return mock(ReviewCommandService.class);
		}

		@Bean
		ReviewQueryService reviewQueryService() {
			return mock(ReviewQueryService.class);
		}

		@Bean
		ReviewLikeCommandService reviewLikeCommandService() {
			return mock(ReviewLikeCommandService.class);
		}

		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}

		@Bean
		JwtTokenProvider jwtTokenProvider() {
			String secret = testSecret();
			return new JwtTokenProvider(secret, 5, 1);
		}

		@Bean
		CustomerOauth2UserService oauth2UserService() {
			return new CustomerOauth2UserService(mock(UserDao.class));
		}

		@Bean
		OAuth2SuccessHandler oauth2SuccessHandler(JwtTokenProvider provider, CookieProvider cookies) {
			return new OAuth2SuccessHandler(provider, cookies);
		}

		@Bean
		ClientRegistrationRepository clientRegistrationRepository() {
			return new InMemoryClientRegistrationRepository(ClientRegistration.withRegistrationId("test")
				.clientId("test-client")
				.clientSecret("test-only")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("http://localhost/login/oauth2/code/test")
				.authorizationUri("http://localhost/oauth/authorize")
				.tokenUri("http://localhost/oauth/token")
				.userInfoUri("http://localhost/oauth/userinfo")
				.userNameAttributeName("id")
				.clientName("test")
				.build());
		}
	}
}
