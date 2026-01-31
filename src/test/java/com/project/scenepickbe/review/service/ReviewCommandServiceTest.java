package com.project.scenepickbe.review.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
class ReviewCommandServiceTest {

	@Mock
	private ReviewDao reviewDao;

	private ReviewCommandService reviewCommandService() {
		ReviewDtoMapper mapper = Mappers.getMapper(ReviewDtoMapper.class);
		return new ReviewCommandService(mapper, reviewDao);
	}

	@Test
	@DisplayName("리뷰 생성 시 생성된 reviewId가 응답 포함")
	void createReviewPopulatesReviewId() {
		ReviewCommandService service = reviewCommandService();

		Long contentId = 12L;
		String userId = "user123";
		ReviewRequest.Create request = new ReviewRequest.Create(
			"좋아요",
			false,
			"track123",
			"youtube123",
			10,
			20
		);

		when(reviewDao.insertReview(org.mockito.ArgumentMatchers.any(ReviewVo.class)))
			.thenAnswer(invocation -> {
				ReviewVo vo = invocation.getArgument(0);
				vo.setReviewId(999L);
				return 1;
			});

		ReviewResponse.Created result = service.createReview(contentId, userId, request);

		assertThat(result.reviewId()).isEqualTo(999L);

		verify(reviewDao, times(1)).insertReview(org.mockito.ArgumentMatchers.any(ReviewVo.class));
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReviewSuccess() {
		ReviewCommandService service = reviewCommandService();

		when(reviewDao.deleteReview(10L, "user123")).thenReturn(1);

		assertThatCode(() -> service.deleteReview(10L, "user123")).doesNotThrowAnyException();

		verify(reviewDao, times(1)).deleteReview(10L, "user123");
	}

	@Test
	@DisplayName("리뷰 삭제 실패 시 FORBIDDEN 발생")
	void deleteReviewForbidden() {
		ReviewCommandService service = reviewCommandService();

		when(reviewDao.deleteReview(10L, "user123")).thenReturn(0);

		assertThatThrownBy(() -> service.deleteReview(10L, "user123"))
			.isInstanceOf(GeneralException.class)
			.extracting("code")
			.isEqualTo(ErrorStatus._FORBIDDEN);

		verify(reviewDao, times(1)).deleteReview(10L, "user123");
	}
}
