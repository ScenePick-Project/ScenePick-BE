---
title: PR Description Format
impact: MEDIUM
tags: pr, description, concise
---

Write for a reviewer who has not read the conversation. Preserve the repository
PR template's section headings, order and checkbox style.

- Lead Summary with the concrete problem and resulting behavior in 1–2 sentences.
- For several behavior changes, use a `대상 | 변경 전 | 변경 후` table. A simple
  change needs only a short paragraph; do not force a table.
- Put validation in an `항목 | 결과` table. Distinguish pass, fail, skipped and
  unverified results, and fixture tests from real integration. Include test counts
  and material skip reasons. Put long commands or logs in `<details>` when useful.
- Keep prerequisite PRs, rollout order, data impact and remaining actions visible
  in Impact & Risk. For blocked work, state the blocker and resolution condition
  near the summary without a `[blocked]` title prefix. Do not hide risks in details.
- Limit Review Points to logic or contracts that need attention. Avoid repeating
  the same explanation across Summary, Impact, Review Points and Evidence.
- Omit conversation history, tool calls, abandoned attempts, local paths and
  commit/base bookkeeping from the body; keep those in the handoff. If an approval
  or policy exception affects review, summarize its scope in one line.
- Preserve required evidence and limitations, including supplied Oracle MCP
  findings. Brevity must not turn missing checks into passing results.
- Rewrite the title and body around the final scope when the change grows. After
  saving, read back the body and check tables, line breaks, links, duplication,
  blockers and unverified items.

Example validation table (replace with actual results):

| 항목 | 결과 |
| --- | --- |
| 회귀 테스트 | 19개 통과 |
| 모바일 UI | fixture API로 확인 |
| 실제 배포 API | 미검증 |
