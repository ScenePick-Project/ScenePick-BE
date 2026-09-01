# ScenePick Backend Agent Rules

이 문서는 ScenePick 백엔드 저장소에서 Agent가 매 작업마다 따라야 하는 행동 규칙, 안전 경계 및 반복 절차의 원본이다. 프로젝트 소개나 설계 배경보다 실행 가능하고 검증 가능한 규칙을 우선한다.

## 1. 적용 범위와 규칙 강도

이 규칙은 Issue 분석, 코드 변경, 테스트, 브랜치·커밋·push, Draft PR 작성 및 Handoff에 적용한다.

- `MUST`: 반드시 준수한다. 충족하지 못하면 정상 Handoff를 할 수 없다.
- `MUST NOT`: 어떤 일반 작업 요청으로도 우회하지 않는다.
- `SHOULD`: 특별한 이유가 없으면 준수하며, 예외는 Handoff와 PR에 기록한다.
- `MAY`: 상황에 따라 선택할 수 있다.

## 2. 핵심 용어

- **Ready Issue**: `codex-ready` 라벨과 필수 요구사항을 갖추고 사람이 구현을 허가한 열린 Issue.
- **Agent Run**: Issue 하나를 읽은 시점부터 Draft PR 또는 중단 보고까지 수행하는 한 번의 작업.
- **Blocked Issue**: 결정, 권한 또는 검증 환경이 부족해 안전하게 진행할 수 없는 Issue.
- **Handoff**: 변경 내용, 검증 결과 및 남은 위험을 사람에게 넘기는 단계.
- **Done**: Draft PR이나 코드 작성 완료가 아니라 사람이 승인하고 PR을 병합한 상태.

## 3. 지침 우선순위

충돌 시 다음 순서로 판단한다.

1. 시스템 안전 및 권한 제한
2. 사용자가 현재 작업에서 명시적으로 내린 지시
3. 루트와 작업 경로의 `AGENTS.md`
4. 이 `RULES.md`
5. Issue 요구사항과 인수 조건
6. 기존 코드의 관행

- `SAFE-001 MUST`: DB, 보안, 데이터 보호, Git 기록 보존 및 승인 경계는 Issue 문구만으로 완화하지 않는다.
- `SAFE-002 MUST`: 충돌을 안전하게 해결할 수 없으면 변경을 시작하거나 계속하지 않고 사용자에게 보고한다.

## 4. Issue 시작 조건

Issue URL은 Agent Run 요청이며, `codex-ready` 라벨은 구현 허가다. URL을 받으면 준비 상태를 확인할 수 있지만 아래 조건을 모두 만족하기 전에는 코드를 수정하지 않는다.

- `ISSUE-001 MUST`: URL이 현재 작업 대상 저장소의 열린 Issue인지 확인한다.
- `ISSUE-002 MUST`: `codex-ready` 라벨이 있는지 확인한다.
- `ISSUE-003 MUST`: 작업 유형 라벨이 정확히 하나인지 확인한다.
- `ISSUE-004 MUST`: 목적, 기대 결과, 검증 가능한 인수 조건, 영향 범위와 제외 범위, API·DB 변경 여부가 명확한지 확인한다.
- `ISSUE-005 MUST`: 구현에 필수인 외부 자료를 실제로 읽을 수 있는지 확인한다.
- `ISSUE-006 MUST`: 사람이 결정해야 할 미해결 질문이 없어야 한다.

필수 정보가 부족하면 코드 탐색까지만 허용한다. 필요한 질문을 명확히 남기고 `codex-blocked` 상태로 Handoff한다.

## 5. GitHub 상태 라벨

- `codex-ready`: 사람이 요구사항을 검토하고 구현을 허가함.
- `codex-in-progress`: Agent Run이 진행 중임.
- `codex-blocked`: 결정, 권한 또는 환경 부족으로 중단됨.
- `codex-review`: Draft PR이 생성되어 사람의 검토를 기다림.

정상 상태 전이는 다음과 같다.

```text
codex-ready
-> codex-in-progress
-> codex-review | codex-blocked
```

- `LABEL-001 MUST`: `codex-ready`는 사람이 지정한다. Agent가 스스로 구현 허가를 부여하지 않는다.
- `LABEL-002 MUST`: Agent는 연결된 GitHub 권한이 있을 때만 나머지 상태 라벨을 변경한다.
- `LABEL-003 MUST`: 라벨 변경 권한이 없으면 성공한 것처럼 보고하지 않고 필요한 상태 변경을 사용자에게 요청한다.

