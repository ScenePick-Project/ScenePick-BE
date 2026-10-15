---
name: create-pr
description: Create or update a GitHub pull request for the current branch using this repository's `.github/pull_request_template.md`. Use when asked to push and open a PR or Draft PR.
allowed-tools: Read Bash(git:*) Bash(gh:*)
metadata:
  short-description: Push the branch and open or update a PR from the repo template.
---

You push the current branch and create or update a GitHub pull request using
this repository's PR template.

## Source of truth

- Read `.github/pull_request_template.md` before drafting the PR body.
- Preserve the template's section order, headings, checkbox style, and linked
  issue format.
- If the template and this skill disagree, follow the template file.

## Workflow

1. Check current git status, branch, and remote.
2. Confirm the branch has committed work relative to its intended base.
3. Confirm whether remote mutation is already approved by the caller.
   - If not, stop and ask before `git push` or PR creation.
4. Determine the base branch.
   - Prefer an explicit caller instruction.
   - Otherwise use the branch's intended workflow base.
5. Detect whether a PR already exists for the current branch.
   - If a PR already exists, update it instead of creating a duplicate.
6. Collect the PR inputs from the issue, current diff, validation results, and
   caller context:
   - linked issue number
   - whether the issue keyword should be `closed`, `fixed`, or `resolved`
   - task type checkboxes
   - summary
   - impact and risk
   - self-checklist truth values
   - review points
   - evidence
   - whether the PR must be draft
7. If the caller supplies an Oracle MCP report, follow
   [SQL evidence handoff](../issue-to-draft-pr/references/oracle-mcp-validation.md#evidence-handoff-to-create-pr).
   Verify its run/issue/branch, fixed base SHA, final HEAD and gate decision.
   A missing/stale report returns to the caller for refresh before push; do not
   silently omit it. This handoff applies only when requested by the caller,
   not to unrelated standalone PRs or leftover files in `build/`.
8. Render the PR body from `.github/pull_request_template.md`.
9. Push the current branch to the remote, using `-u` if needed.
10. Create or update the PR with `gh`, using `--draft` when draft state is
   required.
11. Read back the PR's body, base, head commit and draft state (for example
    `gh pr view <url> --json url,body,baseRefName,headRefOid,isDraft`).
    Confirm they match the requested state and supplied evidence; fix a body
    omission within the approved PR action before reporting success.
12. Return the PR URL and whether the PR is draft.

## Body rules

- Do not use `--fill` or invent a different body structure.
- Replace placeholder guidance and comment text with real content or explicit
  `없음` / `N/A`.
- Do not leave raw template placeholder text in the final PR body.
- Check only the task type boxes that are actually true.
- Check self-checklist items only when they are already true.
- Reference relevant evidence artifacts from the current run. Otherwise summarize
  the executed validation commands and results in the Evidence section. Do not
  collect unrelated or stale artifacts merely because they exist.
- For a supplied Oracle MCP report, copy its applicability/gate status, measured
  results, interpretation, actual test results and limitations into Evidence.
  Include a before/after comparison table only for measured performance improvements.
  Use `N/A + reason` for unavailable metrics. Do not publish example numbers or
  present advisory gaps as passing measurements. A local `build/` path alone
  cannot provide evidence to GitHub reviewers; include the relevant content.
- Keep existing required quality gates and approval rules. Advisory MCP
  unavailability alone is not a new blocker, but unmet required criteria are.

## Linked issue rules

Choose exactly one issue keyword in the template:

- `fixed #<issue_number>` for bug fixes or hotfixes
- `resolved #<issue_number>` for inquiry or request-style issues
- `closed #<issue_number>` for general development work

If the issue number cannot be determined from the caller, branch, or current
context, stop and ask for it before creating the PR.

## Title rules

- Use `<emoji> [<type>] <summary>`, for example `✨ [feat] 리뷰 알림 추가`.
- Choose the type for the delivered change and its matching emoji from RULES.md.
- Reuse the issue summary only when it matches the final scope; normalize its prefix to this format.
- Do not add `[draft]`, `[Draft]`, or `Draft:` to the title. Set draft state with `--draft`.
- See [PR title format](rules/pr-title.md) for examples. Existing blocked-reporting requirements still apply.

## GitHub execution rules

- Use `GH_PAGER=cat` to avoid interactive paging in CLI output.
- Prefer `--body-file` over inline multi-line shell strings.
- Include `--base <branch>` when the workflow or caller already determined the
  correct base branch.
- If a PR already exists for the branch, update the title and body with
  `gh pr edit` instead of creating another PR.
- If draft state is required for an existing ready PR, convert it back to draft
  with `gh pr ready --undo`.

## Stop conditions

Stop instead of pushing or creating a PR when any of these happen:

- remote mutation was not approved
- there are no committed changes to open
- the linked issue number is missing
- caller-supplied SQL evidence is missing/stale or required criteria are unmet
- the repo template cannot be followed correctly
- the requested draft or base-branch state cannot be created accurately