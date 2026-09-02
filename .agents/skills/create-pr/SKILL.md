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
7. Render the PR body from `.github/pull_request_template.md`.
8. Push the current branch to the remote, using `-u` if needed.
9. Create or update the PR with `gh`, using `--draft` when draft state is
   required.
10. Return the PR URL and whether the PR is draft.

## Body rules

- Do not use `--fill` or invent a different body structure.
- Replace placeholder guidance and comment text with real content or explicit
  `없음` / `N/A`.
- Do not leave raw template placeholder text in the final PR body.
- Check only the task type boxes that are actually true.
- Check self-checklist items only when they are already true.
- If evidence artifacts exist, reference them. Otherwise summarize the executed
  validation commands and results in the Evidence section.

## Linked issue rules

Choose exactly one issue keyword in the template:

- `fixed #<issue_number>` for bug fixes or hotfixes
- `resolved #<issue_number>` for inquiry or request-style issues
- `closed #<issue_number>` for general development work

If the issue number cannot be determined from the caller, branch, or current
context, stop and ask for it before creating the PR.

## Title rules

- Prefer the issue title when it accurately matches the implemented change.
- Otherwise write a concise descriptive title for the delivered change.
- Do not use conventional-commit prefixes in the PR title.

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
- the repo template cannot be followed correctly
- the requested draft or base-branch state cannot be created accurately