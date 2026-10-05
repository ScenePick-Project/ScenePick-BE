package com.project.scenepickbe.mypage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.project.scenepickbe.common.apiPayload.code.exception.ExceptionAdvice;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import com.project.scenepickbe.common.config.SecurityConfig;
import com.project.scenepickbe.common.jwt.*;
import com.project.scenepickbe.common.security.oauth.*;
import com.project.scenepickbe.user.dao.UserDao;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.*;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;

@SpringJUnitConfig(MyPageIntegrationTest.TestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = {"jwt.access-expiration-minutes=5", "jwt.refresh-expiration-days=1"})
@Testcontainers
class MyPageIntegrationTest {
	@Container static final OracleContainer ORACLE = new OracleContainer(DockerImageName.parse(
		"gvenzl/oracle-xe@sha256:c2682a4216b0fe65537912c4a049c7e5c15a85bb7c94bb8137d5f2d2eef60603"))
		.withUsername("mypage_test").withPassword("mypage_test_only").withStartupTimeout(Duration.ofMinutes(3));
	private static final AtomicLong IDS = new AtomicLong(20000);
	@Autowired WebApplicationContext context;
	@Autowired DataSource dataSource;
	@Autowired ObjectMapper json;
	@Autowired JwtTokenProvider tokens;
	@Autowired ProfileImageService images;
	private MockMvc mvc;
	private JdbcTemplate db;
	private String owner;
	private String other;
	private long contentId;

