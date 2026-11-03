# 비밀번호 변경 (#75)

## API

`PATCH /api/v1/user/password` — 인증된 일반 회원 본인의 비밀번호를 변경한다.

```json
{"currentPassword":"현재 비밀번호","newPassword":"새 비밀번호"}
```

- 새 비밀번호는 공백이 아니고 최소 8자이며, BCrypt 저장 한도인 UTF-8 72바이트 이하여야 한다.
- 현재와 같은 비밀번호는 거부한다. 회원 ID는 요청 본문이 아닌 인증 정보에서 얻는다.
- 성공: HTTP 200, 기존 ApiResponse의 COMMON200. null 결과는 기존 직렬화 규칙에 따라 생략된다.
- 성공 시 현재 브라우저의 access_token(`/`)과 refresh_token(`/api/v1/user`) 쿠키를 만료시킨다.
- 모든 기기의 refresh token을 폐기한다. 기존 access token은 만료까지 유효하다.
- 헤더 토큰을 보관하는 클라이언트는 성공 후 자체 저장소의 토큰도 삭제해야 한다.
- 비인증: 401. 입력 오류: 400/COMMON400. 회원 없음·탈퇴: 400/USER4001.
- 현재 비밀번호 불일치: 400/USER4006. 동일 비밀번호: 400/USER4007. 소셜 전용 회원: 400/USER4008.

## 저장과 동시성

기존 USERS 및 USER_REFRESH_TOKEN 테이블을 사용하며 스키마 변경은 없다.
비밀번호 저장과 전체 refresh token 폐기는 동일 트랜잭션이다.
로그인·재발급·비밀번호 변경은 동일 회원 행을 FOR UPDATE로 잠가 발급과 폐기를 직렬화한다.
잠금 조회는 MyBatis 캐시를 비워 실행을 보장하고, 재발급은 잠금 대기 후 토큰이 폐기됐는지 다시 확인한다. 다른 회원 행은 잠그지 않는다.

## 검증

Java 21과 Docker가 필요하다. PasswordChangeIntegrationTest는 기존 테스트와 동일하게
고정 digest의 Oracle XE 이미지를 사용하고 매 실행 전용 컨테이너·스키마를 만든다.
실제 SecurityConfig, JWT, UserController, 서비스 및 MyBatis mapper를 사용한다.
외부 소셜 제공자와 운영 DB는 호출하지 않는다.

```powershell
.\gradlew.bat test --tests '*PasswordChangeIntegrationTest' --tests '*UserCommandServiceTest'
.\gradlew.bat test
.\gradlew.bat build
git diff --check
```

기본 변경·이전 비밀번호 거부·쿠키 만료·모든 기기 토큰 폐기·다른 회원 격리·기존 access token 유지,
잘못된 입력·비인증·소셜 전용 회원·회원 없음/탈퇴·토큰 폐기 실패 시 롤백·재발급 경합을 검증한다.
롤백 오류 주입과 재발급 경합의 스케줄 조정에만 mapper spy를 사용하며 SQL 자체는 실제 Oracle에서 실행한다.

Oracle MCP 실행계획 평가는 advisory다. 도구가 없으면 실행계획은 미검증으로 기록하며,
Testcontainers 검증을 MCP 평가로 대신 표기하지 않는다. 상세 근거는 로컬 build/oracle-mcp-evidence.md에 기록한다.
현재 Gradle Checkstyle 연동은 없어 통과를 주장하지 않는다(RULES.md VERIFY-005/006).
회원 탈퇴, 소셜 계정 연결, 비밀번호 찾기, 프런트엔드 화면, 배포는 범위에서 제외한다.
