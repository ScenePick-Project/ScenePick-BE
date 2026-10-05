package com.project.scenepickbe.mypage;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.scenepickbe.common.apiPayload.ApiResponse;
import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;
import com.project.scenepickbe.common.apiPayload.code.status.ErrorStatus;
import com.project.scenepickbe.common.swagger.DocSuccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON400 입력 오류 / PAGING4001 커서 쌍 오류", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "COMMON401 로그인 필요", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "COMMON403 탈퇴 또는 정지된 본인", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class))),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "REVIEW4001 본인 리뷰가 없거나 삭제됨", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class)))
})
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "본인만 접근. 401 인증 없음, 403 탈퇴/정지, 400 입력·커서 오류. 목록은 size 기본 10·최대 30, 생성시각과 ID 최신순. nextCursor의 두 값을 함께 전달. 빈 items와 hasNext=false는 마지막 페이지.")
public class MyPageController {
	private final MyPageService service;
	private String id(User user) {
		if (user == null) throw new GeneralException(ErrorStatus._UNAUTHORIZED);
		return user.getUsername();
	}
	@GetMapping("/profile")
	@Operation(summary = "본인 프로필 조회")
	@DocSuccess(MyPageResponse.Profile.class)
	public ApiResponse<?> profile(@AuthenticationPrincipal(errorOnInvalidType = false) User user) {
		return ApiResponse.onSuccess(service.profile(id(user)));
	}
	@PatchMapping("/profile")
	@Operation(summary = "본인 프로필 부분 수정", description = "생략은 유지, bio/imageKey null은 삭제. nickname 30자, bio 500자.")
	public ApiResponse<?> updateProfile(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@RequestBody MyPageRequest.ProfileUpdate request) {
		service.updateProfile(id(user), request); return ApiResponse.onSuccess(null);
	}
	@GetMapping("/reviews")
	@Operation(summary = "본인 작성 리뷰 최신순 목록")
	public ApiResponse<MyPageResponse.Page<MyPageResponse.ReviewItem>> reviews(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@ParameterObject MyPageRequest.Page request) { return ApiResponse.onSuccess(service.reviews(id(user), false, request)); }
	@GetMapping("/review-bookmarks")
	@Operation(summary = "북마크한 리뷰 최신순 목록")
	public ApiResponse<MyPageResponse.Page<MyPageResponse.ReviewItem>> bookmarks(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@ParameterObject MyPageRequest.Page request) { return ApiResponse.onSuccess(service.reviews(id(user), true, request)); }
	@GetMapping("/following")
	@Operation(summary = "본인의 팔로잉 목록")
	public ApiResponse<MyPageResponse.Page<MyPageResponse.UserItem>> following(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@ParameterObject MyPageRequest.Page request) { return ApiResponse.onSuccess(service.follows(id(user), false, request)); }
	@GetMapping("/followers")
	@Operation(summary = "본인의 팔로워 목록")
	public ApiResponse<MyPageResponse.Page<MyPageResponse.UserItem>> followers(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@ParameterObject MyPageRequest.Page request) { return ApiResponse.onSuccess(service.follows(id(user), true, request)); }
	@GetMapping("/track-bookmarks")
	@Operation(summary = "내 앨범의 곡 최신순 목록")
	public ApiResponse<MyPageResponse.Page<MyPageResponse.TrackItem>> tracks(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@ParameterObject MyPageRequest.Page request) { return ApiResponse.onSuccess(service.tracks(id(user), request)); }
	@GetMapping("/rating-statistics")
	@Operation(summary = "본인 유효 리뷰 별점 분포", description = "미입력·삭제 리뷰 제외. 0.5~5점 10구간과 평가 리뷰 총수.")
	@DocSuccess(MyPageResponse.Ratings.class)
	public ApiResponse<?> ratings(@AuthenticationPrincipal(errorOnInvalidType = false) User user) {
		return ApiResponse.onSuccess(service.ratings(id(user)));
	}
	@PatchMapping("/reviews/{reviewId}/rating")
	@Operation(summary = "본인 리뷰 별점 수정", description = "0.5~5점, 0.5 간격. null은 삭제.")
	public ApiResponse<?> rate(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@PathVariable long reviewId, @RequestBody MyPageRequest.Rating request) {
		service.rate(id(user), reviewId, request.rating()); return ApiResponse.onSuccess(null);
	}
	@PutMapping("/following/{targetId}")
	@Operation(summary = "팔로우 추가", description = "재요청은 멱등. 자기 자신·탈퇴/정지 대상 불가.")
	public ApiResponse<?> follow(@AuthenticationPrincipal(errorOnInvalidType = false) User user, @PathVariable String targetId) {
		service.follow(id(user), targetId, false); return ApiResponse.onSuccess(null);
	}
	@DeleteMapping("/following/{targetId}")
	@Operation(summary = "팔로우 해제")
	public ApiResponse<?> unfollow(@AuthenticationPrincipal(errorOnInvalidType = false) User user, @PathVariable String targetId) {
		service.follow(id(user), targetId, true); return ApiResponse.onSuccess(null);
	}
	@PutMapping("/review-bookmarks/{reviewId}")
	@Operation(summary = "리뷰 북마크 추가", description = "좋아요와 별개. 재요청은 멱등.")
	public ApiResponse<?> bookmark(@AuthenticationPrincipal(errorOnInvalidType = false) User user, @PathVariable long reviewId) {
		service.bookmarkReview(id(user), reviewId, false); return ApiResponse.onSuccess(null);
	}
	@DeleteMapping("/review-bookmarks/{reviewId}")
	@Operation(summary = "리뷰 북마크 해제")
	public ApiResponse<?> unbookmark(@AuthenticationPrincipal(errorOnInvalidType = false) User user, @PathVariable long reviewId) {
		service.bookmarkReview(id(user), reviewId, true); return ApiResponse.onSuccess(null);
	}
	@PostMapping("/track-bookmarks")
	@Operation(summary = "곡 북마크 저장", description = "trackId별 멱등. 곡명·아티스트는 사용자 제공 스냅샷이며 외부 조회하지 않습니다.")
	public ApiResponse<?> bookmarkTrack(@AuthenticationPrincipal(errorOnInvalidType = false) User user,
		@Valid @RequestBody MyPageRequest.Track request) {
		service.bookmarkTrack(id(user), request); return ApiResponse.onSuccess(null);
	}
	@DeleteMapping("/track-bookmarks/{trackId}")
	@Operation(summary = "곡 북마크 해제")
	public ApiResponse<?> unbookmarkTrack(@AuthenticationPrincipal(errorOnInvalidType = false) User user, @PathVariable String trackId) {
		service.unbookmarkTrack(id(user), trackId); return ApiResponse.onSuccess(null);
	}
}
