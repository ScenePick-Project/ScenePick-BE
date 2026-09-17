package com.project.scenepickbe.moment;

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

@SpringJUnitConfig(MomentIntegrationTest.TestConfig.class)
@WebAppConfiguration
@Testcontainers
class MomentIntegrationTest {
	@Container
	static final OracleContainer ORACLE = new OracleContainer(DockerImageName.parse(
		"gvenzl/oracle-xe@sha256:c2682a4216b0fe65537912c4a049c7e5c15a85bb7c94bb8137d5f2d2eef60603"))
		.withUsername("moment_test")
		.withPassword("moment_test_only")
		.withStartupTimeout(Duration.ofMinutes(3));

	private static final AtomicLong FIXTURE_IDS = new AtomicLong(1000);
	private static final String VIDEO_ID = "abcdefghijk";

	@Autowired
	private WebApplicationContext context;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mvc;
	private long contentId;
	private String ownerId;
	private String otherId;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
		contentId = FIXTURE_IDS.incrementAndGet();
		ownerId = "moment-owner-" + contentId;
		otherId = "moment-other-" + contentId;
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		jdbc.update("INSERT INTO CONTENT (CONTENT_ID, TITLE) VALUES (?, ?)", contentId, "모먼트 테스트 작품");
		for (String userId : List.of(ownerId, otherId)) {
			jdbc.update("INSERT INTO USERS (USER_ID, EMAIL, USERNAME) VALUES (?, ?, ?)",
				userId, userId + "@example.test", userId);
		}
		asUser(ownerId);
	}

	@AfterEach
	void clearAuthentication() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void createsMomentWithoutReview() throws Exception {
		long momentId = createMoment(12, 18, "색감이 인상적인 장면");
		assertThat(momentId).isPositive();
	}

	@Test
	void retrievesStoredVideoRangeAndMemo() throws Exception {
		long momentId = createMoment(12, 18, "색감이 인상적인 장면");
		mvc.perform(get("/api/v1/moments/{momentId}", momentId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.momentId").value(momentId))
			.andExpect(jsonPath("$.result.contentId").value(contentId))
			.andExpect(jsonPath("$.result.youtubeId").value(VIDEO_ID))
			.andExpect(jsonPath("$.result.startTime").value(12))
			.andExpect(jsonPath("$.result.endTime").value(18))
			.andExpect(jsonPath("$.result.memo").value("색감이 인상적인 장면"))
			.andExpect(jsonPath("$.result.createdAt").isString());
	}

	@Test
	void listsOnlyOwnMomentsWithStableCursor() throws Exception {
		long firstId = createMoment(12, 18, "첫 장면");
		long secondId = createMoment(40, 55, "두 번째 장면");
		long thirdId = createMoment(60, 70, "세 번째 장면");
		new JdbcTemplate(dataSource).update(
			"UPDATE MOMENT SET CREATED_AT = TIMESTAMP '2026-09-08 12:00:00' WHERE USER_ID = ?", ownerId);
		asUser(otherId);
		createMoment(80, 90, "다른 사용자의 비공개 메모");
		asUser(ownerId);

		String firstPage = mvc.perform(get("/api/v1/me/moments")
				.param("contentId", String.valueOf(contentId)).param("youtubeId", VIDEO_ID).param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.momentList.length()").value(2))
			.andExpect(jsonPath("$.result.momentList[0].momentId").value(thirdId))
			.andExpect(jsonPath("$.result.momentList[1].momentId").value(secondId))
			.andExpect(jsonPath("$.result.hasNext").value(true))
			.andReturn().getResponse().getContentAsString();
		var cursor = objectMapper.readTree(firstPage).path("result").path("nextCursor");
		mvc.perform(get("/api/v1/me/moments")
				.param("contentId", String.valueOf(contentId)).param("youtubeId", VIDEO_ID).param("size", "2")
				.param("cursorCreatedAt", cursor.path("createdAt").asText())
				.param("cursorMomentId", cursor.path("momentId").asText()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.momentList.length()").value(1))
			.andExpect(jsonPath("$.result.momentList[0].momentId").value(firstId))
			.andExpect(jsonPath("$.result.hasNext").value(false))
			.andExpect(jsonPath("$.result.nextCursor").isEmpty());
		mvc.perform(get("/api/v1/me/moments").param("youtubeId", "zyxwvutsrqp"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.momentList").isEmpty());
	}

	@org.junit.jupiter.params.ParameterizedTest
	@org.junit.jupiter.params.provider.ValueSource(strings = {
		"{}",
		"{\"youtubeId\":null,\"startTime\":12,\"endTime\":18}",
		"{\"youtubeId\":\"bad\",\"startTime\":12,\"endTime\":18}",
		"{\"youtubeId\":\"abc!efghijk\",\"startTime\":12,\"endTime\":18}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":-1,\"endTime\":18}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":12,\"endTime\":12}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":18,\"endTime\":12}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":null,\"endTime\":18}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":12,\"endTime\":null}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":12.5,\"endTime\":18}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":\"12\",\"endTime\":18}",
		"{\"youtubeId\":\"abcdefghijk\",\"startTime\":12,\"endTime\":2147483648}",
		"{"
	})
	void rejectsInvalidCreateRequests(String body) throws Exception {
		mvc.perform(post("/api/v1/contents/{contentId}/moments", contentId)
				.contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false));
	}

	@Test
	void rejectsMissingContentBeforeSaving() throws Exception {
		mvc.perform(post("/api/v1/contents/{contentId}/moments", Long.MAX_VALUE)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"youtubeId\":\"abcdefghijk\",\"startTime\":12,\"endTime\":18}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("CONTENT4001"));
	}

	@Test
	void patchesOnlyProvidedFieldsAndClearsMemo() throws Exception {
		long momentId = createMoment(12, 18, "처음 메모");
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"startTime\":14}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.startTime").value(14))
			.andExpect(jsonPath("$.result.endTime").value(18))
			.andExpect(jsonPath("$.result.memo").value("처음 메모"))
			.andExpect(jsonPath("$.result.updatedAt").isString());
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"memo\":\"\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.memo").isEmpty());
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"memo\":\"새 메모\",\"endTime\":20}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.memo").value("새 메모"))
			.andExpect(jsonPath("$.result.startTime").value(14))
			.andExpect(jsonPath("$.result.endTime").value(20));
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"memo\":null}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.memo").isEmpty());
		mvc.perform(get("/api/v1/moments/{momentId}", momentId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.startTime").value(14))
			.andExpect(jsonPath("$.result.endTime").value(20))
			.andExpect(jsonPath("$.result.memo").isEmpty());
	}

	@org.junit.jupiter.params.ParameterizedTest
	@org.junit.jupiter.params.provider.ValueSource(strings = {
		"{\"startTime\":18}", "{\"endTime\":12}", "{\"startTime\":-1}",
		"{\"startTime\":null}", "{\"endTime\":null}", "{\"startTime\":12.5}",
		"{\"endTime\":\"20\"}", "{\"endTime\":2147483648}", "{"
	})
	void rejectsInvalidPatchWithoutChangingMoment(String body) throws Exception {
		long momentId = createMoment(12, 18, "유지할 메모");
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.isSuccess").value(false));
		mvc.perform(get("/api/v1/moments/{momentId}", momentId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.startTime").value(12))
			.andExpect(jsonPath("$.result.endTime").value(18))
			.andExpect(jsonPath("$.result.memo").value("유지할 메모"));
	}

	@Test
	void deletesOwnMomentAndExcludesItFromReads() throws Exception {
		long momentId = createMoment(12, 18, "삭제할 메모");
		mvc.perform(delete("/api/v1/moments/{momentId}", momentId)).andExpect(status().isOk());
		mvc.perform(get("/api/v1/moments/{momentId}", momentId)).andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/me/moments"))
			.andExpect(status().isOk()).andExpect(jsonPath("$.result.momentList").isEmpty());
		mvc.perform(delete("/api/v1/moments/{momentId}", momentId)).andExpect(status().isNotFound());
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"memo\":\"복구 시도\"}"))
			.andExpect(status().isNotFound());
	}

	@Test
	void hidesOtherUsersMomentAndRejectsTheirChanges() throws Exception {
		long momentId = createMoment(12, 18, "나만 볼 메모");
		asUser(otherId);
		mvc.perform(get("/api/v1/moments/{momentId}", momentId)).andExpect(status().isNotFound());
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"memo\":\"변경 시도\"}"))
			.andExpect(status().isNotFound());
		mvc.perform(delete("/api/v1/moments/{momentId}", momentId)).andExpect(status().isNotFound());
		mvc.perform(get("/api/v1/me/moments"))
			.andExpect(status().isOk()).andExpect(jsonPath("$.result.momentList").isEmpty());
		asUser(ownerId);
		mvc.perform(get("/api/v1/moments/{momentId}", momentId))
			.andExpect(status().isOk()).andExpect(jsonPath("$.result.memo").value("나만 볼 메모"));
	}

	@Test
	void rejectsAnonymousAccessToEveryMomentEndpoint() throws Exception {
		long momentId = createMoment(12, 18, "비공개");
		SecurityContextHolder.clearContext();
		mvc.perform(post("/api/v1/contents/{contentId}/moments", contentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"youtubeId\":\"abcdefghijk\",\"startTime\":12,\"endTime\":18}"))
			.andExpect(status().isUnauthorized());
		mvc.perform(get("/api/v1/moments/{momentId}", momentId)).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/v1/me/moments")).andExpect(status().isUnauthorized());
		mvc.perform(patch("/api/v1/moments/{momentId}", momentId)
				.contentType(MediaType.APPLICATION_JSON).content("{\"memo\":\"변경\"}"))
			.andExpect(status().isUnauthorized());
		mvc.perform(delete("/api/v1/moments/{momentId}", momentId)).andExpect(status().isUnauthorized());
	}

	@Test
	void keepsMomentAfterDeletingReview() throws Exception {
		String createdReview = mvc.perform(post("/api/v1/contents/{contentId}/reviews", contentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"reviewBody\":\"삭제할 리뷰\",\"isSpoiler\":false,"
					+ "\"youtubeId\":\"abcdefghijk\",\"startTime\":12,\"endTime\":18}"))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		long reviewId = objectMapper.readTree(createdReview).path("result").path("reviewId").asLong();
		long momentId = createMoment(12, 18, "따로 보관한 장면");
		mvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId)).andExpect(status().isOk());
		mvc.perform(get("/api/v1/moments/{momentId}", momentId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.youtubeId").value(VIDEO_ID))
			.andExpect(jsonPath("$.result.memo").value("따로 보관한 장면"));
	}

	@Test
	void allowsDuplicateSegmentsAndFiltersByContent() throws Exception {
		long first = createMoment(0, 5, "");
		long second = createMoment(0, 5, "");
		assertThat(second).isNotEqualTo(first);
		mvc.perform(get("/api/v1/me/moments").param("contentId", String.valueOf(contentId)))
			.andExpect(status().isOk()).andExpect(jsonPath("$.result.momentList.length()").value(2));
		mvc.perform(get("/api/v1/me/moments").param("contentId", String.valueOf(Long.MAX_VALUE)))
			.andExpect(status().isOk()).andExpect(jsonPath("$.result.momentList").isEmpty());
	}

	@Test
	void rejectsInvalidCursorAndQueryInputs() throws Exception {
		for (Map.Entry<String, String> query : Map.of(
			"cursorMomentId", "1", "cursorCreatedAt", "2026-09-08T12:00:00",
			"size", "0", "contentId", "-1", "youtubeId", "bad").entrySet()) {
			mvc.perform(get("/api/v1/me/moments").param(query.getKey(), query.getValue()))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.isSuccess").value(false));
		}
		mvc.perform(get("/api/v1/moments/not-an-id"))
			.andExpect(status().isBadRequest()).andExpect(jsonPath("$.isSuccess").value(false));
	}

	private long createMoment(int startTime, int endTime, String memo) throws Exception {
		String response = mvc.perform(post("/api/v1/contents/{contentId}/moments", contentId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"youtubeId", VIDEO_ID, "startTime", startTime, "endTime", endTime, "memo", memo))))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).path("result").path("momentId").asLong();
	}

	private void asUser(String userId) {
		User principal = new User(userId, "", List.of());
		SecurityContextHolder.getContext().setAuthentication(
			UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
	}

	@Configuration
	@EnableWebMvc
	@EnableTransactionManagement
	@ComponentScan({"com.project.scenepickbe.moment", "com.project.scenepickbe.review"})
	@MapperScan({"com.project.scenepickbe.moment.dao", "com.project.scenepickbe.review.dao"})
	@Import(ExceptionAdvice.class)
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
			factory.setMapperLocations(resolver.getResource("classpath:mybatis/mapper/MomentMapper.xml"),
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
