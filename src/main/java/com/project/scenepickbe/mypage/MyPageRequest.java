package com.project.scenepickbe.mypage;

import java.math.BigDecimal;

public class MyPageRequest {
	@io.swagger.v3.oas.annotations.media.Schema(name = "MyPageCursorRequest")
	public record Page(Integer size,
		@org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
		java.time.LocalDateTime cursorCreatedAt, Long cursorId) {
	}
	@io.swagger.v3.oas.annotations.media.Schema(name = "MyPageRatingUpdate", description = "null은 별점을 지웁니다")
	public record Rating(BigDecimal rating) {
	}
	@io.swagger.v3.oas.annotations.media.Schema(name = "MyPageTrackBookmark")
	public record Track(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 100) String trackId,
		@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 300) String trackName,
		@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 300) String artist) {
	}
	@lombok.Getter
	@io.swagger.v3.oas.annotations.media.Schema(name = "MyPageProfileUpdate", description = "생략 값 유지. bio/imageKey null은 삭제")
	public static class ProfileUpdate {
		private String nickname;
		private String bio;
		private String imageKey;
		@com.fasterxml.jackson.annotation.JsonIgnore private boolean nicknameProvided;
		@com.fasterxml.jackson.annotation.JsonIgnore private boolean bioProvided;
		@com.fasterxml.jackson.annotation.JsonIgnore private boolean imageKeyProvided;
		public void setNickname(String value) { nickname = value; nicknameProvided = true; }
		public void setBio(String value) { bio = value; bioProvided = true; }
		public void setImageKey(String value) { imageKey = value; imageKeyProvided = true; }
	}

}
