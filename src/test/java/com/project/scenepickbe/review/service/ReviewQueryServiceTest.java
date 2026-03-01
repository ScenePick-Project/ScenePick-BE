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
import com.project.scenepickbe.review.dao.ReviewLikeDao;
import com.project.scenepickbe.review.dto.request.ReviewRequest;
import com.project.scenepickbe.review.dto.response.ReviewResponse;
import com.project.scenepickbe.review.mapper.ReviewDtoMapper;
import com.project.scenepickbe.review.vo.ReviewVo;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

	@Mock
	private ReviewDao reviewDao;

	@Mock
	private ReviewLikeDao reviewLikeDao;

	private ReviewQueryService reviewQueryService;

	@BeforeEach
	void setup() {
		ReviewDtoMapper mapper = Mappers.getMapper(ReviewDtoMapper.class);
		reviewQueryService = new ReviewQueryService(reviewDao, reviewLikeDao, mapper);
	}

	@Test
	@DisplayName("데이터가 없으면 빈 리스트, hasNext false 그리고 nextCursor null")
	void getReviewList_EmptyData() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, null, null, null, null);

		when(reviewDao.selectReviewCursor(any(), any(), any(), eq(4))).thenReturn(List.of());

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		assertThat(result.reviewList()).isEmpty();
		assertThat(result.hasNext()).isFalse();
		assertThat(result.nextCursor()).isNull();
	}

	@Test
	@DisplayName("마지막 페이지면 hasNext는 false이고 nextCursor는 null")
	void getReviewList_LastPage() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, null, null, null, null);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(1L, LocalDateTime.of(2026, 2, 1, 10, 0)),
			reviewVo(2L, LocalDateTime.of(2026, 2, 1, 9, 0))
		);

		when(reviewDao.selectReviewCursor(any(), any(), any(), eq(4))).thenReturn(mockVoList);
		when(reviewLikeDao.countReviewLikes(anyLong())).thenReturn(0);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		assertThat(result.reviewList()).hasSize(2);
		assertThat(result.hasNext()).isFalse();
		assertThat(result.nextCursor()).isNull();
	}

	@Test
	@DisplayName("size가 0 이하이면 예외 발생")
	void getReviewList_InvalidSize() {
		ReviewRequest.Slice request = new ReviewRequest.Slice(0, null, null, null, null);

		assertThatThrownBy(() -> reviewQueryService.getReviewList(21L, null, request))
			.isInstanceOf(GeneralException.class)
			.extracting("code")
			.isEqualTo(ErrorStatus.INVALID_PAGE_SIZE);
	}

	@Test
	@DisplayName("sortBy가 null이면 LATEST 정렬로 동작")
	void getReviewList_DefaultSortByLatest() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, null, null, null, null);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(1L, LocalDateTime.of(2026, 2, 1, 10, 0)),
			reviewVo(2L, LocalDateTime.of(2026, 2, 1, 9, 0))
		);

		when(reviewDao.selectReviewCursor(any(), any(), any(), eq(4))).thenReturn(mockVoList);
		when(reviewLikeDao.countReviewLikes(anyLong())).thenReturn(0);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		verify(reviewDao).selectReviewCursor(eq(contentId), isNull(), isNull(), eq(4));
		verify(reviewDao, never()).selectReviewCursorByPopularity(any(), any(), any(), any(), anyInt());
		assertThat(result.reviewList()).hasSize(2);
	}

	@Test
	@DisplayName("sortBy=LATEST이면 최신순 정렬 쿼리 호출")
	void getReviewList_SortByLatest() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, "LATEST", null, null, null);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(1L, LocalDateTime.of(2026, 2, 1, 10, 0)),
			reviewVo(2L, LocalDateTime.of(2026, 2, 1, 9, 0)),
			reviewVo(3L, LocalDateTime.of(2026, 2, 1, 8, 0)),
			reviewVo(4L, LocalDateTime.of(2026, 2, 1, 7, 0))
		);

		when(reviewDao.selectReviewCursor(any(), any(), any(), eq(4))).thenReturn(mockVoList);
		when(reviewLikeDao.countReviewLikes(anyLong())).thenReturn(0);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		verify(reviewDao).selectReviewCursor(eq(contentId), isNull(), isNull(), eq(4));
		verify(reviewDao, never()).selectReviewCursorByPopularity(any(), any(), any(), any(), anyInt());
		assertThat(result.reviewList()).hasSize(3);
		assertThat(result.hasNext()).isTrue();
		assertThat(result.nextCursor()).isNotNull();
		assertThat(result.nextCursor().likeCount()).isNull();
	}

	@Test
	@DisplayName("sortBy=POPULAR이면 인기순 정렬 쿼리 호출")
	void getReviewList_SortByPopular() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, "POPULAR", null, null, null);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(1L, LocalDateTime.of(2026, 2, 1, 10, 0)),
			reviewVo(2L, LocalDateTime.of(2026, 2, 1, 9, 0)),
			reviewVo(3L, LocalDateTime.of(2026, 2, 1, 8, 0))
		);

		when(reviewDao.selectReviewCursorByPopularity(any(), any(), any(), any(), eq(4))).thenReturn(mockVoList);
		when(reviewLikeDao.countReviewLikes(1L)).thenReturn(10);
		when(reviewLikeDao.countReviewLikes(2L)).thenReturn(5);
		when(reviewLikeDao.countReviewLikes(3L)).thenReturn(3);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		verify(reviewDao).selectReviewCursorByPopularity(eq(contentId), isNull(), isNull(), isNull(), eq(4));
		verify(reviewDao, never()).selectReviewCursor(any(), any(), any(), anyInt());
		assertThat(result.reviewList()).hasSize(3);
		assertThat(result.reviewList().get(0).likeCount()).isEqualTo(10);
		assertThat(result.reviewList().get(1).likeCount()).isEqualTo(5);
		assertThat(result.reviewList().get(2).likeCount()).isEqualTo(3);
	}

	@Test
	@DisplayName("sortBy=POPULAR이고 hasNext=true이면 nextCursor에 likeCount 포함")
	void getReviewList_PopularWithNextCursor() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, "POPULAR", null, null, null);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(1L, LocalDateTime.of(2026, 2, 1, 10, 0)),
			reviewVo(2L, LocalDateTime.of(2026, 2, 1, 9, 0)),
			reviewVo(3L, LocalDateTime.of(2026, 2, 1, 8, 0)),
			reviewVo(4L, LocalDateTime.of(2026, 2, 1, 7, 0))
		);

		when(reviewDao.selectReviewCursorByPopularity(any(), any(), any(), any(), eq(4))).thenReturn(mockVoList);
		when(reviewLikeDao.countReviewLikes(1L)).thenReturn(10);
		when(reviewLikeDao.countReviewLikes(2L)).thenReturn(5);
		when(reviewLikeDao.countReviewLikes(3L)).thenReturn(3);
		when(reviewLikeDao.countReviewLikes(4L)).thenReturn(2);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		assertThat(result.hasNext()).isTrue();
		assertThat(result.nextCursor()).isNotNull();
		assertThat(result.nextCursor().likeCount()).isEqualTo(3);
		assertThat(result.nextCursor().reviewId()).isEqualTo(3L);
	}

	@Test
	@DisplayName("sortBy=POPULAR이고 커서 파라미터가 있으면 해당 커서로 쿼리 호출")
	void getReviewList_PopularWithCursor() {
		Long contentId = 21L;
		LocalDateTime cursorCreatedAt = LocalDateTime.of(2026, 2, 1, 8, 0);
		Long cursorReviewId = 3L;
		Integer cursorLikeCount = 3;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, "POPULAR", cursorCreatedAt, cursorReviewId,
			cursorLikeCount);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(5L, LocalDateTime.of(2026, 2, 1, 7, 0)),
			reviewVo(6L, LocalDateTime.of(2026, 2, 1, 6, 0))
		);

		when(reviewDao.selectReviewCursorByPopularity(any(), any(), any(), any(), eq(4))).thenReturn(mockVoList);
		when(reviewLikeDao.countReviewLikes(anyLong())).thenReturn(2);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		verify(reviewDao).selectReviewCursorByPopularity(eq(contentId), eq(cursorLikeCount), eq(cursorCreatedAt),
			eq(cursorReviewId), eq(4));
		assertThat(result.reviewList()).hasSize(2);
	}

	@Test
	@DisplayName("리뷰 조회 시 좋아요 수가 enriched 되어야 함")
	void getReviewList_EnrichedWithLikeCount() {
		Long contentId = 21L;
		ReviewRequest.Slice request = new ReviewRequest.Slice(3, null, null, null, null);

		LocalDateTime createdAt1 = LocalDateTime.of(2026, 2, 1, 10, 0);
		LocalDateTime createdAt2 = LocalDateTime.of(2026, 2, 1, 9, 0);

		List<ReviewVo> mockVoList = List.of(
			reviewVo(1L, createdAt1),
			reviewVo(2L, createdAt2)
		);

		when(reviewDao.selectReviewCursor(any(), any(), any(), eq(4))).thenReturn(mockVoList);
		when(reviewLikeDao.countReviewLikes(1L)).thenReturn(15);
		when(reviewLikeDao.countReviewLikes(2L)).thenReturn(7);

		ReviewResponse.SliceList result = reviewQueryService.getReviewList(contentId, null, request);

		assertThat(result.reviewList()).hasSize(2);
		assertThat(result.reviewList().get(0).likeCount()).isEqualTo(15);
		assertThat(result.reviewList().get(1).likeCount()).isEqualTo(7);
		assertThat(result.reviewList().get(0).createdAt()).isEqualTo(createdAt1);
		assertThat(result.reviewList().get(1).createdAt()).isEqualTo(createdAt2);
		verify(reviewLikeDao, times(2)).countReviewLikes(anyLong());
	}

	private ReviewVo reviewVo(Long reviewId, LocalDateTime createdAt) {
		ReviewVo vo = new ReviewVo();
		vo.setReviewId(reviewId);
		vo.setCreatedAt(createdAt);
		return vo;
	}
}

