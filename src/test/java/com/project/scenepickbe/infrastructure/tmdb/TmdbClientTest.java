package com.project.scenepickbe.infrastructure.tmdb;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

class TmdbClientTest {
	private static final LocalDate FROM = LocalDate.of(2026, 9, 13);
	private static final LocalDate TO = LocalDate.of(2026, 10, 12);
	private MockRestServiceServer server;
	private TmdbClient client;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://tmdb.test/3");
		server = MockRestServiceServer.bindTo(builder).build();
		client = new TmdbClient(builder.build(), new TmdbProperties());
	}

	@Test
	void discoversReleasedMoviesWithinWindowInReleaseOrder() {
		server.expect(request -> assertThat(request.getURI().getPath()).isEqualTo("/3/discover/movie"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(queryParam("primary_release_date.gte", FROM.toString()))
			.andExpect(queryParam("primary_release_date.lte", TO.toString()))
			.andExpect(queryParam("sort_by", "primary_release_date.desc"))
			.andExpect(queryParam("include_adult", "false"))
			.andExpect(queryParam("include_video", "false"))
			.andExpect(queryParam("language", "ko-KR"))
			.andExpect(queryParam("page", "1"))
			.andRespond(withSuccess("""
				{"results":[{"id":11,"release_date":"2026-10-12"},{"id":12,"release_date":"2026-10-11"}]}
				""", MediaType.APPLICATION_JSON));

		assertThat(client.getRecentMovies(FROM, TO).contentList())
			.extracting(TmdbResponse.PopularContent::id).containsExactly(11L, 12L);
		server.verify();
	}

	@Test
	void discoversRecentEpisodesWithoutExcludingOlderSeries() {
		server.expect(request -> {
			assertThat(request.getURI().getPath()).isEqualTo("/3/discover/tv");
			assertThat(UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams())
				.doesNotContainKey("first_air_date.gte");
		})
			.andExpect(queryParam("air_date.gte", FROM.toString()))
			.andExpect(queryParam("air_date.lte", TO.toString()))
			.andExpect(queryParam("first_air_date.lte", TO.toString()))
			.andExpect(queryParam("include_null_first_air_dates", "false"))
			.andExpect(queryParam("sort_by", "popularity.desc"))
			.andExpect(queryParam("include_adult", "false"))
			.andExpect(queryParam("timezone", "Asia/Seoul"))
			.andRespond(withSuccess("""
				{"results":[{"id":21,"first_air_date":"2010-01-01"},{"id":22,"first_air_date":"2026-10-01"}]}
				""", MediaType.APPLICATION_JSON));

		assertThat(client.getRecentlyAiredTvs(FROM, TO).contentList())
			.extracting(TmdbResponse.PopularContent::id).containsExactly(21L, 22L);
		server.verify();
	}

	@Test
	void preservesPopularRequestsForExistingManualImport() {
		server.expect(requestTo("https://tmdb.test/3/movie/popular?language=ko-KR&page=1"))
			.andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));
		server.expect(requestTo("https://tmdb.test/3/tv/popular?language=ko-KR&page=1"))
			.andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

		assertThat(client.getPopularMovies().contentList()).isEmpty();
		assertThat(client.getPopularTvs().contentList()).isEmpty();
		server.verify();
	}

	@Test
	void mapsDiscoveryFailureToExistingTmdbError() {
		server.expect(request -> assertThat(request.getURI().getPath()).isEqualTo("/3/discover/movie"))
			.andRespond(withServerError());

		assertThatThrownBy(() -> client.getRecentMovies(FROM, TO)).isInstanceOf(GeneralException.class);
		server.verify();
	}
}
