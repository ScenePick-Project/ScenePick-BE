package com.project.scenepickbe.review.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
	 * 리뷰 페이징 목록 조회
	 */
	List<ReviewVo> selectReviewCursor(@Param("contentId") Long contentId,
		@Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
		@Param("cursorReviewId") Long cursorReviewId,
		@Param("limit") int limit);

	/**
	 * 리뷰 삭제
	 */
	int deleteReview(Long reviewId, String userId);

	/**
	 * 리뷰 페이징 목록 조회 (인기순 정렬)
	 */
	List<ReviewVo> selectReviewCursorByPopularity(@Param("contentId") Long contentId,
		@Param("cursorLikeCount") Integer cursorLikeCount,
		@Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
		@Param("cursorReviewId") Long cursorReviewId,
		@Param("limit") int limit);
}
