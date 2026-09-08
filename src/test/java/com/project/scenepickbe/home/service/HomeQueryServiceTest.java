package com.project.scenepickbe.home.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.DataAccessResourceFailureException;

import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.home.dto.response.HomeResponse;

class HomeQueryServiceTest {

	@Test
	void returnsEmptyRecommendationsWhenNoContentExists() {
		ContentDao contentDao = mock(ContentDao.class);
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of());

		var result = new HomeQueryService(contentDao).getRecommendations();

		assertThat(result.recommendationType()).isEqualTo("RANDOM");
		assertThat(result.contentList()).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(ContentType.class)
	void returnsStoredContentSummary(ContentType contentType) {
		ContentDao contentDao = mock(ContentDao.class);
		ContentVo content = new ContentVo();
		content.setContentId(17L);
		content.setTitle("추천 영화");
		content.setPosterImageUrl("https://example.com/movie.jpg");
		content.setContentType(contentType);
		when(contentDao.selectRandomContentsForHome()).thenReturn(List.of(content));

		var result = new HomeQueryService(contentDao).getRecommendations();

		assertThat(result.contentList()).containsExactly(
			new HomeResponse.Content(17L, "추천 영화", "https://example.com/movie.jpg", contentType));
	}

	@Test
	void propagatesDatabaseFailure() {
		ContentDao contentDao = mock(ContentDao.class);
		var failure = new DataAccessResourceFailureException("test database unavailable");
		when(contentDao.selectRandomContentsForHome()).thenThrow(failure);

		assertThatThrownBy(() -> new HomeQueryService(contentDao).getRecommendations()).isSameAs(failure);
	}
}