## 6. Agent Run 사전 검사

다음 순서로 사전 검사를 수행한다.

1. Issue 시작 조건을 확인한다.
2. `codex-in-progress` 또는 `codex-review` 라벨이 이미 있는지 확인한다.
3. Issue를 연결한 기존 브랜치, 열린 PR 및 로컬 worktree가 있는지 확인한다.
4. DB, 인증·인가, 공개 API, 배포, 다중 저장소 등 위험 변경 여부를 판정한다.
5. 현재 작업 디렉터리와 Git 상태를 확인하고 기존 변경을 식별한다.
6. 최신 `origin/dev`를 기준으로 독립 worktree와 작업 브랜치를 준비한다.
7. 구현 계획과 검증 계획을 작성한다.
8. 가능한 경우 `codex-in-progress` 상태로 전환한 뒤 구현을 시작한다.

- `PREFLIGHT-001 MUST`: 기존 Agent Run, 브랜치, PR 또는 worktree가 발견되면 새 작업을 중복 생성하지 않는다.
- `PREFLIGHT-002 MUST`: 사용자의 기존 변경, 브랜치 또는 worktree를 덮어쓰거나 정리하지 않는다.
- `PREFLIGHT-003 MUST`: `origin/dev`를 확인하거나 갱신할 수 없으면 기준점을 추측하지 않는다.

## 7. 브랜치와 worktree

기본 브랜치 형식은 `codex/{type}/#{issue-number}-{slug}`다.

| Issue 유형 | 브랜치 예시 |
|---|---|
| `feat` | `codex/feat/#12-login-api` |
| `fix` | `codex/fix/#34-scene-filter` |
| `style` | `codex/style/#45-format-review` |
| `docs` | `codex/docs/#46-update-api-docs` |
| `refactor` | `codex/refactor/#56-review-service` |
| `test` | `codex/test/#67-review-regression` |
| `setting` | `codex/setting/#78-add-ci` |
| `chore` | `codex/chore/#89-update-wrapper` |

- `GIT-001 MUST`: 기본적으로 최신 `origin/dev`에서 독립 worktree를 생성한다.
- `GIT-002 MUST`: 여러 작업 유형 라벨이 있으면 임의로 브랜치 유형을 선택하지 않는다.
- `GIT-003 MUST`: 다른 기준 브랜치가 필요하면 생성 전에 사용자 승인을 받는다.
- `GIT-004 MUST NOT`: `dev` 또는 `main`에 직접 push하지 않는다.
- `GIT-005 MUST NOT`: force push, rebase에 의한 공유 기록 재작성 또는 기존 브랜치 삭제를 자동 수행하지 않는다.
- `GIT-006 MUST`: Draft PR 생성 후 리뷰 대응을 위해 worktree를 유지한다.
- `GIT-007 MUST`: 병합 또는 작업 취소가 확인되고 미커밋 변경이 없을 때만 worktree 정리를 제안하거나 수행한다.

## 8. 변경 범위와 구현 원칙

- `SCOPE-001 MUST`: 인수 조건을 충족하는 최소 변경만 수행한다.
- `SCOPE-002 MUST`: 버그 수정에는 실패를 재현하고 수정을 검증하는 회귀 테스트를 추가한다.
- `SCOPE-003 MUST`: 기능 추가에는 인수 조건을 검증하는 테스트를 추가한다.
- `SCOPE-004 MUST NOT`: 관련 없는 포맷팅, 이름 변경, 의존성 갱신 또는 리팩터링을 섞지 않는다.
- `SCOPE-005 MUST NOT`: 빌드 산출물, IDE 전용 파일, 비밀정보 또는 로컬 환경 파일을 커밋하지 않는다.
- `SCOPE-006 MUST`: 별도 문제는 현재 범위에 포함하지 않고 Handoff의 후속 Issue 후보로 기록한다.

코드는 Naver Checkstyle 규칙과 Java 21을 기준으로 작성한다. 데이터 전달 객체에는 적합한 경우 `record`를 사용하고, 복잡한 여러 줄 문자열에는 적합한 경우 text block을 사용한다. 기존 저장소의 탭 들여쓰기와 네이밍 관행을 유지한다.

## 9. 외부 자료와 저장소별 참고 규칙

