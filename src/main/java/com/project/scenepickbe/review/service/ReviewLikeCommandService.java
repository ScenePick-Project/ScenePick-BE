package com.project.scenepickbe.review.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.review.dao.ReviewDao;
import com.project.scenepickbe.review.dao.ReviewLikeDao;
import com.project.scenepickbe.review.dto.response.ReviewLikeResponse;
import com.project.scenepickbe.review.vo.ReviewLikeVo;
import com.project.scenepickbe.review.vo.ReviewVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewLikeCommandService {

	private final ReviewLikeDao reviewLikeDao;
	private final ReviewDao reviewDao;

	/**
	 * 리뷰 좋아요 토글 (좋아요 추가/취소)
	 * @param reviewId 리뷰 ID
	 * @param userId 유저 ID
	 * @return 좋아요 토글 결과
	 */
	@Transactional
	public ReviewLikeResponse.Toggle toggleReviewLike(Long reviewId, String userId) {
		ReviewVo review = reviewDao.selectReview(reviewId);
		if (review == null) {
			throw new GeneralException(ErrorStatus.REVIEW_NOT_FOUND);
		}

		if (review.getUserId().equals(userId)) {
			throw new GeneralException(ErrorStatus._BAD_REQUEST);
		}

		int exists = reviewLikeDao.existsReviewLike(userId, reviewId);
		boolean isLiked;

		if (exists > 0) {
			reviewLikeDao.deleteReviewLike(userId, reviewId);
			isLiked = false;
		} else {
			ReviewLikeVo reviewLikeVo = new ReviewLikeVo();
			reviewLikeVo.setUserId(userId);
			reviewLikeVo.setReviewId(reviewId);
			reviewLikeDao.insertReviewLike(reviewLikeVo);
			isLiked = true;
		}

		int likeCount = reviewLikeDao.countReviewLikes(reviewId);

		return new ReviewLikeResponse.Toggle(reviewId, isLiked, likeCount);
	}
}
