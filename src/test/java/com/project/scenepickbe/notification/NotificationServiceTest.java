package com.project.scenepickbe.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.mypage.MyPageDao;
import com.project.scenepickbe.mypage.MyPageRequest;
import com.project.scenepickbe.mypage.MyPageRow;
import com.project.scenepickbe.mypage.MyPageService;
import com.project.scenepickbe.mypage.ProfileImageService;
import com.project.scenepickbe.review.dao.ReviewDao;
import com.project.scenepickbe.review.dao.ReviewLikeDao;
import com.project.scenepickbe.review.service.ReviewLikeCommandService;
import com.project.scenepickbe.review.vo.ReviewVo;
import org.springframework.dao.DuplicateKeyException;

class NotificationServiceTest {
	private final NotificationDao dao = mock(NotificationDao.class);
	private final MyPageDao users = mock(MyPageDao.class);
	private final NotificationService service = new NotificationService(dao, users);

	@Test
	void requiresActiveOwnerAndCompleteCursorBeforeReading() {
		assertThatThrownBy(() -> service.unreadCount(null)).isInstanceOf(GeneralException.class);
		assertThatThrownBy(() -> service.unreadCount("banned")).isInstanceOf(GeneralException.class);
		when(users.profile("owner")).thenReturn(new MyPageRow());
		assertThatThrownBy(() -> service.list("owner", new MyPageRequest.Page(10, null, 1L)))
			.isInstanceOf(GeneralException.class);
		assertThatThrownBy(() -> service.read("owner", 77)).isInstanceOf(GeneralException.class);
		verify(dao).read("owner", 77);
		verifyNoMoreInteractions(dao);
	}

	@Test
	void pagesByLastVisibleItemAndKeepsDeletedReview() {
		when(users.profile("owner")).thenReturn(new MyPageRow());
		var time = LocalDateTime.of(2026, 10, 12, 12, 0);
		var deleted = new NotificationResponse.Item(2, "REVIEW_LIKE", null, null, "actor", time, null);
		when(dao.list("owner", null, null, 2)).thenReturn(List.of(deleted,
			new NotificationResponse.Item(1, "REVIEW_BOOKMARK", 5L, 8L, "actor", time, null)));
		var page = service.list("owner", new MyPageRequest.Page(1, null, null));
		assertThat(page.items()).containsExactly(deleted);
		assertThat(page.hasNext()).isTrue();
		assertThat(page.nextCursor().id()).isEqualTo(2);
	}

	@Test
	void bookmarkOnlyNotifiesOnNewInsert() {
		when(users.profile("actor")).thenReturn(new MyPageRow());
		var notifications = mock(NotificationService.class);
		var mypage = new MyPageService(users, mock(ProfileImageService.class), notifications);
		when(users.bookmarkReview("actor", 7)).thenReturn(1).thenThrow(new DuplicateKeyException("duplicate"));
		mypage.bookmarkReview("actor", 7, false);
		mypage.bookmarkReview("actor", 7, false);
		mypage.bookmarkReview("actor", 7, true);
		verify(notifications, times(1)).added("actor", 7, "REVIEW_BOOKMARK");
		verifyNoMoreInteractions(notifications);
	}

	@Test
	void likeOnlyNotifiesOnNewInsertAndPropagatesNotificationFailure() {
		var likes = mock(ReviewLikeDao.class);
		var reviews = mock(ReviewDao.class);
		var notifications = mock(NotificationService.class);
		var review = new ReviewVo();
		review.setUserId("owner");
		when(reviews.selectReview(7L)).thenReturn(review);
		when(likes.insertReviewLike(any())).thenReturn(1).thenThrow(new DuplicateKeyException("duplicate"));
		var commands = new ReviewLikeCommandService(likes, reviews, notifications);
		commands.toggleReviewLike(7L, "actor");
		commands.toggleReviewLike(7L, "actor");
		when(likes.existsReviewLike("actor", 7L)).thenReturn(1);
		commands.toggleReviewLike(7L, "actor");
		verify(notifications, times(1)).added("actor", 7, "REVIEW_LIKE");
		when(likes.existsReviewLike("actor", 7L)).thenReturn(0);
		doReturn(1).when(likes).insertReviewLike(any());
		doThrow(new IllegalStateException("storage failed")).when(notifications).added("actor", 7, "REVIEW_LIKE");
		assertThatThrownBy(() -> commands.toggleReviewLike(7L, "actor")).isInstanceOf(IllegalStateException.class);
	}
}
