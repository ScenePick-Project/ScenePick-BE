package com.project.scenepickbe.review.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project.scenepickbe.common.paging.CursorPaging;
import com.project.scenepickbe.review.dao.ReviewDao;
import com.project.scenepickbe.review.dao.ReviewLikeDao;
import com.project.scenepickbe.review.dto.request.ReviewRequest;
import com.project.scenepickbe.review.dto.response.ReviewResponse;
import com.project.scenepickbe.review.mapper.ReviewDtoMapper;
import com.project.scenepickbe.review.vo.ReviewVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {
	private final ReviewDao reviewDao;
	private final ReviewLikeDao reviewLikeDao;
	private final ReviewDtoMapper reviewDtoMapper;
	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 30;
	private static final String SORT_LATEST = "LATEST";
	private static final String SORT_POPULAR = "POPULAR";

	/**
	 * 리뷰 단건을 조회합니다.
	 *
	 * @param reviewId 조회할 리뷰 ID
	 * @param currentUserId 현재 사용자 ID (비로그인 시 null)
	 * @return 리뷰 단건 응답 DTO
	 */
	public ReviewResponse.Review getReview(Long reviewId, String currentUserId) {
		ReviewVo reviewVo = reviewDao.selectReview(reviewId);
		return enrichReviewWithLikeData(reviewVo, currentUserId);
	}

	/**
	 * 특정 작품의 리뷰 목록을 조회합니다. (페이징)
	 *
	 * @param contentId 작품 ID
	 * @param currentUserId 현재 사용자 ID (비로그인 시 null)
	 * @return 리뷰 목록 응답 DTO
	 */
	public ReviewResponse.SliceList getReviewList(Long contentId, String currentUserId, ReviewRequest.Slice request) {
		LocalDateTime cursorCreatedAt = request.cursorCreatedAt();
		Long cursorReviewId = request.cursorReviewId();
		Integer cursorLikeCount = request.cursorLikeCount();
		String sortBy = request.sortBy() != null ? request.sortBy() : SORT_LATEST;

		CursorPaging.validateCursorPair(cursorCreatedAt, cursorReviewId);
		int pageSize = CursorPaging.normalizeSize(request.size(), DEFAULT_SIZE, MAX_SIZE);
		int limit = CursorPaging.resolveLimit(pageSize);

		List<ReviewVo> reviewVoList;
		if (SORT_POPULAR.equals(sortBy)) {
			reviewVoList = reviewDao.selectReviewCursorByPopularity(contentId, cursorLikeCount, cursorCreatedAt,
				cursorReviewId, limit);
		} else {
			reviewVoList = reviewDao.selectReviewCursor(contentId, cursorCreatedAt, cursorReviewId, limit);
		}

		if (reviewVoList.isEmpty()) {
			return new ReviewResponse.SliceList(Collections.emptyList(), null, false);
		}

		List<ReviewResponse.Review> enrichedReviews = enrichReviewsWithLikeDataBatch(reviewVoList, currentUserId);

		CursorPaging.Slice<ReviewResponse.Review, ReviewResponse.Cursor> slice = CursorPaging.toSlice(
			enrichedReviews,
			pageSize,
			last -> {
				if (SORT_POPULAR.equals(sortBy)) {
					return new ReviewResponse.Cursor(
						reviewVoList.get(enrichedReviews.indexOf(last)).getCreatedAt(),
						last.reviewId(),
						last.likeCount()
					);
				} else {
					return new ReviewResponse.Cursor(
						reviewVoList.get(enrichedReviews.indexOf(last)).getCreatedAt(),
						last.reviewId(),
						null
					);
				}
			}
		);

		return new ReviewResponse.SliceList(slice.items(), slice.nextCursor(), slice.hasNext());
	}

	/**
	 * 리뷰에 좋아요 데이터를 추가합니다.
	 *
	 * @param reviewVo 리뷰 VO
	 * @param currentUserId 현재 사용자 ID (비로그인 시 null)
	 * @return 좋아요 데이터가 추가된 리뷰 응답 DTO
	 */
	private ReviewResponse.Review enrichReviewWithLikeData(ReviewVo reviewVo, String currentUserId) {
		int likeCount = reviewLikeDao.countReviewLikes(reviewVo.getReviewId());
		Boolean isLikedByCurrentUser = null;

		if (currentUserId != null) {
			int exists = reviewLikeDao.existsReviewLike(currentUserId, reviewVo.getReviewId());
			isLikedByCurrentUser = exists > 0;
		}

		return new ReviewResponse.Review(
			reviewVo.getReviewId(),
			reviewVo.getContentId(),
			reviewVo.getUserId(),
			reviewVo.getReviewBody(),
			reviewVo.getStartTime(),
			reviewVo.getEndTime(),
			reviewVo.getYoutubeId(),
			reviewVo.getIsSpoiler(),
			reviewVo.getTrackId(),
			likeCount,
			isLikedByCurrentUser,
			reviewVo.getCreatedAt()
		);
	}

	/**
	 * 여러 리뷰에 좋아요 데이터를 일괄 추가합니다.
	 *
	 * @param reviewVoList 리뷰 VO 목록
	 * @param currentUserId 현재 사용자 ID (비로그인 시 null)
	 * @return 좋아요 데이터가 추가된 리뷰 응답 DTO 목록
	 */
	private List<ReviewResponse.Review> enrichReviewsWithLikeDataBatch(List<ReviewVo> reviewVoList,
		String currentUserId) {
		if (reviewVoList.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> reviewIds = reviewVoList.stream()
			.map(ReviewVo::getReviewId)
			.toList();

		Map<Long, Map<String, Object>> batchResult = reviewLikeDao.countReviewLikesBatch(reviewIds);
		Map<Long, Integer> likeCountMap = batchResult.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> ((BigDecimal)entry.getValue().get("LIKE_COUNT")).intValue()
			));

		Set<Long> likedReviewIds = Collections.emptySet();
		if (currentUserId != null) {
			likedReviewIds = reviewLikeDao.selectUserLikedReviewIds(currentUserId, reviewIds).stream()
				.collect(Collectors.toSet());
		}

		Set<Long> finalLikedReviewIds = likedReviewIds;
		return reviewVoList.stream()
			.map(vo -> {
				int likeCount = likeCountMap.getOrDefault(vo.getReviewId(), 0);
				Boolean isLikedByCurrentUser = currentUserId != null ? finalLikedReviewIds.contains(
					vo.getReviewId()) : null;

				return new ReviewResponse.Review(
					vo.getReviewId(),
					vo.getContentId(),
					vo.getUserId(),
					vo.getReviewBody(),
					vo.getStartTime(),
					vo.getEndTime(),
					vo.getYoutubeId(),
					vo.getIsSpoiler(),
					vo.getTrackId(),
					likeCount,
					isLikedByCurrentUser,
					vo.getCreatedAt()
				);
			})
			.toList();
	}
}
