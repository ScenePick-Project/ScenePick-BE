package com.project.scenepickbe.content.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.service.ContentCommandService;
import com.project.scenepickbe.infrastructure.tmdb.TmdbClient;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentImportScheduler {
	private static final int IMPORT_LIMIT = 20;

	private final TmdbClient tmdbClient;
	private final ContentCommandService contentCommandService;

	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	public void importPopularContents() {
		log.info("Start scheduled popular content import");
		importPopularContents(ContentType.MOVIE);
		importPopularContents(ContentType.TV);
		log.info("Complete scheduled popular content import");
	}

	private void importPopularContents(ContentType contentType) {
		TmdbResponse.PopularContentList popularContentList;
		try {
			popularContentList = contentType == ContentType.MOVIE
				? tmdbClient.getPopularMovies()
				: tmdbClient.getPopularTvs();
		} catch (RuntimeException ex) {
			log.error("Failed to fetch TMDB popular contents: type={}", contentType, ex);
			return;
		}

		List<TmdbResponse.PopularContent> contentList = popularContentList == null
			|| popularContentList.contentList() == null ? List.of() : popularContentList.contentList();
		contentList.stream()
			.limit(IMPORT_LIMIT)
			.map(TmdbResponse.PopularContent::id)
			.filter(tmdbId -> tmdbId != null)
			.forEach(tmdbId -> importContent(contentType, tmdbId));
	}

	private void importContent(ContentType contentType, Long tmdbId) {
		try {
			contentCommandService.importFromTmdb(contentType, tmdbId, false);
		} catch (RuntimeException ex) {
			log.error("Failed to import TMDB content: type={} tmdbId={}", contentType, tmdbId, ex);
		}
	}
}
