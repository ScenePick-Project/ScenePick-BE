package com.project.scenepickbe.content.enums;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenreType {
	ACTION("액션"),
	ADVENTURE("모험"),
	ANIMATION("애니메이션"),
	ROMANCE("로맨스"),
	DRAMA("드라마"),
	MYSTERY("미스터리"),
	THRILLER("스릴러"),
	HORROR("공포"),
	COMEDY("코미디"),
	CRIME("범죄"),
	DOCUMENTARY("다큐멘터리"),
	FAMILY("가족"),
	FANTASY("판타지"),
	HISTORY("역사"),
	MUSIC("음악"),
	SCIENCE_FICTION("SF"),
	TV_MOVIE("TV 영화"),
	WAR("전쟁"),
	WESTERN("서부");

	private final String value;

	private static final Map<Integer, GenreType> TMDB_GENRE_MAP = Map.ofEntries(
		Map.entry(28, ACTION),
		Map.entry(12, ADVENTURE),
		Map.entry(16, ANIMATION),
		Map.entry(35, COMEDY),
		Map.entry(80, CRIME),
		Map.entry(99, DOCUMENTARY),
		Map.entry(18, DRAMA),
		Map.entry(10751, FAMILY),
		Map.entry(14, FANTASY),
		Map.entry(36, HISTORY),
		Map.entry(27, HORROR),
		Map.entry(10402, MUSIC),
		Map.entry(9648, MYSTERY),
		Map.entry(10749, ROMANCE),
		Map.entry(878, SCIENCE_FICTION),
		Map.entry(10770, TV_MOVIE),
		Map.entry(53, THRILLER),
		Map.entry(10752, WAR),
		Map.entry(37, WESTERN),
		Map.entry(10759, ACTION),
		Map.entry(10765, FANTASY)
	);

	public static GenreType fromTmdbId(Integer tmdbId) {
		if (tmdbId == null) {
			return null;
		}
		return TMDB_GENRE_MAP.get(tmdbId);
	}

}
