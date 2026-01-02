package com.project.scenepickbe.content.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenreType {
	ACTION("액션"),
	ROMANCE("로맨스"),
	THRILLER("스릴러"),
	HORROR("공포"),
	COMEDY("코미디");

	private final String value;

}
