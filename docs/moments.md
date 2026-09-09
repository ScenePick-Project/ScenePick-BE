# 개인 모먼트 API (#58)

리뷰 없이 유튜브 영상의 시간 구간과 메모를 개인 보관함에 저장한다.
프론트는 기존 유튜브 임베드 재생 코드를 재사용한다.

## 계약

모든 경로는 기존 로그인 인증이 필요하다. userId는 로그인 정보에서 결정한다.

| 메서드 | 경로 | 결과 |
| --- | --- | --- |
| POST | /api/v1/contents/{contentId}/moments | 생성된 momentId |
| GET | /api/v1/me/moments | 내 모먼트의 커서 목록 |
| GET | /api/v1/moments/{momentId} | 내 모먼트 상세 |
| PATCH | /api/v1/moments/{momentId} | 수정한 내 모먼트 상세 |
| DELETE | /api/v1/moments/{momentId} | 소프트 삭제, 공통 성공 응답 |

생성 본문:

```json
{
  "youtubeId": "abcdefghijk",
  "startTime": 12,
  "endTime": 18,
  "memo": "색감이 인상적인 장면"
}
```

- youtubeId는 영문 대소문자, 숫자, 밑줄, 하이픈으로 이루어진 11자리 ID다.
- 시간은 JSON 정수이며 0 <= startTime < endTime을 만족해야 한다.
- memo는 선택이다. null 또는 빈 문자열은 메모 없음으로 저장·조회한다.
- 같은 영상·같은 구간을 여러 번 저장할 수 있다.
- 영상 ID와 작품 ID는 생성 후 PATCH로 변경하지 않는다.
- PATCH에서 생략한 필드는 유지한다. 시간에 명시적인 null을 보내면 잘못된 요청이다.
- PATCH의 memo를 null 또는 빈 문자열로 보내면 기존 메모를 지운다.
- 삭제는 소프트 삭제이며 공개된 복구 API는 없다.
- 모먼트는 리뷰 ID를 참조하지 않는다. 리뷰 삭제와 생명주기가 독립적이다.

조회 result의 필드는 momentId, contentId, youtubeId, startTime, endTime, memo,
createdAt, updatedAt이다. 시각은 기존 리뷰와 같은 ISO 로컬 날짜·시간 형식이다.
응답의 youtubeId/startTime/endTime을 기존 프론트 플레이어에 전달한다.

## 목록

선택 필터: contentId, youtubeId. 두 필터를 함께 사용하면 AND로 적용한다.
기본 size=10, 최대 30. 30보다 큰 요청은 30으로 보정하고 0 이하는 거절한다.
정렬은 createdAt DESC, momentId DESC이며 수정해도 저장순은 바뀌지 않는다.

다음 페이지에는 직전 응답의 nextCursor에 있는 cursorCreatedAt에 해당하는 createdAt,
cursorMomentId에 해당하는 momentId를 쿼리로 전달한다. 두 값은 함께 사용한다.
페이지를 이어갈 때 contentId/youtubeId 필터도 동일하게 유지한다.

```json
{
  "momentList": [],
  "nextCursor": null,
  "hasNext": false
}
```

## 오류

공통 ApiResponse 오류 형식을 사용한다.

- 400: 필수값 누락, 잘못된 영상 ID·시간 구간·커서·페이지 크기.
- 401: 비로그인.
- 404: 작품이 없거나, 모먼트가 없거나 삭제되었거나 다른 사용자의 소유인 경우.
- 개인 메모는 공개 댓글/채팅에 게시하지 않는다.

## 검증

Java 21과 Docker가 필요하다. MomentIntegrationTest는 임시 Oracle XE 컨테이너에
실제 Flyway migration을 적용하고 HTTP API와 MyBatis/Oracle 경계에서 검증한다.
테스트 컨테이너는 별도 데이터베이스를 사용하며 운영 설정을 로드하지 않는다.

```powershell
.\gradlew.bat test --tests "com.project.scenepickbe.moment.MomentIntegrationTest"
.\gradlew.bat test
.\gradlew.bat build
```

리뷰 없는 저장, 재조회, 여러 구간, 메모 지우기, 부분 시간 수정, 소유자 격리,
필터·커서, 삭제 후 비노출, 리뷰 삭제와 독립성을 테스트한다.

## 승인 및 범위

2026-09-08 이슈 작업 대화에서 사용자가 DB·신규 API·소유권 처리,
HTTP 및 MyBatis/Oracle 테스트 경계, 중복 허용·소프트 삭제·PATCH 의미를 승인했다.

기존 Checkstyle Gradle 자동화 미연동에 대해 #58 한정 예외도 승인했다.
RULES.md를 변경하지 않으며 전체 test/build 및 Naver 규칙 수동 검토를 수행하고,
PR/Handoff에 자동 Checkstyle 미검증과 이번 예외를 기록한다.

변경 범위는 ScenePick-BE다. 프론트 플레이어/UI 변경, 공개 공유, 채팅,
음악 연결, 영상 파일 처리, 배포 및 운영 DB 적용은 포함하지 않는다.
