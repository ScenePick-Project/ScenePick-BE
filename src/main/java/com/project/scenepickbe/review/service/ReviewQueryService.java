package com.project.scenepickbe.review.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.common.paging.CursorPaging;
import com.project.scenepickbe.review.dao.ReviewDao;
import com.project.scenepickbe.review.dto.request.ReviewRequest;
import com.project.scenepickbe.review.dto.response.ReviewResponse;
import com.project.scenepickbe.review.mapper.ReviewDtoMapper;
import com.project.scenepickbe.review.vo.ReviewVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {
	private final ReviewDao reviewDao;
	private final ReviewDtoMapper reviewDtoMapper;
	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 30;

	/**
	 * 리뷰 단건을 조회합니다.
	 *
	 * @param reviewId 조회할 리뷰 ID
	 * @return 리뷰 단건 응답 DTO
	 */
	public ReviewResponse.Review getReview(Long reviewId) {
		ReviewVo reviewVo = reviewDao.selectReview(reviewId);
		return reviewDtoMapper.toReview(reviewVo);
	}

	/**
	 * 특정 작품의 리뷰 목록을 조회합니다. (페이징)
	 *
	 * @param contentId 작품 ID
	 * @return 리뷰 목록 응답 DTO
	 */
	public ReviewResponse.SliceList getReviewList(Long contentId, ReviewRequest.Slice request) {
		LocalDateTime cursorCreatedAt = request.cursorCreatedAt();
		Long cursorReviewId = request.cursorReviewId();
		CursorPaging.validateCursorPair(cursorCreatedAt, cursorReviewId);
		int pageSize = CursorPaging.normalizeSize(request.size(), DEFAULT_SIZE, MAX_SIZE);
		int limit = CursorPaging.resolveLimit(pageSize);

		List<ReviewVo> reviewVoList = reviewDao.selectReviewCursor(contentId, cursorCreatedAt, cursorReviewId, limit);
		CursorPaging.Slice<ReviewVo, ReviewResponse.Cursor> slice = CursorPaging.toSlice(
			reviewVoList,
			pageSize,
			last -> new ReviewResponse.Cursor(last.getCreatedAt(), last.getReviewId())
		);
		return reviewDtoMapper.toSliceList(slice.items(), slice.nextCursor(), slice.hasNext());
	}
}
