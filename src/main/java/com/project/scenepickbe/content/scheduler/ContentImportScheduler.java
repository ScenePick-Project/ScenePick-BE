package com.project.scenepickbe.content.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

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
	private static final int IMPORT_WINDOW_DAYS = 30;

	private final TmdbClient tmdbClient;
	private final ContentCommandService contentCommandService;

	@Scheduled(cron = "${tmdb.import.cron:-}", zone = "Asia/Seoul")
	public void importRecentContents() {
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		LocalDate fromDate = today.minusDays(IMPORT_WINDOW_DAYS - 1);
		log.info("Start scheduled recent content import: from={} to={}", fromDate, today);
		importRecentContents(ContentType.MOVIE, fromDate, today);
		importRecentContents(ContentType.TV, fromDate, today);
		log.info("Complete scheduled recent content import");
	}

	private void importRecentContents(ContentType contentType, LocalDate fromDate, LocalDate toDate) {
		TmdbResponse.PopularContentList recentContents;
		try {
			recentContents = contentType == ContentType.MOVIE
				? tmdbClient.getRecentMovies(fromDate, toDate)
				: tmdbClient.getRecentlyAiredTvs(fromDate, toDate);
		} catch (RuntimeException ex) {
			log.error("Failed to fetch TMDB recent contents: type={}", contentType, ex);
			return;
		}

		List<TmdbResponse.PopularContent> contentList = recentContents == null
			|| recentContents.contentList() == null ? List.of() : recentContents.contentList();
		contentList.stream()
			.filter(Objects::nonNull)
			.map(TmdbResponse.PopularContent::id)
			.filter(tmdbId -> tmdbId != null && tmdbId > 0)
			.distinct()
			.limit(IMPORT_LIMIT)
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
