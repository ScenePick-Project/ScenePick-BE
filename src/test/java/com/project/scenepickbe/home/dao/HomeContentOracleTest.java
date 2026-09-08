package com.project.scenepickbe.home.dao;

import static org.assertj.core.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.vo.ContentVo;

@EnabledIfEnvironmentVariable(named = "SCENEPICK_TEST_ORACLE_URL", matches = ".+")
class HomeContentOracleTest {

	private static UnpooledDataSource dataSource;
	private static SqlSessionFactory sqlSessionFactory;
	private static boolean createdTable;

	@BeforeAll
	static void setUpDatabase() throws Exception {
		String url = System.getenv("SCENEPICK_TEST_ORACLE_URL");
		String user = System.getenv("SCENEPICK_TEST_ORACLE_USER");
		String password = System.getenv("SCENEPICK_TEST_ORACLE_PASSWORD");
		// 전용 로컬 테스트 스키마만 허용하며 기존 테이블을 덮어쓰지 않는다.
		assertThat(url).matches("jdbc:oracle:thin:@//(127\\.0\\.0\\.1|localhost):[0-9]+/XEPDB1");
		assertThat(user).isEqualTo("HOME_TEST");
		assertThat(password).isNotBlank();
		dataSource = new UnpooledDataSource("oracle.jdbc.OracleDriver", url, user, password);

		// 변경 불가인 V1 migration의 첫 CONTENT DDL을 그대로 사용한다.
		String migration;
		try (var input = Resources.getResourceAsStream("db/migration/V1__Initial_schema.sql")) {
			migration = new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
		try (Connection connection = dataSource.getConnection();
			var statement = connection.createStatement()) {
			statement.execute(migration.substring(0, migration.indexOf(';')));
			createdTable = true;
		}

		Configuration configuration = new Configuration(
			new Environment("home-oracle-test", new JdbcTransactionFactory(), dataSource));
		configuration.getTypeAliasRegistry().registerAliases("com.project.scenepickbe.content.vo");
		String mapper = "mybatis/mapper/ContentMapper.xml";
		try (InputStream input = Resources.getResourceAsStream(mapper)) {
			new XMLMapperBuilder(input, configuration, mapper, configuration.getSqlFragments()).parse();
		}
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
	}

	@AfterAll
	static void tearDownDatabase() throws SQLException {
		if (createdTable) {
			try (Connection connection = dataSource.getConnection();
				var statement = connection.createStatement()) {
				statement.execute("DROP TABLE CONTENT PURGE");
			}
		}
	}

	@ParameterizedTest
	@CsvSource({"0, 0", "3, 3", "15, 10"})
	void selectsOnlyEligibleDistinctSummariesWithinLimit(int candidateCount, int expectedCount) throws Exception {
		try (SqlSession session = sqlSessionFactory.openSession()) {
			try {
				Connection connection = session.getConnection();
				List<Tuple> expectedSummaries = new ArrayList<>();
				for (int id = 1; id <= candidateCount; id++) {
					ContentType type = id % 2 == 0 ? ContentType.TV : ContentType.MOVIE;
					String title = "작품 " + id;
					String poster = "https://example.com/" + id + ".jpg";
					insertContent(connection, id, type.name(), title, poster);
					expectedSummaries.add(tuple((long)id, title, poster, type));
				}
				insertContent(connection, 1001, "MOVIE", "포스터 없음", null);
				insertContent(connection, 1002, "TV", "빈 포스터", "");
				insertContent(connection, 1003, "MOVIE", "공백 포스터", "   ");
				insertContent(connection, 1004, "TV", "줄바꿈 포스터", "\t\r\n");
				insertContent(connection, 1005, "OTHER", "다른 유형", "https://example.com/other.jpg");

				List<ContentVo> result = session.getMapper(ContentDao.class).selectRandomContentsForHome();

				assertThat(result).hasSize(expectedCount);
				assertThat(result).extracting(ContentVo::getContentId).doesNotHaveDuplicates();
				assertThat(result).extracting(ContentVo::getContentId, ContentVo::getTitle,
						ContentVo::getPosterImageUrl, ContentVo::getContentType)
					.isSubsetOf(expectedSummaries);
			} finally {
				session.rollback(true);
			}
		}
	}

	private static void insertContent(Connection connection, long id, String type, String title, String poster)
		throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("""
			INSERT INTO CONTENT (CONTENT_ID, CONTENT_TYPE, TITLE, POSTER_IMAGE_URL, TMDB_ID)
			VALUES (?, ?, ?, ?, ?)
			""")) {
			statement.setLong(1, id);
			statement.setString(2, type);
			statement.setString(3, title);
			statement.setString(4, poster);
			statement.setLong(5, id);
			statement.executeUpdate();
		}
	}
}
