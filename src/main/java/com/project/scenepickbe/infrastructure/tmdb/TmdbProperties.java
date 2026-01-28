package com.project.scenepickbe.infrastructure.tmdb;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "tmdb")
@Getter
@Setter
public class TmdbProperties {
	private String apiToken;
	private String baseUrl = "https://api.themoviedb.org/3";
	private String imageBaseUrl = "https://image.tmdb.org/t/p/w500";
}
