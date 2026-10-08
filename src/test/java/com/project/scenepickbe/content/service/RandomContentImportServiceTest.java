package com.project.scenepickbe.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.infrastructure.tmdb.TmdbClient;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

@ExtendWith(MockitoExtension.class)
class RandomContentImportServiceTest {

	@InjectMocks
	private RandomContentImportService randomContentImportService;

	@Mock
	private TmdbClient tmdbClient;

	@Mock
	private ContentCommandService contentCommandService;

	@Test
	void importsRandomCandidatesFromMovieAndTvPopularLists() {
		when(tmdbClient.getPopularMovies()).thenReturn(popularContents(1L, 2L));
		when(tmdbClient.getPopularTvs()).thenReturn(popularContents(3L, 4L));
		when(contentCommandService.importFromTmdb(any(), anyLong(), eq(false))).thenAnswer(invocation -> {
			ContentType contentType = invocation.getArgument(0);
			Long tmdbId = invocation.getArgument(1);
			return importedResult(contentType, tmdbId);
		});

		ContentImportResponse.Batch result = randomContentImportService.importRandomPopularContents(3);

		assertThat(result.requestedCount()).isEqualTo(3);
		assertThat(result.processedCount()).isEqualTo(3);
		assertThat(result.failedCount()).isZero();
		assertThat(result.resultList()).hasSize(3);
		assertThat(result.resultList()).extracting(item -> item.result().tmdbId())
			.allMatch(tmdbId -> List.of(1L, 2L, 3L, 4L).contains(tmdbId));
	}

	@Test
	void importsAtMostTwentyCandidates() {
		Long[] movieIds = new Long[21];
		for (int index = 0; index < movieIds.length; index++) {
			movieIds[index] = (long) index + 1;
		}
		when(tmdbClient.getPopularMovies()).thenReturn(popularContents(movieIds));
		when(tmdbClient.getPopularTvs()).thenReturn(popularContents());
		when(contentCommandService.importFromTmdb(any(), anyLong(), eq(false))).thenAnswer(invocation ->
			importedResult(invocation.getArgument(0), invocation.getArgument(1)));

		ContentImportResponse.Batch result = randomContentImportService.importRandomPopularContents(25);

		assertThat(result.requestedCount()).isEqualTo(20);
		assertThat(result.processedCount()).isEqualTo(20);
		verify(contentCommandService, times(20)).importFromTmdb(any(), anyLong(), eq(false));
	}

	@Test
	void continuesAfterAnIndividualImportFailure() {
		when(tmdbClient.getPopularMovies()).thenReturn(popularContents(1L, 2L));
		when(tmdbClient.getPopularTvs()).thenReturn(popularContents());
		doThrow(new RuntimeException("TMDB detail failed")).when(contentCommandService)
			.importFromTmdb(ContentType.MOVIE, 1L, false);
		when(contentCommandService.importFromTmdb(ContentType.MOVIE, 2L, false))
			.thenReturn(importedResult(ContentType.MOVIE, 2L));

		ContentImportResponse.Batch result = randomContentImportService.importRandomPopularContents(2);

		assertThat(result.processedCount()).isEqualTo(1);
		assertThat(result.failedCount()).isEqualTo(1);
		verify(contentCommandService).importFromTmdb(ContentType.MOVIE, 2L, false);
	}

	@Test
	void continuesWhenOnePopularListCannotBeRetrieved() {
		when(tmdbClient.getPopularMovies()).thenThrow(new RuntimeException("TMDB unavailable"));
		when(tmdbClient.getPopularTvs()).thenReturn(popularContents(3L));
		when(contentCommandService.importFromTmdb(ContentType.TV, 3L, false))
			.thenReturn(importedResult(ContentType.TV, 3L));

		ContentImportResponse.Batch result = randomContentImportService.importRandomPopularContents(2);

		assertThat(result.processedCount()).isOne();
		assertThat(result.failedCount()).isZero();
		verify(contentCommandService).importFromTmdb(ContentType.TV, 3L, false);
	}

	private TmdbResponse.PopularContentList popularContents(Long... tmdbIds) {
		return new TmdbResponse.PopularContentList(
			List.of(tmdbIds).stream().map(TmdbResponse.PopularContent::new).toList()
		);
	}

	private ContentImportResponse.Result importedResult(ContentType contentType, Long tmdbId) {
		return new ContentImportResponse.Result(
			new ContentImportResponse.Import(tmdbId, tmdbId, contentType, true),
			"TMDB에서 작품 정보를 가져왔습니다."
		);
	}
}
