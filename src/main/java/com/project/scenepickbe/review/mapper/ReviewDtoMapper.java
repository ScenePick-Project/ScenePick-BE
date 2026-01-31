package com.project.scenepickbe.review.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.project.scenepickbe.review.dto.request.ReviewRequest;
import com.project.scenepickbe.review.dto.response.ReviewResponse;
import com.project.scenepickbe.review.vo.ReviewVo;

@Mapper(componentModel = "spring")
public interface ReviewDtoMapper {
	@Mapping(target = "contentId", source = "contentId")
	@Mapping(target = "userId", source = "userId")
	ReviewVo toVo(ReviewRequest.Create request, Long contentId, String userId);

	ReviewResponse.Created toCreated(ReviewVo reviewVo);

	ReviewResponse.Review toReview(ReviewVo reviewVo);

	List<ReviewResponse.Review> toReviewItems(List<ReviewVo> reviewVoList);

	default ReviewResponse.ReviewList toReviewList(List<ReviewVo> reviewVoList) {
		return new ReviewResponse.ReviewList(toReviewItems(reviewVoList));
	}

}
