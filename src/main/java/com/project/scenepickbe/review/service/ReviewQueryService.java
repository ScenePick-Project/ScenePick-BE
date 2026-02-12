package com.project.scenepickbe.review.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
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
		if (request.cursorCreatedAt() == null && request.cursorReviewId() != null
			|| request.cursorCreatedAt() != null && request.cursorReviewId() == null) {
			throw new GeneralException(ErrorStatus.CURSOR_INVALID);
		}
		int pageSize = normalizeSize(request.size());
		int limit = pageSize + 1;

		List<ReviewVo> reviewVoList = reviewDao.selectReviewCursor(contentId, cursorCreatedAt, cursorReviewId, limit);
		boolean hasNext = reviewVoList.size() > pageSize;
		if (hasNext) {
			reviewVoList = reviewVoList.subList(0, pageSize);
		}

		ReviewResponse.Cursor nextCursor = null;
		if (hasNext && !reviewVoList.isEmpty()) {
			ReviewVo last = reviewVoList.get(reviewVoList.size() - 1);
			nextCursor = new ReviewResponse.Cursor(last.getCreatedAt(), last.getReviewId());
		}
		return reviewDtoMapper.toSliceList(reviewVoList, nextCursor, hasNext);
	}

	/**
	 * 페이지 크기 요청값을 검증하고 보정합니다.
	 */
	private int normalizeSize(Integer size) {
		int resolvedSize = size == null ? DEFAULT_SIZE : size;
		if (resolvedSize <= 0) {
			throw new GeneralException(ErrorStatus.INVALID_PAGE_SIZE);
		}
		return Math.min(resolvedSize, MAX_SIZE);
	}
}
