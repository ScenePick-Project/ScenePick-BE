package com.project.scenepickbe.content.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.service.ContentCommandService;
import com.project.scenepickbe.infrastructure.tmdb.TmdbClient;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

@ExtendWith(MockitoExtension.class)
class ContentImportSchedulerTest {
	@InjectMocks
	private ContentImportScheduler contentImportScheduler;
	@Mock
	private TmdbClient tmdbClient;
	@Mock
	private ContentCommandService contentCommandService;

	@Test
	void importsRecentMoviesAndTvsWithSameThirtyDayWindowWithoutForce() {
		when(tmdbClient.getRecentMovies(any(), any())).thenReturn(contents(1L, 2L));
		when(tmdbClient.getRecentlyAiredTvs(any(), any())).thenReturn(contents(3L));
		LocalDate before = LocalDate.now(ZoneId.of("Asia/Seoul"));

		contentImportScheduler.importRecentContents();

		var from = ArgumentCaptor.forClass(LocalDate.class);
		var to = ArgumentCaptor.forClass(LocalDate.class);
		verify(tmdbClient).getRecentMovies(from.capture(), to.capture());
		assertThat(to.getValue()).isBetween(before, LocalDate.now(ZoneId.of("Asia/Seoul")));
		assertThat(from.getValue()).isEqualTo(to.getValue().minusDays(29));
		verify(tmdbClient).getRecentlyAiredTvs(from.getValue(), to.getValue());
		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 1L, false);
		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 2L, false);
		verify(contentCommandService).importFromTmdb(ContentType.TV, 3L, false);
		verify(tmdbClient, never()).getPopularMovies();
		verify(tmdbClient, never()).getPopularTvs();
	}

	@Test
	void continuesAfterAnImportFailure() {
		when(tmdbClient.getRecentMovies(any(), any())).thenReturn(contents(1L, 2L));
		when(tmdbClient.getRecentlyAiredTvs(any(), any())).thenReturn(contents());
		doThrow(new RuntimeException()).when(contentCommandService)
			.importFromTmdb(ContentType.MOVIE, 1L, false);

		contentImportScheduler.importRecentContents();

		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 2L, false);
	}

	@Test
	void importsAtMostTwentyUniqueValidContentsPerType() {
		Long[] ids = LongStream.rangeClosed(1L, 21L).boxed().toArray(Long[]::new);
		when(tmdbClient.getRecentMovies(any(), any())).thenReturn(contents(ids));
		when(tmdbClient.getRecentlyAiredTvs(any(), any())).thenReturn(contents(ids));

		contentImportScheduler.importRecentContents();

		verify(contentCommandService, times(20)).importFromTmdb(eq(ContentType.MOVIE), anyLong(), eq(false));
		verify(contentCommandService, times(20)).importFromTmdb(eq(ContentType.TV), anyLong(), eq(false));
		verify(contentCommandService, never()).importFromTmdb(any(), eq(21L), anyBoolean());
	}

	@Test
	void skipsNullInvalidAndDuplicateCandidates() {
		when(tmdbClient.getRecentMovies(any(), any())).thenReturn(new TmdbResponse.PopularContentList(
			Arrays.asList(null, new TmdbResponse.PopularContent(null),
				new TmdbResponse.PopularContent(0L), new TmdbResponse.PopularContent(-1L),
				new TmdbResponse.PopularContent(5L), new TmdbResponse.PopularContent(5L))));
		when(tmdbClient.getRecentlyAiredTvs(any(), any())).thenReturn(contents());

		contentImportScheduler.importRecentContents();

		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 5L, false);
		verifyNoMoreInteractions(contentCommandService);
	}

	@Test
	void continuesWithTvWhenMovieDiscoveryFails() {
		when(tmdbClient.getRecentMovies(any(), any())).thenThrow(new RuntimeException("TMDB unavailable"));
		when(tmdbClient.getRecentlyAiredTvs(any(), any())).thenReturn(contents(3L));

		contentImportScheduler.importRecentContents();

		verify(contentCommandService).importFromTmdb(ContentType.TV, 3L, false);
		verifyNoMoreInteractions(contentCommandService);
	}

	@Test
	void handlesMissingDiscoveryResponses() {
		when(tmdbClient.getRecentMovies(any(), any())).thenReturn(null);
		when(tmdbClient.getRecentlyAiredTvs(any(), any())).thenReturn(new TmdbResponse.PopularContentList(null));

		contentImportScheduler.importRecentContents();

		verifyNoInteractions(contentCommandService);
	}

	private TmdbResponse.PopularContentList contents(Long... ids) {
		return new TmdbResponse.PopularContentList(List.of(ids).stream()
			.map(TmdbResponse.PopularContent::new).toList());
	}
}
