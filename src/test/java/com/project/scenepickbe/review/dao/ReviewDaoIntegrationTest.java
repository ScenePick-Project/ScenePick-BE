package com.project.scenepickbe.review.dao;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.jdbc.core.JdbcTemplate;

import com.project.scenepickbe.review.vo.ReviewVo;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@MapperScan("com.project.scenepickbe.review.dao")
class ReviewDaoIntegrationTest {

	@Autowired
	private ReviewDao reviewDao;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private String userId;
	private Long contentId;

	@AfterEach
	void cleanup() {
		if (contentId != null) {
			jdbcTemplate.update("DELETE FROM REVIEW WHERE CONTENT_ID = ?", contentId);
			jdbcTemplate.update("DELETE FROM CONTENT WHERE CONTENT_ID = ?", contentId);
		}
		if (userId != null) {
			jdbcTemplate.update("DELETE FROM USERS WHERE USER_ID = ?", userId);
		}
	}

	@Test
	@DisplayName("리뷰 목록 조회 시 삭제된 리뷰는 제외")
	void selectReviewListExcludesDeleted() {
		userId = "test_user_001";

		// 테스트 유저
		jdbcTemplate.update(
			"INSERT INTO USERS (USER_ID, EMAIL, USERNAME, ROLE, DEL_YN) VALUES (?, ?, ?, 'USER', 'N')",
			userId, "test_user_001@example.com", "테스트유저"
		);

		Long tmdbId = 99999L;

		// 테스트 작품
		jdbcTemplate.update(
			"INSERT INTO CONTENT (TMDB_ID, CONTENT_TYPE, TITLE) VALUES (?, ?, ?)",
			tmdbId, "MOVIE", "테스트 작품"
		);
		contentId = jdbcTemplate.queryForObject(
			"SELECT CONTENT_ID FROM CONTENT WHERE TMDB_ID = ? AND CONTENT_TYPE = ?",
			Long.class, tmdbId, "MOVIE"
		);

		// 정상 리뷰
		jdbcTemplate.update(
			"INSERT INTO REVIEW (CONTENT_ID, USER_ID, REVIEW_BODY, IS_SPOILER, DEL_YN) VALUES (?, ?, ?, ?, 'N')",
			contentId, userId, "삭제되지 않은 리뷰", 0
		);

		// 삭제된 리뷰
		jdbcTemplate.update(
			"INSERT INTO REVIEW (CONTENT_ID, USER_ID, REVIEW_BODY, IS_SPOILER, DEL_YN, DELETED_AT) " +
				"VALUES (?, ?, ?, ?, 'Y', SYSTIMESTAMP)",
			contentId, userId, "삭제된 리뷰", 0
		);

		List<ReviewVo> result = reviewDao.selectReviewList(contentId);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getReviewBody()).isEqualTo("삭제되지 않은 리뷰");
	}
}
