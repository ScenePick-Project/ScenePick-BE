# 홈 추천 API 검증 (#57)

## 계약

`GET /api/v1/home/recommendations`는 기존 JWT 쿠키 인증을 유지한다.
DB의 MOVIE/TV 중 포스터 URL이 null 또는 공백뿐인 작품을 제외하고,
중복 없이 최대 10개를 무작위로 선택한다. 후보가 부족하면 있는 만큼 반환한다.

성공 응답은 기존 `ApiResponse`의 `result` 안에 다음 필드를 제공한다.

- `recommendationType`: `RANDOM`
- `contentList`: `contentId`, `title`, `posterImageUrl`, `contentType`을 가진 배열

빈 목록도 HTTP 200과 빈 배열이며, 미인증은 401, DB 오류는 기존 예외 처리에 따른 500이다.
Swagger의 Home 태그에서 성공 DTO와 인증 요구사항을 확인할 수 있다.
개인화, 배너, 순위, 내 최근 리뷰, 외부 이미지 유효성 조회는 이번 범위가 아니다.

## HTTP·서비스 테스트

Java 21로 저장소 루트에서 실행한다.

```powershell
.\gradlew.bat test --tests '*HomeControllerTest' --tests '*HomeQueryServiceTest'
```

HTTP 테스트는 실제 HomeController, HomeQueryService, SecurityConfig 및 JWT 처리 경로를 사용한다.
DB 경계는 mock으로 대체한다. OAuth 설정은 로컬 테스트 fixture이며 외부 호출하지 않는다.
정상 응답, 빈 배열, MOVIE/TV 필드 매핑, 미인증·잘못된 JWT의 401,
DB 실패 전달을 검증한다. mock 테스트로 실제 SQL을 검증했다고 판단하지 않는다.

## 실제 Oracle mapper 테스트

전용 로컬 Oracle XE 컨테이너의 빈 `HOME_TEST` 스키마를 준비한다.
운영 DB나 SSH 포트 포워딩을 사용하지 않는다. 포트는 로컬호스트에만 바인딩한다.
이미지 환경 변수는 [Oracle XE 컨테이너 문서](https://github.com/gvenzl/oci-oracle-xe)를 참고한다.

```powershell
docker run --name scenepick-home-test --detach --publish 127.0.0.1:11557:1521 --env ORACLE_PASSWORD=LocalTestOnly57 --env APP_USER=HOME_TEST --env APP_USER_PASSWORD=LocalTestOnly57 gvenzl/oracle-xe:21-slim
docker logs scenepick-home-test
```

위 비밀번호는 일회용 로컬 테스트 예시다. DB 준비가 끝난 뒤 다음을 실행한다.

```powershell
$env:SCENEPICK_TEST_ORACLE_URL='jdbc:oracle:thin:@//127.0.0.1:11557/XEPDB1'
$env:SCENEPICK_TEST_ORACLE_USER='HOME_TEST'
$env:SCENEPICK_TEST_ORACLE_PASSWORD='LocalTestOnly57'
.\gradlew.bat test --tests '*HomeContentOracleTest' --rerun-tasks
```

- URL 환경 변수가 없으면 Oracle 테스트는 건너뛴다. 전체 test 성공만으로 Oracle 검증을 주장하지 않는다.
- URL은 localhost/127.0.0.1의 XEPDB1, 사용자는 HOME_TEST만 허용한다.
- 기존 V1 migration에서 첫 CONTENT 테이블 DDL만 읽어 테스트 스키마에 생성한다. migration 파일은 수정하지 않는다.
- CONTENT가 이미 존재하면 생성 단계에서 실패하며 기존 테이블을 삭제하지 않는다.
- 테스트가 직접 생성한 CONTENT만 종료 시 삭제한다. 각 사례의 데이터는 강제 rollback한다.
- 정상 후보 0·3·15개에서 각각 0·3·10개를 반환하는지 실제 ContentMapper.xml로 검증한다.
- null, 빈 문자열, 공백, 탭·개행 포스터 및 MOVIE/TV 이외의 유형이 제외되는지 확인한다.
- ID 중복 여부와 모든 요약 필드 매핑을 검증한다. 연속 호출의 결과가 반드시 달라야 한다는 확률 의존 검증은 하지 않는다.

테스트 컨테이너 정리:

```powershell
docker stop scenepick-home-test
docker rm scenepick-home-test
```

다른 컨테이너나 볼륨은 정리하지 않는다.

## Handoff 검증

Oracle 환경 변수를 설정한 상태로 다음 필수 검증을 실행하고 테스트 보고서에서
Oracle 테스트의 실행/건너뜀 개수를 확인한다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
git diff --check
```

현재 저장소는 Gradle Checkstyle 자동화가 없다. RULES.md VERIFY-005/006에 따라
Checkstyle 통과를 주장하지 않고 정상 자동 Handoff의 미충족 품질 게이트로 기록한다.
기능 변경에서 Checkstyle 설정이나 규칙을 변경하지 않는다.
랜덤 정렬은 초기 소규모 목록을 위한 구현이며 데이터 증가 시 정렬 비용을 재평가한다.
