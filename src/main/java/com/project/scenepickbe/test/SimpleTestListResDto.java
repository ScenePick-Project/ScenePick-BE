package com.project.scenepickbe.test;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class SimpleTestListResDto {

	private int totalCount;
	private List<SimpleTestListDetailResDto> dataList;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
	public static class SimpleTestListDetailResDto {
		private int id;
		private String name;
	}
}
