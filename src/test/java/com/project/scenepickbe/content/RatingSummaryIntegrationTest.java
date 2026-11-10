package com.project.scenepickbe.content;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.project.scenepickbe.content.dao.ContentDao;

@Testcontainers
class RatingSummaryIntegrationTest {
	@Container
	static final OracleContainer ORACLE = new OracleContainer(DockerImageName.parse(
		"gvenzl/oracle-xe@sha256:c2682a4216b0fe65537912c4a049c7e5c15a85bb7c94bb8137d5f2d2eef60603"))
		.withUsername("rating_test").withPassword("rating_test_only")
		.withStartupTimeout(Duration.ofMinutes(8));

	@Test
	void calculatesOnlyRatedActiveReviewsForSelectedContent() throws Exception {
		var source = new DriverManagerDataSource(ORACLE.getJdbcUrl(), ORACLE.getUsername(), ORACLE.getPassword());
		var db = new JdbcTemplate(source);
		db.execute("CREATE TABLE REVIEW (CONTENT_ID NUMBER, RATING NUMBER(2,1), DEL_YN VARCHAR2(1))");
		var config = new org.apache.ibatis.session.Configuration();
		config.setMapUnderscoreToCamelCase(true);
		var factory = new SqlSessionFactoryBean();
		factory.setDataSource(source);
		factory.setConfiguration(config);
		factory.setTypeAliasesPackage("com.project.scenepickbe.content.vo");
		factory.setMapperLocations(new ClassPathResource("mybatis/mapper/ContentMapper.xml"));
		try (var session = factory.getObject().openSession(true)) {
			var dao = session.getMapper(ContentDao.class);
			var empty = dao.selectRatingSummary(1L);
			assertThat(empty.averageRating()).isNull();
			assertThat(empty.ratingCount()).isZero();
			db.update("INSERT INTO REVIEW VALUES (1, 3.5, 'N')");
			db.update("INSERT INTO REVIEW VALUES (1, 4, 'N')");
			db.update("INSERT INTO REVIEW VALUES (1, NULL, 'N')");
			db.update("INSERT INTO REVIEW VALUES (1, 1, 'Y')");
			db.update("INSERT INTO REVIEW VALUES (2, 5, 'N')");
			session.clearCache();
			var summary = dao.selectRatingSummary(1L);
			assertThat(summary.averageRating()).isEqualByComparingTo("3.75");
			assertThat(summary.ratingCount()).isEqualTo(2L);
		}
	}
}
