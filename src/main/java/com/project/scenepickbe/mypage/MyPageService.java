package com.project.scenepickbe.mypage;

import java.math.BigDecimal;
import java.util.function.IntSupplier;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.paging.CursorPaging;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {
	private final MyPageDao dao;
	private final ProfileImageService images;

	private MyPageRow owner(String userId) {
		if (userId == null) throw new GeneralException(ErrorStatus._UNAUTHORIZED);
		var row = dao.profile(userId);
		if (row == null) throw new GeneralException(ErrorStatus._FORBIDDEN);
		return row;
	}
	public MyPageResponse.Profile profile(String userId) {
		var row = owner(userId);
		return new MyPageResponse.Profile(row.getUserId(), row.getNickname(), row.getBio(), row.getImageKey(),
			images.url(row.getImageKey()), row.getFollowingCount(), row.getFollowerCount());
	}
	@Transactional
	public void updateProfile(String userId, MyPageRequest.ProfileUpdate request) {
		owner(userId);
		if (request.isNicknameProvided() && (request.getNickname() == null || request.getNickname().isBlank()
			|| request.getNickname().length() > 30)) throw new GeneralException(ErrorStatus._BAD_REQUEST);
		if (request.getBio() != null && request.getBio().length() > 500) throw new GeneralException(ErrorStatus._BAD_REQUEST);
		if (request.isImageKeyProvided()) images.validate(userId, request.getImageKey());
		if (dao.updateProfile(userId, request) == 0) throw new GeneralException(ErrorStatus._FORBIDDEN);
	}
	private <T> MyPageResponse.Page<T> page(String userId, String kind, MyPageRequest.Page request,
		Function<MyPageRow, T> convert) {
		owner(userId);
		CursorPaging.validateCursorPair(request.cursorCreatedAt(), request.cursorId());
		if (request.cursorId() != null && request.cursorId() <= 0) throw new GeneralException(ErrorStatus.CURSOR_INVALID);
		int size = CursorPaging.normalizeSize(request.size(), 10, 30);
		var rows = dao.list(userId, kind, request.cursorCreatedAt(), request.cursorId(), CursorPaging.resolveLimit(size));
		var slice = CursorPaging.toSlice(rows, size, row -> new MyPageResponse.Cursor(row.getCreatedAt(), row.getId()));
		return new MyPageResponse.Page<>(slice.items().stream().map(convert).toList(), slice.nextCursor(), slice.hasNext());
	}
	public MyPageResponse.Page<MyPageResponse.ReviewItem> reviews(String userId, boolean bookmarks, MyPageRequest.Page request) {
		return page(userId, bookmarks ? "bookmarks" : "reviews", request, row -> new MyPageResponse.ReviewItem(
			row.getReviewId(), row.getContentId(), row.getTitle(), row.getReviewBody(), row.getRating(), row.getCreatedAt()));
	}
	public MyPageResponse.Page<MyPageResponse.UserItem> follows(String userId, boolean followers, MyPageRequest.Page request) {
		return page(userId, followers ? "followers" : "following", request,
			row -> new MyPageResponse.UserItem(row.getUserId(), row.getNickname()));
	}
	public MyPageResponse.Page<MyPageResponse.TrackItem> tracks(String userId, MyPageRequest.Page request) {
		return page(userId, "tracks", request, row -> new MyPageResponse.TrackItem(row.getTrackId(), row.getTrackName(), row.getArtist()));
	}
	public MyPageResponse.Ratings ratings(String userId) {
		owner(userId);
		var rows = dao.ratings(userId);
		var buckets = IntStream.rangeClosed(1, 10).mapToObj(i -> {
			BigDecimal rating = BigDecimal.valueOf(i).divide(BigDecimal.valueOf(2));
			long count = rows.stream().filter(row -> row.getRating().compareTo(rating) == 0).mapToLong(MyPageRow::getCount).sum();
			return new MyPageResponse.RatingBucket(rating, count);
		}).toList();
		return new MyPageResponse.Ratings(buckets.stream().mapToLong(MyPageResponse.RatingBucket::count).sum(), buckets);
	}
	@Transactional
	public void rate(String userId, long reviewId, BigDecimal rating) {
		owner(userId);
		com.project.scenepickbe.review.dto.request.ReviewRequest.validateRating(rating);
		if (dao.rate(userId, reviewId, rating) == 0) throw new GeneralException(ErrorStatus.REVIEW_NOT_FOUND);
	}
	private void add(IntSupplier insert) {
		try {
			if (insert.getAsInt() == 0) throw new GeneralException(ErrorStatus._BAD_REQUEST);
		} catch (DuplicateKeyException duplicate) {
			// 동일 관계의 재저장은 멱등 요청입니다. DB 유일 제약이 동시 요청도 보호합니다.
		}
	}
	@Transactional
	public void follow(String userId, String targetId, boolean remove) {
		owner(userId);
		if (userId.equals(targetId)) throw new GeneralException(ErrorStatus._BAD_REQUEST);
		if (remove) dao.unfollow(userId, targetId); else add(() -> dao.follow(userId, targetId));
	}
	@Transactional
	public void bookmarkReview(String userId, long reviewId, boolean remove) {
		owner(userId);
		if (remove) dao.unbookmarkReview(userId, reviewId); else add(() -> dao.bookmarkReview(userId, reviewId));
	}
	@Transactional
	public void bookmarkTrack(String userId, MyPageRequest.Track track) {
		owner(userId);
		add(() -> dao.bookmarkTrack(userId, track));
	}
	@Transactional
	public void unbookmarkTrack(String userId, String trackId) {
		owner(userId);
		dao.unbookmarkTrack(userId, trackId);
	}
}
