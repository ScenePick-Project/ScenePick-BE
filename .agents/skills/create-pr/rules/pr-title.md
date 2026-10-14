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

Do not add `[draft]`, `[Draft]`, or `Draft:` to the title. Draft is a GitHub PR state set with `--draft`, not a title prefix. Preserve any separately required blocked reporting.
