# Requirements Document - Query Optimization: getReviewList() Enrich 낭비 제거

## Intent Analysis Summary

### User Request
`ReviewQueryService.getReviewList()`에서 발생하는 불필요한 enrich 쿼리 낭비를 최적화한다.

### Request Type
**Performance Optimization** - 기존 기능의 동작은 유지하면서 내부 실행 효율을 개선

### Scope Estimate
**Single Method** - `ReviewQueryService.getReviewList()` 메서드 내부 로직 변경 + 테스트 업데이트

### Complexity Estimate
**Simple** - 명확한 버그 패턴, 단일 파일 수정, 기존 테스트 호환성 유지 필요

---

## Problem Statement

### 현재 문제 (AS-IS)

```
DB 조회:    reviewVoList (limit=11개, pageSize+1)
                |
                v
enrichReviewsWithLikeDataBatch(reviewVoList)  ← 11개 전부 enrich
  - countReviewLikesBatch(11개 reviewId)       ← 낭비
  - selectUserLikedReviewIds(11개 reviewId)    ← 낭비
                |
                v
CursorPaging.toSlice(enrichedReviews, 10)     ← 여기서 10개로 잘라냄
```

**낭비 발생 지점**: `toSlice()`로 잘리는 마지막 1개(hasNext 판별용)에 대해 불필요한 배치 쿼리 실행

### 목표 (TO-BE)

```
DB 조회:    reviewVoList (limit=11개, pageSize+1)
                |
                v
hasNext 판별 + subList(0, 10)               ← 먼저 10개로 컷
                |
                v
enrichReviewsWithLikeDataBatch(10개만)      ← 정확히 10개만 enrich
  - countReviewLikesBatch(10개 reviewId)
  - selectUserLikedReviewIds(10개 reviewId)
                |
                v
cursor 생성 후 SliceList 반환
```

---

## Functional Requirements

### FR-OPT1: Enrich 대상을 pageSize개로 제한
**Priority**: High
**Description**: `enrichReviewsWithLikeDataBatch()` 호출 전에 `reviewVoList`를 pageSize개로 잘라낸다.

**Acceptance Criteria**:
- DB에서 조회한 `reviewVoList`가 limit(pageSize+1)개이면, enrich 전에 `subList(0, pageSize)`로 먼저 컷
- `enrichReviewsWithLikeDataBatch()`에 전달되는 리스트는 항상 pageSize 이하
- `hasNext` 판별은 `reviewVoList.size() > pageSize` 조건으로 enrich 전에 수행
- 기존 `CursorPaging.toSlice()` 호출 제거

**Business Rules**:
- 응답 결과(리뷰 목록, nextCursor, hasNext)는 최적화 전과 동일해야 함
- LATEST, POPULAR 두 sortBy 모두에 적용

### FR-OPT2: cursor 생성 로직 직접 인라인 처리
**Priority**: High
**Description**: `CursorPaging.toSlice()` 제거 후 cursor 생성을 서비스 레이어에서 직접 처리한다.

**Acceptance Criteria**:
- hasNext=true일 때 `pageVoList`의 마지막 요소(index=pageSize-1)를 cursor 소스로 사용
- POPULAR sort: `ReviewResponse.Cursor(lastVo.getCreatedAt(), lastReview.reviewId(), lastReview.likeCount())`
- LATEST sort: `ReviewResponse.Cursor(lastVo.getCreatedAt(), lastReview.reviewId(), null)`
- `enrichedReviews.indexOf(last)` O(n) 탐색 제거 → 직접 마지막 인덱스 참조

### FR-OPT3: 테스트 코드 업데이트
**Priority**: High
**Description**: 최적화된 로직에 맞게 기존 테스트를 수정하고, 최적화 동작을 검증하는 새 테스트를 추가한다.

**Acceptance Criteria**:
- 기존 11개 테스트 메서드가 최적화된 코드와 호환되도록 수정
- 새 테스트: enrich가 정확히 pageSize개에 대해서만 호출되는지 검증
  - `countReviewLikesBatch()` 호출 시 전달되는 reviewId 리스트 크기 = pageSize (hasNext=true인 경우)
  - `selectUserLikedReviewIds()` 호출 시 전달되는 reviewId 리스트 크기 = pageSize (hasNext=true인 경우)

---

## Non-Functional Requirements

### NFR-OPT1: 성능 개선
**Priority**: High
**Description**: enrich 배치 쿼리 실행 횟수를 줄여 응답 시간 단축

**Acceptance Criteria**:
- pageSize=10 기준, hasNext=true인 경우 배치 쿼리 대상이 11→10개로 감소
- 최대 pageSize=30 기준, 배치 쿼리 대상 31→30개로 감소

### NFR-OPT2: 하위 호환성 유지
**Priority**: High
**Description**: API 응답 스펙 변경 없음

**Acceptance Criteria**:
- `ReviewResponse.SliceList` 구조 동일
- `reviewList`, `nextCursor`, `hasNext` 값 동일
- LATEST/POPULAR 두 sortBy 모두 동작 동일

---

## Impacted Components

### Modified: ReviewQueryService.java
**메서드**: `getReviewList()`
**변경 내용**:
- `enrichReviewsWithLikeDataBatch(reviewVoList, ...)` 호출 전 hasNext 판별 및 subList 수행
- `CursorPaging.toSlice()` 호출 제거
- cursor 생성 인라인 처리

### Modified: ReviewQueryServiceTest.java
**변경 내용**:
- 기존 테스트 메서드: 최적화 로직과 호환되도록 수정
- 새 테스트 메서드 추가: enrich 호출 범위 검증

---

## Success Criteria

1. ✅ `getReviewList()` 호출 시 enrich 배치 쿼리 대상이 pageSize개(hasNext=true), 또는 실제 조회된 개수(hasNext=false)로 정확히 제한됨
2. ✅ API 응답 결과 동일 (reviewList, nextCursor, hasNext)
3. ✅ LATEST, POPULAR 두 sortBy 모두 정상 동작
4. ✅ 기존 11개 테스트 통과
5. ✅ 최적화 동작 검증 테스트 추가 및 통과
