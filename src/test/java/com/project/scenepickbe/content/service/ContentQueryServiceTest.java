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
import org.modelmapper.ModelMapper;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.content.dao.ContentDao;
import com.project.scenepickbe.content.dto.response.ContentResponse;
import com.project.scenepickbe.content.enums.GenreType;
import com.project.scenepickbe.content.vo.ContentVo;
import com.project.scenepickbe.content.vo.EpisodeVo;
import com.project.scenepickbe.content.vo.PersonVo;

@ExtendWith(MockitoExtension.class)
class ContentQueryServiceTest {

	@InjectMocks
	private ContentQueryService contentQueryService;

	@Mock
	private ContentDao contentDao;

	@Mock
	private ModelMapper modelMapper;

	@Test
	@DisplayName("작품 기본정보 조회 성공")
	void getBasicInfoSuccess() {
		Long contentId = 1L;
		ContentVo mockVo = new ContentVo();
		mockVo.setContentId(contentId);
		mockVo.setTitle("테스트 영화");
		mockVo.setPosterImageUrl("poster");
		mockVo.setSynopsis("테스트 줄거리");
		mockVo.setGenreList(List.of(GenreType.ACTION, GenreType.ROMANCE));

		when(contentDao.selectContentBasic(contentId)).thenReturn(mockVo);
		when(modelMapper.map(mockVo, ContentResponse.Basic.class))
			.thenReturn(new ContentResponse.Basic(contentId, "테스트 영화", "poster", "테스트 줄거리", List.of()));

		ContentResponse.Basic result = contentQueryService.getBasicInfo(contentId);

		assertThat(result).isNotNull();
		assertThat(result.contentId()).isEqualTo(contentId);
		assertThat(result.title()).isEqualTo("테스트 영화");
		assertThat(result.genreList())
			.hasSize(2)
			.contains(GenreType.ACTION.name(), GenreType.ROMANCE.name());
		verify(contentDao, times(1)).selectContentBasic(contentId);
		verify(modelMapper, times(1)).map(mockVo, ContentResponse.Basic.class);
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
	void getPersons() {
		Long contentId = 1L;
		PersonVo mockVo1 = new PersonVo();
		PersonVo mockVo2 = new PersonVo();

		mockVo1.setContentId(contentId);
		mockVo1.setPersonId(1L);
		mockVo1.setName("배우A");
		mockVo1.setCharName("배역A");

		mockVo2.setPersonId(2L);
		mockVo2.setContentId(contentId);
		mockVo2.setName("배우B");
		mockVo2.setCharName("배역B");

		when(contentDao.selectContentPersons(contentId)).thenReturn(List.of(mockVo1, mockVo2));
		when(modelMapper.map(mockVo1, ContentResponse.Person.class))
			.thenReturn(new ContentResponse.Person(1L, "배우A", "배역A", null));
		when(modelMapper.map(mockVo2, ContentResponse.Person.class))
			.thenReturn(new ContentResponse.Person(2L, "배우B", "배역B", null));

		ContentResponse.PersonList expected = new ContentResponse.PersonList(List.of(
			new ContentResponse.Person(1L, "배우A", "배역A", null),
			new ContentResponse.Person(2L, "배우B", "배역B", null)
		));

		ContentResponse.PersonList result = contentQueryService.getPersons(contentId);

		assertThat(result)
			.usingRecursiveComparison()
			.isEqualTo(expected);

		verify(contentDao, times(1)).selectContentPersons(contentId);
		verify(modelMapper, times(1)).map(mockVo1, ContentResponse.Person.class);
		verify(modelMapper, times(1)).map(mockVo2, ContentResponse.Person.class);
	}

	@Test
	@DisplayName("작품 에피소드 리스트 조회 성공")
	void getEpisodes() {
		Long contentId = 1L;
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

		when(contentDao.selectContentEpisodes(contentId)).thenReturn(List.of(mockVo1, mockVo2));
		when(modelMapper.map(mockVo1, ContentResponse.Episode.class))
			.thenReturn(new ContentResponse.Episode(1L, 1, "제목A", null, null));
		when(modelMapper.map(mockVo2, ContentResponse.Episode.class))
			.thenReturn(new ContentResponse.Episode(2L, 2, "제목B", null, null));

		ContentResponse.EpisodeList expected = new ContentResponse.EpisodeList(List.of(
			new ContentResponse.Episode(1L, 1, "제목A", null, null),
			new ContentResponse.Episode(2L, 2, "제목B", null, null)
		));

		ContentResponse.EpisodeList result = contentQueryService.getEpisodes(contentId);

		assertThat(result)
			.usingRecursiveComparison()
			.isEqualTo(expected);

		verify(contentDao, times(1)).selectContentEpisodes(contentId);
		verify(modelMapper, times(1)).map(mockVo1, ContentResponse.Episode.class);
		verify(modelMapper, times(1)).map(mockVo2, ContentResponse.Episode.class);
	}
}
