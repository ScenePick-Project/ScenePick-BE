package com.project.scenepickbe.content.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentResponse;
import com.project.scenepickbe.content.enums.GenreType;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.EpisodeVo;
import com.project.scenepickbe.content.vo.CreditVo;

@ExtendWith(MockitoExtension.class)
class ContentQueryServiceTest {

	@InjectMocks
	private ContentQueryService contentQueryService;

	@Mock
	private ContentDao contentDao;

	@Test
	@DisplayName("작품 기본정보 조회 성공")
	void getBasicInfoSuccess() {
		Long contentId = 1L;
		ContentVo mockVo = new ContentVo();
		mockVo.setContentId(contentId);
		mockVo.setContentType(com.project.scenepickbe.content.enums.ContentType.TV);
		mockVo.setTitle("테스트 영화");
		mockVo.setPosterImageUrl("poster");
		mockVo.setSynopsis("테스트 줄거리");
		mockVo.setGenreList(List.of(GenreType.ACTION, GenreType.ROMANCE));

		when(contentDao.selectContentBasic(contentId)).thenReturn(mockVo);
		when(contentDao.selectContentSeasons(contentId)).thenReturn(List.of());

		ContentResponse.Basic result = contentQueryService.getBasicInfo(contentId);

		assertThat(result).isNotNull();
		assertThat(result.contentId()).isEqualTo(contentId);
		assertThat(result.title()).isEqualTo("테스트 영화");
		assertThat(result.defaultSeasonNo()).isEqualTo(1);
		assertThat(result.seasonList()).isEmpty();
		assertThat(result.genreList())
			.hasSize(2)
			.contains(GenreType.ACTION.name(), GenreType.ROMANCE.name());
		verify(contentDao, times(1)).selectContentBasic(contentId);
	}

	@Test
	@DisplayName("작품 기본정보 조회 실패 - 작품 없을 경우")
	void getBasicInfoFailure() {
		Long invalidContentId = 9999L;

		when(contentDao.selectContentBasic(invalidContentId)).thenReturn(null);

		assertThatThrownBy(() -> contentQueryService.getBasicInfo(invalidContentId))
			.isInstanceOf(GeneralException.class)
			.satisfies(res -> {
				GeneralException ex = (GeneralException)res;
				assertThat(ex.getCode()).isEqualTo(ErrorStatus.CONTENT_NOT_FOUND);
			});

		verify(contentDao, times(1)).selectContentBasic(invalidContentId);
	}

	@Test
	@DisplayName("작품 출연진 리스트 조회 성공")
	void getCredits() {
		Long contentId = 1L;
		CreditVo mockVo1 = new CreditVo();
		CreditVo mockVo2 = new CreditVo();

		mockVo1.setContentId(contentId);
		mockVo1.setCreditId(1L);
		mockVo1.setName("배우A");
		mockVo1.setCharName("배역A");

		mockVo2.setCreditId(2L);
		mockVo2.setContentId(contentId);
		mockVo2.setName("배우B");
		mockVo2.setCharName("배역B");

		when(contentDao.selectContentCredits(contentId)).thenReturn(List.of(mockVo1, mockVo2));

		ContentResponse.CreditList expected = new ContentResponse.CreditList(List.of(
			new ContentResponse.Credit(1L, "배우A", "배역A", null),
			new ContentResponse.Credit(2L, "배우B", "배역B", null)
		));

		ContentResponse.CreditList result = contentQueryService.getCredits(contentId);

		assertThat(result)
			.usingRecursiveComparison()
			.isEqualTo(expected);

		verify(contentDao, times(1)).selectContentCredits(contentId);
	}

	@Test
	@DisplayName("작품 에피소드 리스트 조회 성공")
	void getEpisodes() {
		Long contentId = 1L;
		Integer seasonNo = 1;
		EpisodeVo mockVo1 = new EpisodeVo();
		EpisodeVo mockVo2 = new EpisodeVo();

		mockVo1.setContentId(contentId);
		mockVo1.setEpisodeId(1L);
		mockVo1.setEpisodeNo(1);
		mockVo1.setTitle("제목A");

		mockVo2.setContentId(contentId);
		mockVo2.setEpisodeId(2L);
		mockVo2.setEpisodeNo(2);
		mockVo2.setTitle("제목B");

		when(contentDao.selectSeasonEpisodes(contentId, seasonNo)).thenReturn(List.of(mockVo1, mockVo2));

		ContentResponse.EpisodeList expected = new ContentResponse.EpisodeList(List.of(
			new ContentResponse.Episode(1L, 1, "제목A", null, null),
			new ContentResponse.Episode(2L, 2, "제목B", null, null)
		));

		ContentResponse.EpisodeList result = contentQueryService.getEpisodes(contentId, seasonNo);

		assertThat(result)
			.usingRecursiveComparison()
			.isEqualTo(expected);

		verify(contentDao, times(1)).selectSeasonEpisodes(contentId, seasonNo);
	}
}
