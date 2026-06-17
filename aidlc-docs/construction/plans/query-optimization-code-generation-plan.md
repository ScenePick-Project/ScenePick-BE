# Code Generation Plan - Query Optimization: getReviewList() Enrich 낭비 제거

## Unit Context
- **Unit Name**: query-optimization
- **Requirements**: aidlc-docs/inception/requirements/query-optimization-requirements.md
- **Target Files**: 2개 수정 (application code)

## Stories / Requirements Coverage
- [x] FR-OPT1: enrich 전 subList(0, pageSize) + hasNext 판별
- [x] FR-OPT2: CursorPaging.toSlice() 제거, cursor 인라인 처리
- [x] FR-OPT3: 기존 테스트 수정 + 최적화 검증 테스트 추가

---

## Step 1: ReviewQueryService.java 수정
- [x] `getReviewList()` 메서드에서 enrich 전 hasNext 판별 및 subList(0, pageSize) 수행
- [x] `CursorPaging.toSlice()` 호출 제거
- [x] hasNext=true일 때만 cursor 생성 (pageVoList 마지막 요소 직접 참조)
- [x] LATEST, POPULAR 두 sortBy 모두 동작 검증
- **File**: `src/main/java/com/project/scenepickbe/review/service/ReviewQueryService.java`

## Step 2: ReviewQueryServiceTest.java 수정 및 테스트 추가
- [x] 기존 테스트 메서드들이 최적화 로직과 호환 확인 (수정 불필요)
- [x] 새 테스트: `getReviewList_EnrichCalledWithPageSizeOnly()` 추가
- [x] 새 테스트: `getReviewList_PopularEnrichCalledWithPageSizeOnly()` 추가
- **File**: `src/test/java/com/project/scenepickbe/review/service/ReviewQueryServiceTest.java`

---

## Completion Criteria
- [x] `getReviewList()` 수정 완료 (enrich 대상 pageSize개로 제한)
- [x] 기존 테스트 11개 모두 통과 (호환성 확인)
- [x] 신규 최적화 검증 테스트 2개 추가
