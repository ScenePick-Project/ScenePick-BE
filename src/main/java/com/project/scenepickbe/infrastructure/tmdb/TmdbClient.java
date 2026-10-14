package com.project.scenepickbe.infrastructure.tmdb;

import java.time.LocalDate;
import java.util.Map;

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

	public TmdbResponse.PopularContentList getPopularMovies() {
		return getPopularContents("/movie/popular");
	}

	public TmdbResponse.PopularContentList getPopularTvs() {
		return getPopularContents("/tv/popular");
	}

	public TmdbResponse.PopularContentList getRecentMovies(LocalDate fromDate, LocalDate toDate) {
		return getContents("/discover/movie", Map.of(
			"primary_release_date.gte", fromDate,
			"primary_release_date.lte", toDate,
			"sort_by", "primary_release_date.desc",
			"include_adult", false,
			"include_video", false));
	}

	public TmdbResponse.PopularContentList getRecentlyAiredTvs(LocalDate fromDate, LocalDate toDate) {
		// 첫 방영일의 하한을 두면 기존 시리즈의 최근 방영분을 놓친다.
		return getContents("/discover/tv", Map.of(
			"air_date.gte", fromDate,
			"air_date.lte", toDate,
			"first_air_date.lte", toDate,
			"include_null_first_air_dates", false,
			"sort_by", "popularity.desc",
			"include_adult", false,
			"timezone", "Asia/Seoul"));
	}

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

	private TmdbResponse.PopularContentList getPopularContents(String path) {
		return getContents(path, Map.of());
	}

	private TmdbResponse.PopularContentList getContents(String path, Map<String, Object> filters) {
		try {
			return tmdbRestClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path(path).queryParam("language", DEFAULT_LANGUAGE).queryParam("page", 1);
					filters.forEach((name, value) -> uriBuilder.queryParam(name, value));
					return uriBuilder.build();
				})
				.retrieve()
				.body(TmdbResponse.PopularContentList.class);
		} catch (RestClientResponseException ex) {
			log.error("TMDB call failed: GET {} status={} body={}", path, ex.getRawStatusCode(),
				ex.getResponseBodyAsString(), ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		} catch (RestClientException ex) {
			log.error("TMDB call failed: GET {}", path, ex);
			throw new GeneralException(ErrorStatus.TMDB_API_FAIL);
		}
	}

}
