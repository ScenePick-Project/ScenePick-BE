package com.project.scenepickbe.content.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.infrastructure.tmdb.TmdbClient;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RandomContentImportService {
	private static final int MAX_IMPORT_COUNT = 20;

	private final TmdbClient tmdbClient;
	private final ContentCommandService contentCommandService;

	public ContentImportResponse.Batch importRandomPopularContents(int count) {
		int importCount = Math.min(count, MAX_IMPORT_COUNT);
		List<ContentCandidate> candidates = new ArrayList<>();
		addPopularContentCandidates(candidates, ContentType.MOVIE);
		addPopularContentCandidates(candidates, ContentType.TV);
		Collections.shuffle(candidates);

		List<ContentImportResponse.Result> resultList = new ArrayList<>();
		int failedCount = 0;
		for (ContentCandidate candidate : candidates.stream().limit(importCount).toList()) {
			try {
				resultList.add(contentCommandService.importFromTmdb(candidate.contentType(), candidate.tmdbId(), false));
			} catch (RuntimeException ex) {
				failedCount++;
				log.error("Failed to import random TMDB content: type={} tmdbId={}", candidate.contentType(),
					candidate.tmdbId(), ex);
			}
		}

		return new ContentImportResponse.Batch(importCount, resultList.size(), failedCount, resultList);
	}

	private void addPopularContentCandidates(List<ContentCandidate> candidates, ContentType contentType) {
		try {
			TmdbResponse.PopularContentList popularContentList = contentType == ContentType.MOVIE
				? tmdbClient.getPopularMovies()
				: tmdbClient.getPopularTvs();
			if (popularContentList == null || popularContentList.contentList() == null) {
				return;
			}

			popularContentList.contentList().stream()
				.map(TmdbResponse.PopularContent::id)
				.filter(tmdbId -> tmdbId != null)
				.map(tmdbId -> new ContentCandidate(contentType, tmdbId))
				.forEach(candidates::add);
		} catch (RuntimeException ex) {
			log.error("Failed to retrieve TMDB popular contents: type={}", contentType, ex);
		}
	}

	private record ContentCandidate(ContentType contentType, Long tmdbId) {
	}
}
