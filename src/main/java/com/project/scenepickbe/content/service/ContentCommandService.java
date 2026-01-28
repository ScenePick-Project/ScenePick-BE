package com.project.scenepickbe.content.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentImportResponse;
import com.project.scenepickbe.content.enums.ContentType;
import com.project.scenepickbe.content.enums.GenreType;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.EpisodeVo;
import com.project.scenepickbe.content.vo.PersonVo;
import com.project.scenepickbe.infrastructure.tmdb.TmdbClient;
import com.project.scenepickbe.infrastructure.tmdb.dto.response.TmdbResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentCommandService {
	private static final int MAX_CAST_COUNT = 20;

	private final ContentDao contentDao;
	private final TmdbClient tmdbClient;

	/**
	 * TMDB에서 작품 정보를 가져와 저장합니다.
	 *
	 * @param contentType 작품 타입
	 * @param tmdbId TMDB 작품 ID
	 * @param force 기존 등록 여부와 무관하게 갱신 여부
	 * @return 가져오기 결과와 메시지
	 */
	@Transactional
	public ContentImportResponse.Result importFromTmdb(ContentType contentType, Long tmdbId, boolean force) {
		Long existingId = contentDao.selectContentIdByTmdb(tmdbId, contentType);
		if (existingId != null && !force) {
			return new ContentImportResponse.Result(
				new ContentImportResponse.Import(
					existingId,
					tmdbId,
					contentType,
					false
				),
				"이미 등록된 작품입니다. force=true로 다시 가져올 수 있습니다."
			);
		}

		ContentVo contentVo = buildContentVo(contentType, tmdbId);
		Long contentId;

		if (existingId == null) {
			contentDao.insertContent(contentVo);
			contentId = contentDao.selectContentIdByTmdb(tmdbId, contentType);
		} else {
			contentDao.updateContentByTmdb(contentVo);
			contentId = existingId;
			contentDao.deleteContentGenres(contentId);
			contentDao.deleteContentPersons(contentId);
			contentDao.deleteContentEpisodes(contentId);
		}

		if (contentId == null) {
			throw new IllegalStateException("TMDB 작품 등록에 실패했습니다.");
		}

		contentVo.setContentId(contentId);
		insertGenres(contentId, contentType, tmdbId);
		insertPersons(contentId, contentType, tmdbId);
		if (contentType == ContentType.TV) {
			insertEpisodes(contentId, tmdbId);
		}

		return new ContentImportResponse.Result(
			new ContentImportResponse.Import(
				contentId,
				tmdbId,
				contentType,
				true
			),
			"TMDB에서 작품 정보를 가져왔습니다."
		);
	}

	/**
	 * TMDB 상세 정보를 조회해 작품 기본 정보를 구성합니다.
	 *
	 * @param contentType 작품 타입
	 * @param tmdbId TMDB 작품 ID
	 * @return 작품 기본 정보 VO
	 */
	private ContentVo buildContentVo(ContentType contentType, Long tmdbId) {
		ContentVo contentVo = new ContentVo();
		contentVo.setTmdbId(tmdbId);
		contentVo.setContentType(contentType);

		if (contentType == ContentType.TV) {
			TmdbResponse.TvDetail detail = tmdbClient.getTvDetail(tmdbId);
			contentVo.setTitle(detail.name());
			contentVo.setSynopsis(detail.overview());
			contentVo.setPosterImageUrl(tmdbClient.toImageUrl(detail.posterPath()));
		} else {
			TmdbResponse.MovieDetail detail = tmdbClient.getMovieDetail(tmdbId);
			contentVo.setTitle(detail.title());
			contentVo.setSynopsis(detail.overview());
			contentVo.setPosterImageUrl(tmdbClient.toImageUrl(detail.posterPath()));
		}

		return contentVo;
	}

	/**
	 * TMDB 장르 정보를 조회해 작품 장르를 저장합니다.
	 *
	 * @param contentId 저장된 작품 ID
	 * @param contentType 작품 타입
	 * @param tmdbId TMDB 작품 ID
	 */
	private void insertGenres(Long contentId, ContentType contentType, Long tmdbId) {
		List<Integer> tmdbGenreIdList;
		if (contentType == ContentType.TV) {
			TmdbResponse.TvDetail detail = tmdbClient.getTvDetail(tmdbId);
			tmdbGenreIdList = detail.genreList() == null
				? List.of()
				: detail.genreList().stream().map(genre -> genre.id()).collect(Collectors.toList());
		} else {
			TmdbResponse.MovieDetail detail = tmdbClient.getMovieDetail(tmdbId);
			tmdbGenreIdList = detail.genreList() == null
				? List.of()
				: detail.genreList().stream().map(genre -> genre.id()).collect(Collectors.toList());
		}

		tmdbGenreIdList.stream()
			.map(GenreType::fromTmdbId)
			.filter(Objects::nonNull)
			.forEach(genre -> contentDao.insertContentGenre(contentId, genre));
	}

	/**
	 * TMDB 출연진 정보를 조회해 작품 출연진을 저장합니다.
	 *
	 * @param contentId 저장된 작품 ID
	 * @param contentType 작품 타입
	 * @param tmdbId TMDB 작품 ID
	 */
	private void insertPersons(Long contentId, ContentType contentType, Long tmdbId) {
		TmdbResponse.CreditsList credits = contentType == ContentType.TV
			? tmdbClient.getTvCredits(tmdbId)
			: tmdbClient.getMovieCredits(tmdbId);

		if (credits == null || credits.castList() == null) {
			return;
		}

		credits.castList().stream()
			.limit(MAX_CAST_COUNT)
			.map(cast -> toPersonVo(contentId, cast))
			.forEach(contentDao::insertContentPerson);
	}

	/**
	 * TMDB 출연진 정보를 작품 출연진 VO로 변환합니다.
	 *
	 * @param contentId 저장된 작품 ID
	 * @param cast TMDB 출연진 정보
	 * @return 작품 출연진 VO
	 */
	private PersonVo toPersonVo(Long contentId, TmdbResponse.Cast cast) {
		PersonVo personVo = new PersonVo();
		personVo.setContentId(contentId);
		personVo.setName(cast.name());
		personVo.setCharName(cast.character());
		personVo.setProfileImageUrl(tmdbClient.toImageUrl(cast.profilePath()));
		return personVo;
	}

	/**
	 * TMDB TV 시즌/에피소드 정보를 조회해 에피소드를 저장합니다.
	 *
	 * @param contentId 저장된 작품 ID
	 * @param tmdbId TMDB 작품 ID
	 */
	private void insertEpisodes(Long contentId, Long tmdbId) {
		TmdbResponse.TvDetail detail = tmdbClient.getTvDetail(tmdbId);
		if (detail == null || detail.seasonSummaryList() == null) {
			return;
		}

		List<TmdbResponse.SeasonSummary> seasonSummaryList = detail.seasonSummaryList().stream()
			.filter(season -> season.seasonNumber() != null && season.seasonNumber() > 0)
			.toList();

		for (TmdbResponse.SeasonSummary season : seasonSummaryList) {
			TmdbResponse.EpisodeList seasonResponse = tmdbClient.getTvSeason(tmdbId,
				season.seasonNumber());
			if (seasonResponse == null || seasonResponse.episodeList() == null) {
				continue;
			}

			for (TmdbResponse.Episode episode : seasonResponse.episodeList()) {
				EpisodeVo episodeVo = new EpisodeVo();
				episodeVo.setContentId(contentId);
				episodeVo.setEpisodeNo(episode.episodeNumber());
				episodeVo.setTitle(episode.name());
				episodeVo.setSummary(episode.overview());
				episodeVo.setStillImageUrl(tmdbClient.toImageUrl(episode.stillPath()));
				contentDao.insertContentEpisode(episodeVo);
			}
		}
	}
}
