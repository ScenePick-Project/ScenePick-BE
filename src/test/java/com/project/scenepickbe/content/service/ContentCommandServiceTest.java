package com.project.scenepickbe.content.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.infrastructure.tmdb.TmdbClient;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

@ExtendWith(MockitoExtension.class)
class ContentCommandServiceTest {

	@InjectMocks
	private ContentCommandService contentCommandService;

	@Mock
	private ContentDao contentDao;

	@Mock
	private TmdbClient tmdbClient;

	@Test
	@DisplayName("이미 등록된 작품이고 force=false이면 기존 결과 반환")
	void importFromTmdbAlreadyExistsWithoutForce() {
		Long tmdbId = 123L;
		Long existingId = 10L;

		when(contentDao.selectContentIdByTmdb(tmdbId, ContentType.MOVIE)).thenReturn(existingId);

		ContentImportResponse.Result result = contentCommandService.importFromTmdb(ContentType.MOVIE, tmdbId, false);

		assertThat(result).isNotNull();
		assertThat(result.result().contentId()).isEqualTo(existingId);
		assertThat(result.result().imported()).isFalse();
		assertThat(result.message()).contains("이미 등록된 작품");

		verify(contentDao, times(1)).selectContentIdByTmdb(tmdbId, ContentType.MOVIE);
		verifyNoMoreInteractions(contentDao);
		verifyNoInteractions(tmdbClient);
	}

	@Test
	@DisplayName("신규 영화 가져오기 성공")
	void importFromTmdbNewMovieSuccess() {
		Long tmdbId = 456L;
		Long contentId = 99L;

		TmdbResponse.MovieDetail movieDetail = new TmdbResponse.MovieDetail(
			tmdbId,
			"테스트 영화",
			"테스트 개요",
			"/poster.png",
			List.of(new TmdbResponse.Genre(28, "Action"))
		);

		TmdbResponse.CreditList credits = new TmdbResponse.CreditList(
			List.of(new TmdbResponse.Credit("배우A", "배역A", "/profile.png"))
		);

		when(contentDao.selectContentIdByTmdb(tmdbId, ContentType.MOVIE))
			.thenReturn(null, contentId);
		when(tmdbClient.getMovieDetail(tmdbId)).thenReturn(movieDetail);
		when(tmdbClient.getMovieCredits(tmdbId)).thenReturn(credits);

		ContentImportResponse.Result result = contentCommandService.importFromTmdb(ContentType.MOVIE, tmdbId, false);

		assertThat(result.result().contentId()).isEqualTo(contentId);

		verify(contentDao).insertContent(any());
		verify(contentDao).insertContentGenre(eq(contentId), any());
		verify(contentDao).insertContentCredit(any());
	}
}
