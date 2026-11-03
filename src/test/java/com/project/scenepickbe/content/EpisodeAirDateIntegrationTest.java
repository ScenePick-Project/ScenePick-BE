package com.project.scenepickbe.content;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;

import org.flywaydb.core.Flyway;
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
import com.project.scenepickbe.content.vo.EpisodeVo;

@Testcontainers
class EpisodeAirDateIntegrationTest {
	@Container
	static final OracleContainer ORACLE = new OracleContainer(DockerImageName.parse(
		"gvenzl/oracle-xe@sha256:c2682a4216b0fe65537912c4a049c7e5c15a85bb7c94bb8137d5f2d2eef60603"))
		.withUsername("episode_test").withPassword("episode_test_only")
		.withStartupTimeout(Duration.ofMinutes(8));

	@Test
	void migrationPreservesExistingEpisodesAndMapperRoundTripsDates() throws Exception {
		var dataSource = new DriverManagerDataSource(
			ORACLE.getJdbcUrl(), ORACLE.getUsername(), ORACLE.getPassword());
		Flyway.configure().dataSource(dataSource).target("6").load().migrate();
		var db = new JdbcTemplate(dataSource);
		db.update("INSERT INTO CONTENT(CONTENT_ID,TITLE) VALUES(?,?)", 52L, "기존 작품");
		db.update("""
			INSERT INTO CONTENT_EPISODE(EPISODE_ID,CONTENT_ID,SEASON_NO,EPISODE_NO,TITLE)
			VALUES(?,?,?,?,?)
			""", 520L, 52L, 1, 1, "기존 회차");

		Flyway.configure().dataSource(dataSource).load().migrate();

		assertThat(db.queryForObject("SELECT TITLE FROM CONTENT_EPISODE WHERE EPISODE_ID=520", String.class))
			.isEqualTo("기존 회차");
		assertThat(db.queryForObject("SELECT AIR_DATE FROM CONTENT_EPISODE WHERE EPISODE_ID=520", LocalDate.class))
			.isNull();
		assertThat(db.queryForObject("SELECT COUNT(*) FROM CONTENT_EPISODE", Integer.class)).isEqualTo(1);

		var configuration = new org.apache.ibatis.session.Configuration();
		configuration.setMapUnderscoreToCamelCase(true);
		var factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setConfiguration(configuration);
		factory.setTypeAliasesPackage("com.project.scenepickbe.content.vo");
		factory.setMapperLocations(new ClassPathResource("mybatis/mapper/ContentMapper.xml"));
		try (var session = factory.getObject().openSession(true)) {
			ContentDao dao = session.getMapper(ContentDao.class);
			var dated = new EpisodeVo();
			dated.setContentId(52L);
			dated.setSeasonNo(1);
			dated.setEpisodeNo(2);
			dated.setTitle("방영일 있는 회차");
			dated.setAirDate(LocalDate.of(2026, 11, 3));
			dao.insertContentEpisode(dated);
			var undated = new EpisodeVo();
			undated.setContentId(52L);
			undated.setSeasonNo(1);
			undated.setEpisodeNo(3);
			undated.setTitle("방영일 미정 회차");
			dao.insertContentEpisode(undated);

			var episodes = dao.selectSeasonEpisodes(52L, 1);
			assertThat(episodes).extracting(EpisodeVo::getAirDate)
				.containsExactly(null, LocalDate.of(2026, 11, 3), null);
			assertThat(episodes.getFirst().getEpisodeId()).isEqualTo(520L);
		}
	}
}