- `SOURCE-001 MUST`: 외부 링크의 존재를 해당 내용을 검증했다는 의미로 취급하지 않는다.
- `SOURCE-002 MUST`: 필수 IA, Figma, API 또는 DB 자료에 접근할 수 없으면 추측해 구현하지 않는다.
- `SOURCE-003 MUST`: Issue의 인수 조건만으로 요구사항이 완결된 경우에만 참고 자료 접근 실패를 기록하고 진행할 수 있다.
- `DB-001 MUST`: 현재 지원 DB를 Oracle로 간주하며 확인되지 않은 MariaDB 호환성을 주장하지 않는다.
- `DB-002 MUST`: Flyway 변경 전 [`src/main/resources/db/migration/README.md`](./src/main/resources/db/migration/README.md)를 읽고 준수한다.
- `DB-003 MUST NOT`: 이미 적용된 Flyway migration 파일을 수정하지 않는다.

## 10. 반복 검증 절차

구현 중에는 변경 범위에 가까운 검증부터 실행한다.

1. 관련 단위 테스트
2. 가능한 범위의 정적 검사
3. 필요 시 컴파일

Handoff 전에는 저장소 루트에서 다음을 수행한다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

그 다음 전체 diff를 자체 리뷰하고 비밀정보, 생성 파일, 범위 이탈 및 알려진 결함이 없는지 확인한다.

- `VERIFY-001 MUST`: 실제로 실행한 명령과 결과만 보고한다.
- `VERIFY-002 MUST`: 버그 수정과 기능 추가의 테스트를 생략하지 않는다.
- `VERIFY-003 MUST`: 테스트가 현실적으로 불가능하면 임의로 통과 처리하지 않고 사유와 수동 검증 방법을 기록한다.
- `VERIFY-004 MUST`: Handoff 전에 전체 단위 테스트와 Gradle build를 실행한다.
- `VERIFY-005 MUST`: Checkstyle Gradle 연동이 구축되기 전에는 Checkstyle 통과를 주장하지 않는다.
- `VERIFY-006 MUST`: Checkstyle 자동화가 없는 현재 상태를 정상 자동 Handoff의 미충족 품질 게이트로 기록한다.

## 11. 커밋과 push

커밋 메시지는 `[emoji] [type]: [description]` 형식을 사용한다. 영문 설명은 소문자로 작성한다.

| Type | Emoji |
|---|---|
| `init` | 🎉 |
| `feat` | ✨ |
| `fix` | 🐛 |
| `style` | 🎨 |
| `docs` | 📝 |
| `refactor` | ♻️ |
| `test` | 🧪 |
| `setting` | ⚙️ |
| `chore` | 🚀 |

- `COMMIT-001 MUST`: 커밋 전에 staged diff와 포함 파일을 다시 확인한다.
- `COMMIT-002 MUST`: 하나의 커밋은 하나의 논리적 변경을 나타낸다.
- `COMMIT-003 MUST`: 원격에 push할 최종 커밋은 필수 검증을 통과해야 한다.
- `COMMIT-004 MUST NOT`: 컴파일 실패 또는 알려진 `P0`·`P1` 문제가 포함된 브랜치를 push하지 않는다.
- `COMMIT-005 MUST`: 검증 불가 상태를 예외적으로 push할 때는 안전하게 리뷰할 가치가 있는 변경이어야 하며 `[blocked]` Draft PR 정책을 따른다.

## 12. Draft PR과 Handoff

정상 Draft PR은 다음 조건을 모두 만족할 때만 생성한다.

- Issue 인수 조건 충족
- 필수 검증 통과
- 알려진 `P0` 또는 `P1` 문제 없음
- 브랜치 및 커밋 규칙 준수
- Issue와 무관한 변경 없음
- `Closes #<issue-number>` 포함
- Impact & Risk 작성
- 실제 실행한 검증 명령과 결과 기록
- 미검증 항목과 잔여 위험 명시

- `PR-001 MUST`: PR 대상 브랜치는 `dev`로 지정한다.
- `PR-002 MUST`: 저장소의 PR 템플릿을 빠짐없이 작성한다.
- `PR-003 MUST`: 정상 Draft PR 생성 후 가능한 경우 `codex-in-progress`를 제거하고 `codex-review`를 지정한다.
- `PR-004 MUST NOT`: Agent가 PR을 승인, 병합 또는 자동으로 Ready 상태로 전환하지 않는다.

Handoff는 다음 형식을 사용한다.

```md
## Summary
## Changed Files
## Acceptance Criteria
## Verification
## Impact & Risk
## Unverified Items
## Follow-up Issues
## Git State
```

`Verification`에는 실행한 명령, 성공·실패 여부 및 핵심 결과를 기록한다.

