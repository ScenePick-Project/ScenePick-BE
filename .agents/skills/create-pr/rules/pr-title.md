---
title: PR Title Format
impact: HIGH
tags: pr, title
---

Use `<emoji> [<type>] <summary>`. Select the type for the delivered change and the matching emoji from RULES.md.

Examples:

```text
✨ [feat] 리뷰 알림 추가
🐛 [fix] 리뷰 커서 조회 오류 수정
⚙️ [setting] Oracle MCP 검증 흐름 추가 및 TMDB 수집 정책 개선
```

Keep the summary concise and specific to the final scope. Normalize issue titles to this format instead of copying a different prefix.

Do not add status prefixes such as `[blocked]`, `[draft]`, `[Draft]`, or `Draft:`.
Use GitHub draft state and the existing issue status label (for example,
`codex-blocked`). Describe blockers and resolution conditions in the PR body;
this title policy does not waive quality gates or approvals.
