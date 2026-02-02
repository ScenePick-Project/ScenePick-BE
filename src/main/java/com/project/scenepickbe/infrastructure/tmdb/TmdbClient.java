package com.project.scenepickbe.infrastructure.tmdb;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TmdbClient {
	private static final String DEFAULT_LANGUAGE = "ko-KR";

	private final RestClient tmdbRestClient;
	private final TmdbProperties tmdbProperties;

	public TmdbResponse.TvDetail getTvDetail(Long tmdbId) {
		return tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/tv/{id}")
				.queryParam("language", DEFAULT_LANGUAGE)
				.build(tmdbId))
			.retrieve()
			.body(TmdbResponse.TvDetail.class);
	}

	public TmdbResponse.MovieDetail getMovieDetail(Long tmdbId) {
		return tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/movie/{id}")
				.queryParam("language", DEFAULT_LANGUAGE)
				.build(tmdbId))
			.retrieve()
			.body(TmdbResponse.MovieDetail.class);
	}

	public TmdbResponse.CreditList getTvCredits(Long tmdbId) {
		return tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/tv/{id}/credits")
				.queryParam("language", DEFAULT_LANGUAGE)
				.build(tmdbId))
			.retrieve()
			.body(TmdbResponse.CreditList.class);
	}

	public TmdbResponse.CreditList getMovieCredits(Long tmdbId) {
		return tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/movie/{id}/credits")
				.queryParam("language", DEFAULT_LANGUAGE)
				.build(tmdbId))
			.retrieve()
			.body(TmdbResponse.CreditList.class);
	}

	public TmdbResponse.SeasonDetail getTvSeason(Long tmdbId, Integer seasonNumber) {
		return tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/tv/{id}/season/{seasonNumber}")
				.queryParam("language", DEFAULT_LANGUAGE)
				.build(tmdbId, seasonNumber))
			.retrieve()
			.body(TmdbResponse.SeasonDetail.class);
	}

	public TmdbResponse.CreditList getTvSeasonCredits(Long tmdbId, Integer seasonNumber) {
		return tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/tv/{id}/season/{seasonNumber}/credits")
				.queryParam("language", DEFAULT_LANGUAGE)
				.build(tmdbId, seasonNumber))
			.retrieve()
			.body(TmdbResponse.CreditList.class);
	}

	public String toImageUrl(String path) {
		if (path == null || path.isBlank()) {
			return null;
		}
		return tmdbProperties.getImageBaseUrl() + path;
	}
}
