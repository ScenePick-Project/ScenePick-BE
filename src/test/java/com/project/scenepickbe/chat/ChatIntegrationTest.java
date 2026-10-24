package com.project.scenepickbe.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.project.scenepickbe.common.apiPayload.code.exception.ExceptionAdvice;
import com.project.scenepickbe.common.config.SecurityConfig;
import com.project.scenepickbe.common.jwt.CookieProvider;
import com.project.scenepickbe.common.jwt.JwtAuthenticationEntryPoint;
import com.project.scenepickbe.common.jwt.JwtTokenProvider;
import com.project.scenepickbe.common.security.oauth.CustomerOauth2UserService;
import com.project.scenepickbe.common.security.oauth.OAuth2SuccessHandler;
import com.project.scenepickbe.user.dao.UserDao;

@SpringBootTest(classes = ChatIntegrationTest.Config.class,
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = {"spring.config.location=optional:classpath:chat-test.yml",
		"jwt.access-expiration-minutes=5", "jwt.refresh-expiration-days=1",
		"spring.cloud.aws.region.static=ap-northeast-2",
		"spring.cloud.aws.s3.bucket=chat-test-only",
		"spring.cloud.aws.credentials.access-key=test-only",
		"spring.cloud.aws.credentials.secret-key=test-only"})
@Testcontainers
class ChatIntegrationTest {

	@Container
	static final OracleContainer ORACLE = new OracleContainer(DockerImageName.parse(
		"gvenzl/oracle-xe@sha256:c2682a4216b0fe65537912c4a049c7e5c15a85bb7c94bb8137d5f2d2eef60603"))
		.withUsername("chat_test").withPassword("chat_test_only").withStartupTimeout(Duration.ofMinutes(8));
	private static final AtomicLong IDS = new AtomicLong(70000);
	@Autowired TestRestTemplate http;
	@Autowired DataSource dataSource;
	@Autowired JwtTokenProvider tokens;
	@Autowired ObjectMapper json;
	@LocalServerPort int port;
	private JdbcTemplate db;
	private long contentId;
	private String userId;

	@BeforeEach
	void setUp() {
		db = new JdbcTemplate(dataSource);
		contentId = IDS.incrementAndGet();
		userId = "chat-user-" + contentId;
		db.update("INSERT INTO CONTENT(CONTENT_ID,TITLE) VALUES(?,?)", contentId, "채팅 테스트 작품");
		db.update("INSERT INTO USERS(USER_ID,EMAIL,USERNAME) VALUES(?,?,?)", userId,
			userId + "@example.test", "관객");
	}

