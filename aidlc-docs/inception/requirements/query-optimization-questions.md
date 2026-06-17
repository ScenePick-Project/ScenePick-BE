# Query Optimization - Requirement Clarification Questions

기존 `ReviewQueryService.getReviewList()`의 쿼리 낭비 문제에 대한 최적화 요구사항을 명확히 하기 위한 질문입니다.

## 현재 문제 요약

`getReviewList()`에서 `CursorPaging.resolveLimit(pageSize)`로 **pageSize+1개** (예: 11개)를 DB에서 조회한 뒤,
`toSlice()` 호출 전에 11개 전부를 `enrichReviewsWithLikeDataBatch()`에 넘기고 있습니다.

즉:
- DB 조회: 11개 (hasNext 판별용 +1 포함) ← 정상
- enrich 배치 쿼리: 11개 전부 ← 낭비 (실제 응답에는 10개만 포함)

---

## Question 1
최적화 범위를 어디까지 적용할까요?

A) `getReviewList()` 서비스 메서드만 수정 (enrich 전에 pageSize개로 먼저 잘라내기)
B) `CursorPaging.toSlice()`를 수정하여 내부에서 slicing 후 enrich 콜백 방식으로 변경
C) A 적용 후, `CursorPaging.toSlice()` 제거하고 서비스에서 직접 slice 로직 인라인 처리
D) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 2
`CursorPaging.toSlice()`는 다른 서비스에서도 사용되나요? (수정 시 영향 범위 파악)

A) 아니오, ReviewQueryService에서만 사용 → toSlice() 제거 또는 수정 자유롭게 가능
B) 예, 다른 서비스에서도 사용 중 → toSlice()는 건드리지 말고 서비스 레이어만 수정
C) 잘 모르겠음 → 확인 후 결정
D) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 3
최적화 후 테스트 코드도 함께 업데이트할까요?

A) 예, 기존 ReviewQueryServiceTest를 최적화된 로직에 맞게 수정
B) 예, 수정 + 최적화 동작 검증하는 새 테스트 케이스도 추가
C) 아니오, 테스트 코드는 이번 범위에서 제외
D) Other (please describe after [Answer]: tag below)

[Answer]:B

---

모든 질문에 답변 후 "done"이라고 말씀해 주세요.
