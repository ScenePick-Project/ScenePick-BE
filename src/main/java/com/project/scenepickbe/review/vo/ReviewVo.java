package com.project.scenepickbe.review.vo;

import org.apache.ibatis.type.Alias;

import com.project.scenepickbe.common.BaseVo;

import lombok.Data;

@Data
@Alias("ReviewVo")
public class ReviewVo extends BaseVo {

	/** 리뷰 고유 식별자 (PK) */
	private Long reviewId;

	/** 리뷰 대상 작품 */
	private Long contentId;

	/** 리뷰 작성 유저 */
	private String userId;

	/** 리뷰 본문 */
	private String reviewBody;

	/** 첨부 영상 시작 시간 */
	private Integer startTime;

	/** 첨부 영상 종료 시간 */
	private Integer endTime;

	/** 첨부 영상 유튜브 아이디 */
	private String youtubeId;

	/** 리뷰 스포일러 여부 */
	private Boolean isSpoiler;

	/** 첨부 트랙 아이디 */
	private String trackId;

	/** 삭제 여부 (Y,N)*/
	private String delYn;

	/** 삭제 시각 */
	private java.time.LocalDateTime deletedAt;

}
