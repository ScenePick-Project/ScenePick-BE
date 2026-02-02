package com.project.scenepickbe.infrastructure.tmdb;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TmdbClient {
	private static final String DEFAULT_LANGUAGE = "ko-KR";

	private final RestClient tmdbRestClient;
	private final TmdbProperties tmdbProperties;

	public TmdbResponse.TvDetail getTvDetail(Long tmdbId) {
		try {
			return tmdbRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/tv/{id}")
					.queryParam("language", DEFAULT_LANGUAGE)
					.build(tmdbId))
				.retrieve()
				.body(TmdbResponse.TvDetail.class);
		} catch (RestClientResponseException ex) {
			log.error("TMDB call failed: GET /tv/{id} status={} body={}", ex.getRawStatusCode(),
				ex.getResponseBodyAsString(), ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		} catch (RestClientException ex) {
			log.error("TMDB call failed: GET /tv/{id}", ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		}
	}

	public TmdbResponse.MovieDetail getMovieDetail(Long tmdbId) {
		try {
			return tmdbRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/movie/{id}")
					.queryParam("language", DEFAULT_LANGUAGE)
					.build(tmdbId))
				.retrieve()
				.body(TmdbResponse.MovieDetail.class);
		} catch (RestClientResponseException ex) {
			log.error("TMDB call failed: GET /movie/{id} status={} body={}", ex.getRawStatusCode(),
				ex.getResponseBodyAsString(), ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		} catch (RestClientException ex) {
			log.error("TMDB call failed: GET /movie/{id}", ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		}
	}

	public TmdbResponse.CreditList getTvCredits(Long tmdbId) {
		try {
			return tmdbRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/tv/{id}/credits")
					.queryParam("language", DEFAULT_LANGUAGE)
					.build(tmdbId))
				.retrieve()
				.body(TmdbResponse.CreditList.class);
		} catch (RestClientResponseException ex) {
			log.error("TMDB call failed: GET /tv/{id}/credits status={} body={}", ex.getRawStatusCode(),
				ex.getResponseBodyAsString(), ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		} catch (RestClientException ex) {
			log.error("TMDB call failed: GET /tv/{id}/credits", ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		}
	}

	public TmdbResponse.CreditList getMovieCredits(Long tmdbId) {
		try {
			return tmdbRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/movie/{id}/credits")
					.queryParam("language", DEFAULT_LANGUAGE)
					.build(tmdbId))
				.retrieve()
				.body(TmdbResponse.CreditList.class);
		} catch (RestClientResponseException ex) {
			log.error("TMDB call failed: GET /movie/{id}/credits status={} body={}", ex.getRawStatusCode(),
				ex.getResponseBodyAsString(), ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		} catch (RestClientException ex) {
			log.error("TMDB call failed: GET /movie/{id}/credits", ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		}
	}

	public TmdbResponse.SeasonDetail getTvSeason(Long tmdbId, Integer seasonNumber) {
		try {
			return tmdbRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/tv/{id}/season/{seasonNumber}")
					.queryParam("language", DEFAULT_LANGUAGE)
					.build(tmdbId, seasonNumber))
				.retrieve()
				.body(TmdbResponse.SeasonDetail.class);
		} catch (RestClientResponseException ex) {
			log.error("TMDB call failed: GET /tv/{id}/season/{seasonNumber} status={} body={}",
				ex.getRawStatusCode(), ex.getResponseBodyAsString(), ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		} catch (RestClientException ex) {
			log.error("TMDB call failed: GET /tv/{id}/season/{seasonNumber}", ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		}
	}

	public TmdbResponse.CreditList getTvSeasonCredits(Long tmdbId, Integer seasonNumber) {
		try {
			return tmdbRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/tv/{id}/season/{seasonNumber}/credits")
					.queryParam("language", DEFAULT_LANGUAGE)
					.build(tmdbId, seasonNumber))
				.retrieve()
				.body(TmdbResponse.CreditList.class);
		} catch (RestClientResponseException ex) {
			log.error("TMDB call failed: GET /tv/{id}/season/{seasonNumber}/credits status={} body={}",
				ex.getRawStatusCode(), ex.getResponseBodyAsString(), ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		} catch (RestClientException ex) {
			log.error("TMDB call failed: GET /tv/{id}/season/{seasonNumber}/credits", ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		}
	}

	public String toImageUrl(String path) {
		if (path == null || path.isBlank()) {
			return null;
		}
		return tmdbProperties.getImageBaseUrl() + path;
	}

}