	@BeforeEach void setUp() {
		reset(images);
		mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
		db = new JdbcTemplate(dataSource);
		long id = IDS.incrementAndGet(); owner = "mypage-owner-" + id; other = "mypage-other-" + id;
		for (String user : List.of(owner, other)) db.update("INSERT INTO USERS(USER_ID,EMAIL,USERNAME,ROLE) VALUES(?,?,?,'USER')", user, user+"@example.test", user);
		db.update("INSERT INTO CONTENT(CONTENT_ID,TITLE) VALUES(?,?)",id,"테스트 작품"); contentId=id;
	}
	private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, String user) {
		return request.cookie(new Cookie("access_token", tokens.generateToken(user,List.of(new SimpleGrantedAuthority("ROLE_USER"))).getAccessToken()));
	}
	private long review(String user, String rating) throws Exception {
		String body = "{\"reviewBody\":\"리뷰\",\"isSpoiler\":false" + (rating==null?"":",\"rating\":"+rating)+"}";
		String response=mvc.perform(as(post("/api/v1/contents/{id}/reviews",contentId).contentType(MediaType.APPLICATION_JSON).content(body),user))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		return json.readTree(response).path("result").path("reviewId").asLong();
	}
	@Test void rejectsMissingInvalidAndDeletedUserAuthentication() throws Exception {
		for(String route:List.of("profile","reviews","following","followers","review-bookmarks","track-bookmarks","rating-statistics")) {
			mvc.perform(get("/api/v1/me/"+route)).andExpect(status().isUnauthorized());
			mvc.perform(get("/api/v1/me/"+route).cookie(new Cookie("access_token","invalid"))).andExpect(status().isUnauthorized());
		}
		db.update("UPDATE USERS SET DEL_YN='Y' WHERE USER_ID=?",other);
		mvc.perform(as(get("/api/v1/me/profile"),other)).andExpect(status().isForbidden());
	}
	@Test void profilePatchPreservesOmittedValuesAndAllowsExplicitClears() throws Exception {
		mvc.perform(as(patch("/api/v1/me/profile").contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"닉네임\",\"bio\":\"소개\"}"),owner)).andExpect(status().isOk());
		mvc.perform(as(patch("/api/v1/me/profile").contentType(MediaType.APPLICATION_JSON).content("{}"),owner)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/profile").param("userId",other),owner)).andExpect(status().isOk())
			.andExpect(jsonPath("$.result.userId").value(owner)).andExpect(jsonPath("$.result.nickname").value("닉네임"))
			.andExpect(jsonPath("$.result.bio").value("소개")).andExpect(jsonPath("$.result.followingCount").value(0));
		mvc.perform(as(patch("/api/v1/me/profile").contentType(MediaType.APPLICATION_JSON).content("{\"bio\":null,\"imageKey\":null}"),owner)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/profile"),owner)).andExpect(jsonPath("$.result.bio").doesNotExist());
		for(String body:List.of("{\"nickname\":null}","{\"nickname\":\" \"}", json.writeValueAsString(Map.of("bio","a".repeat(501)))))
			mvc.perform(as(patch("/api/v1/me/profile").contentType(MediaType.APPLICATION_JSON).content(body),owner)).andExpect(status().isBadRequest());
	}
	@Test void ratingsExcludeUnratedDeletedAndOtherUsersReviews() throws Exception {
		long rated=review(owner,"4.5"); review(owner,null); review(other,"1"); long deleted=review(owner,"5");
		mvc.perform(as(delete("/api/v1/reviews/{id}",deleted),owner)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/rating-statistics"),owner)).andExpect(status().isOk())
			.andExpect(jsonPath("$.result.totalCount").value(1)).andExpect(jsonPath("$.result.buckets.length()").value(10))
			.andExpect(jsonPath("$.result.buckets[8].count").value(1));
		mvc.perform(as(patch("/api/v1/me/reviews/{id}/rating",rated).contentType(MediaType.APPLICATION_JSON).content("{\"rating\":1.25}"),owner)).andExpect(status().isBadRequest());
		mvc.perform(as(patch("/api/v1/me/reviews/{id}/rating",rated).contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5}"),other)).andExpect(status().isNotFound());
		mvc.perform(as(patch("/api/v1/me/reviews/{id}/rating",rated).contentType(MediaType.APPLICATION_JSON).content("{\"rating\":null}"),owner)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/rating-statistics"),owner)).andExpect(jsonPath("$.result.totalCount").value(0));
		mvc.perform(as(get("/api/v1/reviews/{id}",rated),owner)).andExpect(status().isOk()).andExpect(jsonPath("$.result.rating").doesNotExist());
	}
	@Test void ownReviewCursorHasNoDuplicatesAtEqualTimestamps() throws Exception {
		long first=review(owner,"1"), second=review(owner,"2"), third=review(owner,"3"); review(other,"4");
		db.update("UPDATE REVIEW SET CREATED_AT=TIMESTAMP '2026-10-05 00:00:00' WHERE USER_ID=?",owner);
		String body=mvc.perform(as(get("/api/v1/me/reviews").param("size","2"),owner)).andExpect(status().isOk())
			.andExpect(jsonPath("$.result.items[0].reviewId").value(third)).andExpect(jsonPath("$.result.items[1].reviewId").value(second))
			.andExpect(jsonPath("$.result.hasNext").value(true)).andReturn().getResponse().getContentAsString();
		var cursor=json.readTree(body).path("result").path("nextCursor");
		mvc.perform(as(get("/api/v1/me/reviews").param("cursorCreatedAt",cursor.path("createdAt").asText()).param("cursorId",cursor.path("id").asText()),owner))
			.andExpect(status().isOk()).andExpect(jsonPath("$.result.items.length()").value(1)).andExpect(jsonPath("$.result.items[0].reviewId").value(first));
		mvc.perform(as(get("/api/v1/me/reviews").param("cursorId","1"),owner)).andExpect(status().isBadRequest());
		mvc.perform(as(get("/api/v1/me/reviews").param("size","0"),owner)).andExpect(status().isBadRequest());
	}
	@Test void followIsIdempotentPrivateAndExcludesDisabledUsers() throws Exception {
		for(int i=0;i<2;i++) mvc.perform(as(put("/api/v1/me/following/{id}",other),owner)).andExpect(status().isOk());
		mvc.perform(as(put("/api/v1/me/following/{id}",owner),owner)).andExpect(status().isBadRequest());
		mvc.perform(as(get("/api/v1/me/following"),owner)).andExpect(jsonPath("$.result.items.length()").value(1));
		mvc.perform(as(get("/api/v1/me/followers"),other)).andExpect(jsonPath("$.result.items[0].userId").value(owner));
		mvc.perform(as(get("/api/v1/me/profile"),owner)).andExpect(jsonPath("$.result.followingCount").value(1));
		db.update("UPDATE USERS SET BAN_YN='Y' WHERE USER_ID=?",other);
		mvc.perform(as(get("/api/v1/me/following"),owner)).andExpect(jsonPath("$.result.items").isEmpty());
		mvc.perform(as(get("/api/v1/me/profile"),owner)).andExpect(jsonPath("$.result.followingCount").value(0));
		mvc.perform(as(delete("/api/v1/me/following/{id}",other),owner)).andExpect(status().isOk());
	}
	@Test void bookmarksAreSeparatePrivateIdempotentAndRemovable() throws Exception {
		long id=review(other,"3.5");
		for(int i=0;i<2;i++) mvc.perform(as(put("/api/v1/me/review-bookmarks/{id}",id),owner)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/review-bookmarks"),owner)).andExpect(jsonPath("$.result.items.length()").value(1));
		mvc.perform(as(get("/api/v1/me/review-bookmarks"),other)).andExpect(jsonPath("$.result.items").isEmpty());
		mvc.perform(as(delete("/api/v1/me/review-bookmarks/{id}",id),other)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/review-bookmarks"),owner)).andExpect(jsonPath("$.result.items.length()").value(1));
		mvc.perform(as(delete("/api/v1/reviews/{id}",id),other)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/review-bookmarks"),owner)).andExpect(jsonPath("$.result.items").isEmpty());
		mvc.perform(as(put("/api/v1/me/review-bookmarks/{id}",id),owner)).andExpect(status().isBadRequest());
		mvc.perform(as(delete("/api/v1/me/review-bookmarks/{id}",id),owner)).andExpect(status().isOk());
	}
	@Test void tracksValidateInputAndKeepFirstSnapshotUntilRemoved() throws Exception {
		String body="{\"trackId\":\"track-1\",\"trackName\":\"곡명\",\"artist\":\"아티스트\"}";
		for(int i=0;i<2;i++) mvc.perform(as(post("/api/v1/me/track-bookmarks").contentType(MediaType.APPLICATION_JSON).content(body),owner)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/track-bookmarks"),owner)).andExpect(jsonPath("$.result.items.length()").value(1)).andExpect(jsonPath("$.result.items[0].trackName").value("곡명"));
		mvc.perform(as(get("/api/v1/me/track-bookmarks"),other)).andExpect(jsonPath("$.result.items").isEmpty());
		mvc.perform(as(post("/api/v1/me/track-bookmarks").contentType(MediaType.APPLICATION_JSON).content("{\"trackId\":\" \"}"),owner)).andExpect(status().isBadRequest());
		mvc.perform(as(delete("/api/v1/me/track-bookmarks/track-1"),owner)).andExpect(status().isOk());
		mvc.perform(as(get("/api/v1/me/track-bookmarks"),owner)).andExpect(jsonPath("$.result.items").isEmpty());
	}
	@Configuration
	@EnableWebMvc
	@EnableTransactionManagement
	@ComponentScan(basePackages = {"com.project.scenepickbe.mypage", "com.project.scenepickbe.review"}, excludeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = ProfileImageService.class))
	@MapperScan({"com.project.scenepickbe.mypage", "com.project.scenepickbe.review.dao"})
	@Import({ExceptionAdvice.class, SecurityConfig.class, JwtAuthenticationEntryPoint.class, CookieProvider.class})
	static class TestConfig implements WebMvcConfigurer {
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
			SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
			factory.setDataSource(dataSource);
			var resolver = new PathMatchingResourcePatternResolver();
			factory.setTypeAliasesPackage("com.project.scenepickbe.review.vo");
			factory.setMapperLocations(resolver.getResource("classpath:mybatis/mapper/MyPageMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/ReviewMapper.xml"),
				resolver.getResource("classpath:mybatis/mapper/ReviewLikeMapper.xml"));
			org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
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
			return new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		}

		@Bean ProfileImageService profileImageService() { return mock(ProfileImageService.class); }
		@Bean JwtTokenProvider jwtTokenProvider() {
			return new JwtTokenProvider(java.util.Base64.getEncoder().encodeToString(
				"mypage-test-only-signing-key-not-production".getBytes(java.nio.charset.StandardCharsets.UTF_8)), 5, 1);
		}
		@Bean CustomerOauth2UserService oauth2UserService() { return new CustomerOauth2UserService(mock(UserDao.class)); }
		@Bean OAuth2SuccessHandler oauth2SuccessHandler(JwtTokenProvider provider, CookieProvider cookies) {
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
		@Override
		public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
			resolvers.add(new AuthenticationPrincipalArgumentResolver());
		}

		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
		}
	}
}
