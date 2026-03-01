package com.project.scenepickbe.review.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.scenepickbe.review.vo.ReviewLikeVo;

@Mapper
public interface ReviewLikeDao {

	/**
	 * 리뷰 좋아요 추가
	 * @param vo 리뷰 좋아요 정보
	 * @return 생성된 reviewLikeId
	 */
	int insertReviewLike(ReviewLikeVo vo);

	/**
	 * 리뷰 좋아요 삭제
	 * @param userId 유저 ID
	 * @param reviewId 리뷰 ID
	 * @return 삭제된 행 수
	 */
	int deleteReviewLike(@Param("userId") String userId, @Param("reviewId") Long reviewId);

	/**
	 * 리뷰 좋아요 여부 확인
	 * @param userId 유저 ID
	 * @param reviewId 리뷰 ID
	 * @return 좋아요 개수 (0 또는 1)
	 */
	int existsReviewLike(@Param("userId") String userId, @Param("reviewId") Long reviewId);

	/**
	 * 리뷰별 좋아요 수 조회
	 * @param reviewId 리뷰 ID
	 * @return 좋아요 수
	 */
	int countReviewLikes(@Param("reviewId") Long reviewId);

	/**
	 * 리뷰 좋아요 정보 조회
	 * @param userId 유저 ID
	 * @param reviewId 리뷰 ID
	 * @return 리뷰 좋아요 정보
	 */
	ReviewLikeVo selectReviewLike(@Param("userId") String userId, @Param("reviewId") Long reviewId);

}