## 13. Blocked 처리

안전하게 리뷰할 수 있는 진전이 있을 때만 `[blocked]` Draft PR을 만들 수 있다. 컴파일 실패, 잘못된 접근법 또는 알려진 `P0`·`P1` 문제가 있으면 push하지 않고 진단만 Handoff한다.

- `BLOCK-001 MUST`: 막힌 단계와 정확한 원인을 기록한다.
- `BLOCK-002 MUST`: 확인한 사실, 시도한 명령과 결과, 변경 파일 및 push 여부를 기록한다.
- `BLOCK-003 MUST`: 재개에 필요한 사람의 결정·권한과 안전한 재개 방법을 기록한다.
- `BLOCK-004 MUST`: 가능한 경우 `codex-in-progress`를 제거하고 `codex-blocked`를 지정한다.
- `BLOCK-005 MUST NOT`: 작업 완료를 주장하기 위해 검증 기준이나 규칙을 낮추지 않는다.

## 14. 위험 작업과 승인

다음 작업은 계획과 영향 범위를 제시한 뒤 사용자 승인을 받아야 한다.

- DB 스키마 또는 데이터 migration
- 인증·인가와 비밀정보 처리
- 공개 API의 호환성을 깨는 변경
- 메이저 의존성 업그레이드
- 배포 또는 운영 인프라 변경
- 최초 Issue에 없던 저장소 변경
- 대규모 구조 변경
- Issue 목적과 무관한 리팩터링
- `origin/dev` 이외의 기준 브랜치 사용

- `RISK-001 MUST`: 조사 중 위험 작업이 발견되면 구현을 멈추고 계획을 다시 승인받는다.
- `RISK-002 MUST`: `codex-ready`를 무제한 변경 허가로 해석하지 않는다.

## 15. 절대 금지 사항

- `PROHIBIT-001 MUST NOT`: `dev` 또는 `main`에 직접 push한다.
- `PROHIBIT-002 MUST NOT`: PR을 승인하거나 병합한다.
- `PROHIBIT-003 MUST NOT`: 배포를 실행한다.
- `PROHIBIT-004 MUST NOT`: 운영 DB에 접근하거나 데이터를 변경한다.
- `PROHIBIT-005 MUST NOT`: GitHub Secrets 또는 저장소 비밀정보를 수정·출력한다.
- `PROHIBIT-006 MUST NOT`: force push, 기존 공유 기록 재작성 또는 사용자 작업 삭제를 수행한다.
- `PROHIBIT-007 MUST NOT`: 인증을 우회하거나 사용자에게 토큰 값을 출력하도록 요청한다.
- `PROHIBIT-008 MUST NOT`: 일반 기능 Issue에서 `AGENTS.md` 또는 `RULES.md`를 변경한다.

## 16. 심각도와 자체 리뷰

- `P0`: 데이터 손실, 보안 사고 또는 서비스 중단 가능성. PR 생성을 중단한다.
- `P1`: 주요 기능 오류 또는 호환성 파괴. 리뷰 요청과 push를 금지한다.
- `P2`: 제한된 조건의 버그 또는 유지보수 위험. Draft PR에 명시한다.
- `P3`: 개선 제안. 리뷰 참고사항으로 기록한다.

- `REVIEW-001 MUST`: Handoff 전에 전체 diff를 독립적인 리뷰 관점으로 다시 검사한다.
- `REVIEW-002 MUST`: `P0` 또는 `P1`이 남아 있으면 승인 가능한 상태로 보고하지 않는다.

## 17. 규칙 예외와 변경

- `RULE-001 MUST NOT`: 안전, 권한, 데이터 보호 또는 Git 기록 보존 규칙에 예외를 적용한다.
- `RULE-002 MUST`: 그 밖의 일회성 예외는 사용자의 명시적 승인, 구체적 범위와 이유를 요구한다.
- `RULE-003 MUST`: 허용된 예외를 Draft PR의 Impact & Risk와 Handoff에 기록한다.
- `RULE-004 MUST`: 영구 규칙 변경은 별도 `setting` Issue와 PR로 수행한다.
- `RULE-005 MUST NOT`: 기능 PR에서 작업을 통과시키기 위해 규칙을 완화하거나 변경한다.

별도 문서 버전과 changelog는 관리하지 않고 Git 이력을 사용한다. 규칙을 변경할 때는 관련 `AGENTS.md`, GitHub Issue·PR 템플릿, CI 및 오케스트레이션 스킬의 동기화 필요성을 함께 검토한다.
