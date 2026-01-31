package com.project.scenepickbe.review.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.scenepickbe.review.vo.ReviewVo;

@Mapper
public interface ReviewDao {

	/**
	 * 리뷰 등록
	 */
	int insertReview(ReviewVo vo);

	/**
	 * 리뷰 단건 조회
	 */
	ReviewVo selectReview(Long reviewId);

	/**
	 * 리뷰 목록 조회
	 */
	List<ReviewVo> selectReviewList(Long contentId);

	/**
	 * 리뷰 삭제
	 */
	int deleteReview(Long reviewId, String userId);
}
