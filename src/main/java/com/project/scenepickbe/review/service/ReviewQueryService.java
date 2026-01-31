package com.project.scenepickbe.review.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.review.dao.ReviewDao;
import com.project.scenepickbe.review.dto.response.ReviewResponse;
import com.project.scenepickbe.review.mapper.ReviewDtoMapper;
import com.project.scenepickbe.review.vo.ReviewVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {
	private final ReviewDao reviewDao;
	private final ReviewDtoMapper reviewDtoMapper;

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
	 * 특정 작품의 리뷰 목록을 조회합니다.
	 *
	 * @param contentId 작품 ID
	 * @return 리뷰 목록 응답 DTO
	 */
	public ReviewResponse.ReviewList getReviewList(Long contentId) {
		List<ReviewVo> reviewVoList = reviewDao.selectReviewList(contentId);
		return reviewDtoMapper.toReviewList(reviewVoList);
	}
}
