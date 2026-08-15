package com.project.scenepickbe.content.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.project.scenepickbe.content.dto.response.ContentResponse;
import com.project.scenepickbe.content.vo.ContentSeasonVo;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.CreditVo;
import com.project.scenepickbe.content.vo.EpisodeVo;

@Mapper(componentModel = "spring")
public interface ContentDtoMapper {

	ContentResponse.Credit toCredit(CreditVo creditVo);

	List<ContentResponse.Credit> toCredits(List<CreditVo> creditVoList);

	ContentResponse.Episode toEpisode(EpisodeVo episodeVo);

	List<ContentResponse.Episode> toEpisodes(List<EpisodeVo> episodeVoList);

	ContentResponse.Season toSeason(ContentSeasonVo contentSeasonVo);

	List<ContentResponse.Season> toSeasons(List<ContentSeasonVo> seasonVoList);

	@Mapping(target = "contentId", source = "contentVo.contentId")
	@Mapping(target = "title", source = "contentVo.title")
	@Mapping(target = "posterImageUrl", source = "contentVo.posterImageUrl")
	@Mapping(target = "synopsis", source = "contentVo.synopsis")
	@Mapping(target = "genreList", source = "genreList")
	@Mapping(target = "defaultSeasonNo", source = "defaultSeasonNo")
	@Mapping(target = "seasonList", source = "seasonList")
	ContentResponse.Basic toBasic(ContentVo contentVo, List<String> genreList, Integer defaultSeasonNo,
		List<ContentResponse.Season> seasonList);

	default ContentResponse.CreditList toCreditList(List<CreditVo> creditVoList) {
		return new ContentResponse.CreditList(toCredits(creditVoList));
	}

	default ContentResponse.EpisodeList toEpisodeList(List<EpisodeVo> episodeVoList) {
		return new ContentResponse.EpisodeList(toEpisodes(episodeVoList));
	}

	default ContentResponse.SeasonList toSeasonList(List<ContentSeasonVo> seasonVoList) {
		return new ContentResponse.SeasonList(toSeasons(seasonVoList));
	}
}
