package com.project.scenepickbe.content.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.vo.ContentVo;
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

	@Test
	@DisplayName("신규 TV의 대표 포스터와 줄거리를 원본 응답에서 저장")
	void importFromTmdbNewTvPreservesSeriesMetadata() {
		Long tmdbId = 789L;
		Long contentId = 100L;
		TmdbResponse.TvDetail detail = new TmdbResponse.TvDetail(
			tmdbId, "테스트 시리즈", "시리즈 줄거리", "/series.png", List.of(), List.of());
		when(contentDao.selectContentIdByTmdb(tmdbId, ContentType.TV)).thenReturn(null, contentId);
		when(tmdbClient.getTvDetail(tmdbId)).thenReturn(detail);
		when(tmdbClient.toImageUrl("/series.png")).thenReturn("https://image.tmdb.org/t/p/w500/series.png");

		contentCommandService.importFromTmdb(ContentType.TV, tmdbId, false);

		var captor = ArgumentCaptor.forClass(ContentVo.class);
		verify(contentDao).insertContent(captor.capture());
		assertThat(captor.getValue().getTitle()).isEqualTo("테스트 시리즈");
		assertThat(captor.getValue().getSynopsis()).isEqualTo("시리즈 줄거리");
		assertThat(captor.getValue().getPosterImageUrl()).isEqualTo("https://image.tmdb.org/t/p/w500/series.png");
		verify(contentDao, never()).updateContentByTmdb(any());
		verify(contentDao, never()).deleteContentSeasons(anyLong());
	}

	@Test
	void importFromTmdbTvAllowsMetadataMissingInSource() {
		Long tmdbId = 790L;
		when(contentDao.selectContentIdByTmdb(tmdbId, ContentType.TV)).thenReturn(null, 101L);
		when(tmdbClient.getTvDetail(tmdbId)).thenReturn(
			new TmdbResponse.TvDetail(tmdbId, "포스터 없는 시리즈", null, null, List.of(), List.of()));

		contentCommandService.importFromTmdb(ContentType.TV, tmdbId, false);

		var captor = ArgumentCaptor.forClass(ContentVo.class);
		verify(contentDao).insertContent(captor.capture());
		assertThat(captor.getValue().getSynopsis()).isNull();
		assertThat(captor.getValue().getPosterImageUrl()).isNull();
		verify(tmdbClient).toImageUrl(null);
	}

	@Test
	void importFromTmdbExistingTvDoesNotRefreshOrDeleteRelatedData() {
		when(contentDao.selectContentIdByTmdb(789L, ContentType.TV)).thenReturn(100L);

		assertThat(contentCommandService.importFromTmdb(ContentType.TV, 789L, false).result().imported()).isFalse();

		verify(contentDao).selectContentIdByTmdb(789L, ContentType.TV);
		verifyNoMoreInteractions(contentDao);
		verifyNoInteractions(tmdbClient);
	}
}
