package com.project.scenepickbe.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.review.dao.ReviewDao;
import com.project.scenepickbe.review.dto.request.ReviewRequest;
import com.project.scenepickbe.review.dto.response.ReviewResponse;
import com.project.scenepickbe.review.mapper.ReviewDtoMapper;
import com.project.scenepickbe.review.vo.ReviewVo;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

	@Mock
	private ReviewDao reviewDao;

	private ReviewQueryService reviewQueryService;

	@BeforeEach
	void setup() {
		ReviewDtoMapper mapper = Mappers.getMapper(ReviewDtoMapper.class);
		reviewQueryService = new ReviewQueryService(reviewDao, mapper);
	}

	@Test
	@DisplayName("데이터가 없으면 빈 리스트, hasNext false 그리고 nextCursor null")
	void getReviewList_EmptyData() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, null, null);

		when(reviewDao.selectReviewCursor(any(), any(), any(), eq(4))).thenReturn(List.of());

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, request);

		assertThat(result.reviewList()).isEmpty();
		assertThat(result.hasNext()).isFalse();
		assertThat(result.nextCursor()).isNull();
	}

	@Test
	@DisplayName("마지막 페이지면 hasNext는 false이고 nextCursor는 null")
	void getReviewList_LastPage() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, null, null);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(1L, LocalDateTime.of(2026, 2, 1, 10, 0)),
			reviewVo(2L, LocalDateTime.of(2026, 2, 1, 9, 0))
		);

		when(reviewDao.selectReviewCursor(any(), any(), any(), eq(4))).thenReturn(mockVoList);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, request);

		assertThat(result.reviewList()).hasSize(2);
		assertThat(result.hasNext()).isFalse();
		assertThat(result.nextCursor()).isNull();
	}

	@Test
	@DisplayName("size가 0 이하이면 예외 발생")
	void getReviewList_InvalidSize() {
		ReviewRequest.Slice request = new ReviewRequest.Slice(0, null, null);

		assertThatThrownBy(() -> reviewQueryService.getReviewList(21L, request))
			.isInstanceOf(GeneralException.class)
			.extracting("code")
			.isEqualTo(ErrorStatus.INVALID_PAGE_SIZE);
	}

	private ReviewVo reviewVo(Long reviewId, LocalDateTime createdAt) {
		ReviewVo vo = new ReviewVo();
		vo.setReviewId(reviewId);
		vo.setCreatedAt(createdAt);
		return vo;
	}
}
