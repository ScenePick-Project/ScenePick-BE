package com.project.scenepickbe.review.vo;

import org.apache.ibatis.type.Alias;

import com.project.scenepickbe.common.BaseVo;

import lombok.Data;

@Data
@Alias("ReviewLikeVo")
public class ReviewLikeVo extends BaseVo {

	/** 리뷰 좋아요 고유 식별자 (PK) */
	private Long reviewLikeId;

	/** 좋아요한 유저 */
	private String userId;

	/** 좋아요 대상 리뷰 */
	private Long reviewId;

}