	@Test
	void anonymousCanReadAnEmptyRoomButUnknownContentIsNotFound() {
		var response = http.getForEntity("/api/v1/contents/" + contentId + "/chat/messages", JsonNode.class);
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody().path("result").path("items").isEmpty()).isTrue();
		assertThat(response.getBody().path("result").path("hasNext").asBoolean()).isFalse();
		assertThat(http.getForEntity("/api/v1/contents/999999999/chat/messages", JsonNode.class)
			.getStatusCode().value()).isEqualTo(404);
	}

	@Test
	void authenticatedMessageReachesAnonymousReaderAndSurvivesReconnect() throws Exception {
		try (var reader = connect(null); var sender = connect(token(userId))) {
			reader.subscribe(topic(contentId));
			sender.subscribe(topic(contentId));
			sender.send("/app/chat/" + contentId + "/messages",
				"{\"body\":\"음악이 좋아요\",\"userId\":\"forged\"}");
			JsonNode received = json.readTree(reader.receive("MESSAGE").split("\n\n", 2)[1]);
			assertThat(received.path("userId").asText()).isEqualTo(userId);
			assertThat(received.path("nickname").asText()).isEqualTo("관객");
			assertThat(received.path("body").asText()).isEqualTo("음악이 좋아요");
			assertThat(received.path("messageId").isTextual()).isTrue();
			assertThat(json.readTree(sender.receive("MESSAGE").split("\n\n", 2)[1])
				.path("messageId")).isEqualTo(received.path("messageId"));
		}
		JsonNode items = http.getForObject("/api/v1/contents/" + contentId + "/chat/messages?afterId=0",
			JsonNode.class).path("result").path("items");
		assertThat(items.size()).isEqualTo(1);
		assertThat(items.get(0).path("body").asText()).isEqualTo("음악이 좋아요");
	}

	@Test
	void cursorPagesCoverEqualTimestampsWithoutDuplicatesAndRejectInvalidParameters() {
		db.update("""
			INSERT INTO CHAT_MESSAGE(CONTENT_ID, USER_ID, BODY, CREATED_AT)
			SELECT ?, ?, 'message-' || LEVEL, TIMESTAMP '2026-10-01 00:00:00'
			FROM DUAL CONNECT BY LEVEL <= 51
			""", contentId, userId);
		String path = "/api/v1/contents/" + contentId + "/chat/messages";
		JsonNode recent = http.getForObject(path, JsonNode.class).path("result");
		assertThat(recent.path("items").size()).isEqualTo(50);
		assertThat(recent.path("hasNext").asBoolean()).isTrue();
		JsonNode older = http.getForObject(path + "?beforeId=" + recent.path("nextCursor").asText(),
			JsonNode.class).path("result");
		assertThat(older.path("items").size()).isEqualTo(1);
		assertThat(older.path("hasNext").asBoolean()).isFalse();
		long first = Long.parseLong(older.path("items").get(0).path("messageId").asText());
		assertThat(Long.parseLong(recent.path("items").get(49).path("messageId").asText())).isGreaterThan(first);
		JsonNode forward = http.getForObject(path + "?afterId=" + first, JsonNode.class).path("result");
		assertThat(forward.path("items").size()).isEqualTo(50);
		assertThat(forward.path("hasNext").asBoolean()).isFalse();
		assertThat(forward.path("items").get(49).path("messageId"))
			.isEqualTo(recent.path("items").get(0).path("messageId"));
		for (String query : List.of("size=0", "size=51", "beforeId=0", "afterId=-1", "beforeId=1&afterId=1")) {
			assertThat(http.getForEntity(path + "?" + query, JsonNode.class).getStatusCode().value()).isEqualTo(400);
		}
	}

	@Test
	void anonymousCannotSendAndClientsCannotPublishToBrokerOrSubscribeWithWildcards() throws Exception {
		for (String destination : List.of("/app/chat/" + contentId + "/messages", topic(contentId))) {
			try (var peer = connect(null)) {
				peer.send(destination, "{\"body\":\"거절\"}");
				peer.receive("ERROR");
			}
		}
		try (var peer = connect(token(userId))) {
			peer.send(topic(contentId), "{\"body\":\"저장 우회\"}");
			peer.receive("ERROR");
		}
		for (String destination : List.of("/topic/contents/*/chat", "/queue/chat/errors",
			"/user/another/queue/chat/errors", "/topic/contents/999999999/chat")) {
			try (var peer = connect(null)) {
				peer.write("SUBSCRIBE\nid:bad\ndestination:" + destination + "\n\n");
				peer.receive("ERROR");
			}
		}
		assertThat(http.getForObject("/api/v1/contents/" + contentId + "/chat/messages", JsonNode.class)
			.path("result").path("items").isEmpty()).isTrue();
	}

	@Test
	void invalidBodiesAreRejectedButFiveHundredEmojiAreStored() throws Exception {
		try (var peer = connect(token(userId))) {
			peer.errors();
			peer.subscribe(topic(contentId));
			for (String body : List.of(" ", "가".repeat(501), "😀".repeat(501))) {
				peer.send("/app/chat/" + contentId + "/messages", json.writeValueAsString(java.util.Map.of("body", body)));
				assertThat(peer.receive("MESSAGE")).contains("COMMON400");
			}
			peer.send("/app/chat/" + contentId + "/messages", json.writeValueAsString(java.util.Map.of("body", "😀".repeat(500))));
			JsonNode message = json.readTree(peer.receive("MESSAGE").split("\n\n", 2)[1]);
			assertThat(message.path("body").asText()).isEqualTo("😀".repeat(500));
		}
	}

	@Test
	void disabledUsersCannotSendAndMessagesStayInTheirRoom() throws Exception {
		long second = IDS.incrementAndGet();
		db.update("INSERT INTO CONTENT(CONTENT_ID,TITLE) VALUES(?,?)", second, "다른 작품");
		try (var otherRoom = connect(null); var sender = connect(token(userId))) {
			otherRoom.subscribe(topic(second));
			sender.errors();
			sender.subscribe(topic(contentId));
			db.update("UPDATE USERS SET BAN_YN='Y' WHERE USER_ID=?", userId);
			sender.send("/app/chat/" + contentId + "/messages", "{\"body\":\"금지\"}");
			assertThat(sender.receive("MESSAGE")).contains("COMMON403");
			db.update("UPDATE USERS SET BAN_YN='N' WHERE USER_ID=?", userId);
			sender.send("/app/chat/" + contentId + "/messages", "{\"body\":\"이 작품에만\"}");
			assertThat(sender.receive("MESSAGE")).contains("이 작품에만");
			assertThat(otherRoom.frames.poll(300, TimeUnit.MILLISECONDS)).isNull();
			assertThat(http.getForObject("/api/v1/contents/" + second + "/chat/messages", JsonNode.class)
				.path("result").path("items").isEmpty()).isTrue();
		}
	}

	@Test
	void databaseFailureDoesNotBroadcastOrLeaveHistory() throws Exception {
		db.execute("ALTER TABLE CHAT_MESSAGE ADD CONSTRAINT CK_CHAT_TEST_FAILURE CHECK (BODY <> 'reject-test-only')");
		try (var observer = connect(null); var sender = connect(token(userId))) {
			observer.subscribe(topic(contentId));
			sender.errors();
			sender.subscribe(topic(contentId));
			sender.send("/app/chat/" + contentId + "/messages", "{\"body\":\"reject-test-only\"}");
			assertThat(sender.receive("MESSAGE")).contains("COMMON500").doesNotContain("ORA-");
			assertThat(observer.frames.poll(300, TimeUnit.MILLISECONDS)).isNull();
			assertThat(http.getForObject("/api/v1/contents/" + contentId + "/chat/messages", JsonNode.class)
				.path("result").path("items").isEmpty()).isTrue();
		} finally {
			db.execute("ALTER TABLE CHAT_MESSAGE DROP CONSTRAINT CK_CHAT_TEST_FAILURE");
		}
	}

	@Test
	void rateLimitAppliesAcrossConnectionsAndRooms() throws Exception {
		long second = IDS.incrementAndGet();
		db.update("INSERT INTO CONTENT(CONTENT_ID,TITLE) VALUES(?,?)", second, "다른 방");
		try (var first = connect(token(userId)); var other = connect(token(userId))) {
			first.errors();
			first.subscribe(topic(contentId));
			other.errors();
			other.subscribe(topic(second));
			first.send("/app/chat/" + contentId + "/messages", "{\"body\":\"첫 전송\"}");
			other.send("/app/chat/" + second + "/messages", "{\"body\":\"동시 전송\"}");
			String left = first.receive("MESSAGE");
			String right = other.receive("MESSAGE");
			assertThat(List.of(left.contains("CHAT4291"), right.contains("CHAT4291")))
				.containsExactlyInAnyOrder(true, false);
		}
	}

	@Test
	void concurrentSendersReceiveCommittedMessagesInIdOrder() throws Exception {
		String secondUser = userId + "-2";
		db.update("INSERT INTO USERS(USER_ID,EMAIL,USERNAME) VALUES(?,?,?)", secondUser,
			secondUser + "@example.test", "두번째 관객");
		try (var first = connect(token(userId)); var second = connect(token(secondUser));
			var observer = connect(null)) {
			observer.subscribe(topic(contentId));
			first.send("/app/chat/" + contentId + "/messages", "{\"body\":\"하나\"}");
			second.send("/app/chat/" + contentId + "/messages", "{\"body\":\"둘\"}");
			JsonNode a = json.readTree(observer.receive("MESSAGE").split("\n\n", 2)[1]);
			JsonNode b = json.readTree(observer.receive("MESSAGE").split("\n\n", 2)[1]);
			assertThat(Long.parseLong(b.path("messageId").asText()))
				.isGreaterThan(Long.parseLong(a.path("messageId").asText()));
			JsonNode replay = http.getForObject("/api/v1/contents/" + contentId + "/chat/messages?afterId="
				+ a.path("messageId").asText(), JsonNode.class).path("result").path("items");
			assertThat(replay.size()).isEqualTo(1);
			assertThat(replay.get(0).path("messageId")).isEqualTo(b.path("messageId"));
		}
	}

	@Test
	void tokenExpiryAfterConnectionPreventsAnotherSend() throws Exception {
		var expires = java.time.Instant.now().plusSeconds(10);
		String expiring = io.jsonwebtoken.Jwts.builder().subject(userId).claim("type", "access")
			.claim("auth", "ROLE_USER").expiration(java.util.Date.from(expires))
			.signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
				"chat-test-only-signing-key-not-production".getBytes(java.nio.charset.StandardCharsets.UTF_8))).compact();
		try (var sender = connect(expiring)) {
			sender.subscribe(topic(contentId));
			sender.send("/app/chat/" + contentId + "/messages", "{\"body\":\"만료 전\"}");
			assertThat(sender.receive("MESSAGE")).contains("만료 전");
			long remaining = java.time.Duration.between(java.time.Instant.now(), expires.plusMillis(200)).toMillis();
			if (remaining > 0) {
				Thread.sleep(remaining);
			}
			sender.send("/app/chat/" + contentId + "/messages", "{\"body\":\"만료 후\"}");
			assertThat(sender.receive("ERROR")).contains("JWT4011");
		}
	}

	private String token(String user) {
		return tokens.generateToken(user, List.of(new SimpleGrantedAuthority("ROLE_USER"))).getAccessToken();
	}

	private String topic(long content) {
		return "/topic/contents/" + content + "/chat";
	}

	private Peer connect(String token) throws Exception {
		return new Peer(token);
	}

	private class Peer implements WebSocket.Listener, AutoCloseable {
		private final HttpClient client = HttpClient.newHttpClient();
		private final BlockingQueue<String> frames = new LinkedBlockingQueue<>();
		private final StringBuilder pending = new StringBuilder();
		private final WebSocket socket;
		private int subscriptions;

		Peer(String token) throws Exception {
			var builder = client.newWebSocketBuilder().header("Origin", "http://localhost:5173")
				.subprotocols("v12.stomp");
			if (token != null) {
				builder.header("Cookie", "access_token=" + token);
			}
			socket = builder.buildAsync(URI.create("ws://localhost:" + port + "/ws/chat"), this)
				.get(15, TimeUnit.SECONDS);
			write("CONNECT\naccept-version:1.2\nheart-beat:0,0\n\n");
			receive("CONNECTED");
		}

		void subscribe(String destination) throws Exception {
			String id = "sub-" + ++subscriptions;
			write("SUBSCRIBE\nid:" + id + "\ndestination:" + destination + "\nreceipt:" + id + "\n\n");
			receive("RECEIPT");
		}

		void errors() {
			write("SUBSCRIBE\nid:errors\ndestination:/user/queue/chat/errors\n\n");
		}

		void send(String destination, String body) {
			write("SEND\ndestination:" + destination + "\ncontent-type:application/json\n\n" + body);
		}

		void write(String frame) {
			socket.sendText(frame + '\0', true).join();
		}

		String receive(String command) throws Exception {
			String frame = frames.poll(15, TimeUnit.SECONDS);
			assertThat(frame).as("Expected STOMP %s", command).isNotNull().startsWith(command + "\n");
			return frame;
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			pending.append(data);
			int end;
			while ((end = pending.indexOf("\0")) >= 0) {
				frames.add(pending.substring(0, end).stripLeading());
				pending.delete(0, end + 1);
			}
			webSocket.request(1);
			return null;
		}

		@Override
		public void close() {
			socket.abort();
			client.shutdownNow();
		}
	}

	@Configuration
	@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
		FlywayAutoConfiguration.class})
	@EnableTransactionManagement
	@ComponentScan(basePackages = "com.project.scenepickbe.chat")
	@MapperScan(basePackages = "com.project.scenepickbe.chat", annotationClass = Mapper.class)
	@Import({SecurityConfig.class, ExceptionAdvice.class, JwtAuthenticationEntryPoint.class, CookieProvider.class,
		com.project.scenepickbe.infrastructure.s3.service.S3QueryService.class})
	static class Config {
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
			factory.setMapperLocations(new PathMatchingResourcePatternResolver()
				.getResources("classpath:mybatis/mapper/ChatMapper.xml"));
			return factory.getObject();
		}

		@Bean
		DataSourceTransactionManager transactionManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}

		@Bean
		JwtTokenProvider jwtTokenProvider() {
			return new JwtTokenProvider(Base64.getEncoder().encodeToString(
				"chat-test-only-signing-key-not-production".getBytes(java.nio.charset.StandardCharsets.UTF_8)), 5, 1);
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
				.clientId("test").clientSecret("test-only")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("http://localhost/login/oauth2/code/test")
				.authorizationUri("http://localhost/oauth/authorize").tokenUri("http://localhost/oauth/token")
				.userInfoUri("http://localhost/oauth/userinfo").userNameAttributeName("id").build());
		}
	}
}
