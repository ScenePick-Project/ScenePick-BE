package com.project.scenepickbe.content.scheduler;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	void importPopularContentsImportsMoviesAndTvsWithoutForce() {
		when(tmdbClient.getPopularMovies()).thenReturn(popularContents(1L, 2L));
		when(tmdbClient.getPopularTvs()).thenReturn(popularContents(3L));

		contentImportScheduler.importPopularContents();

		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 1L, false);
		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 2L, false);
		verify(contentCommandService).importFromTmdb(ContentType.TV, 3L, false);
	}

	@Test
	void importPopularContentsContinuesAfterAnImportFailure() {
		when(tmdbClient.getPopularMovies()).thenReturn(popularContents(1L, 2L));
		when(tmdbClient.getPopularTvs()).thenReturn(popularContents());
		doThrow(new RuntimeException()).when(contentCommandService)
			.importFromTmdb(ContentType.MOVIE, 1L, false);

		contentImportScheduler.importPopularContents();

		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 2L, false);
	}

	@Test
	void importPopularContentsImportsAtMostTwentyContentsPerType() {
		Long[] movieIds = LongStream.rangeClosed(1L, 21L).boxed().toArray(Long[]::new);
		when(tmdbClient.getPopularMovies()).thenReturn(popularContents(movieIds));
		when(tmdbClient.getPopularTvs()).thenReturn(popularContents());

		contentImportScheduler.importPopularContents();

		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 20L, false);
		verify(contentCommandService, never()).importFromTmdb(ContentType.MOVIE, 21L, false);
	}

	private TmdbResponse.PopularContentList popularContents(Long... tmdbIds) {
		return new TmdbResponse.PopularContentList(
			List.of(tmdbIds).stream().map(TmdbResponse.PopularContent::new).toList()
		);
	}
}
