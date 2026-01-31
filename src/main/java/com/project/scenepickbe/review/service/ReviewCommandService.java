package com.project.scenepickbe.review.service;

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
public class ReviewCommandService {
	private final ReviewDtoMapper reviewDtoMapper;
	private final ReviewDao reviewDao;

	/**
	 * 리뷰를 생성하고 생성된 리뷰 ID를 반환합니다.
	 *
	 * @param contentId 리뷰 대상 작품 ID
	 * @param userId 리뷰 작성자 ID
	 * @param request 리뷰 생성 요청 DTO
	 * @return 생성된 리뷰 ID 응답
	 */
	public ReviewResponse.Created createReview(Long contentId, String userId, ReviewRequest.Create request) {
		ReviewVo reviewVo = reviewDtoMapper.toVo(request, contentId, userId);
		reviewDao.insertReview(reviewVo);

		return reviewDtoMapper.toCreated(reviewVo);
	}

	/**
	 * 작성한 리뷰를 삭제합니다.
	 *
	 * @param reviewId 삭제할 리뷰 ID
	 * @param userId 요청자 ID
	 */
	public void deleteReview(Long reviewId, String userId) {
		int deleted = reviewDao.deleteReview(reviewId, userId);
		if (deleted == 0) {
			throw new GeneralException(ErrorStatus._FORBIDDEN);
		}
	}
}
