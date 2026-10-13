# 리뷰 알림 (#69)

승인된 범위: Oracle V5 알림 테이블·본인 접근 API·리뷰 좋아요/북마크 추가 트랜잭션 내 알림 저장. 기준 origin/dev 4cecaa3. 사용자 계획 승인 및 사람이 지정한 codex-ready를 GitHub에서 확인했다.

- GET /api/v1/me/notifications: items(notificationId, type REVIEW_LIKE/REVIEW_BOOKMARK, reviewId, contentId, actorNickname, createdAt, readAt), nextCursor(createdAt,id), hasNext. size 기본10/최대30. cursorCreatedAt/cursorId는 함께 전달한다.
- GET /api/v1/me/notifications/unread-count: {count}.
- PATCH /api/v1/me/notifications/{id}/read: 본인 알림 멱등 읽음 처리, 결과 null.
- 401 비로그인, 403 비활성 계정/타인 또는 없는 알림 ID, 400 잘못된 size/커서. 기존 ApiResponse 래퍼 사용.

관계의 신규 INSERT만 알림을 생성한다. 본인 행동은 SQL에서 제외한다. 취소는 알림을 제거하지 않고 재등록은 새 알림을 만든다. 중복 관계의 유일 제약으로 동시 추가를 보호한다. 알림 저장 실패는 관계 저장까지 롤백한다. 삭제된 리뷰는 목록에 남지만 reviewId/contentId가 null이므로 이동할 수 없다. 탈퇴/정지 행위자는 '탈퇴한 사용자'로 표시한다.

확인: NotificationServiceTest의 접근 제한/커서/중복·취소·저장 오류 검증, MyPageIntegrationTest의 Oracle V5 적용·읽음/개수·타인 접근·삭제 리뷰·rollback·동시 북마크 검증. 전체 gradlew test/build 결과는 handoff에 기록한다.

운영DB 적용·배포·과거 알림 backfill은 수행하지 않는다. Checkstyle Gradle 자동화가 없는 저장소의 미충족 품질 게이트는 RULES.md에 따라 그대로 기록한다.
