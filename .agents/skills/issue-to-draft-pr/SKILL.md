---
name: issue-to-draft-pr
description: Turn one GitHub issue URL into a draft-PR workflow.
disable-model-invocation: true
---

Top-level workflow for one GitHub issue.

The user gives the issue URL once. After that, the only human loop is:

- answering `grilling` questions
- explicit safety or approval gates
- the final `push` and Draft PR confirmation

Do not ask the user to invoke another slash command, restate the issue, or
resume the workflow manually after `grilling`.

## Required dependencies

- vendored upstream `grilling`
- vendored upstream `implement`
- vendored upstream `tdd`
- vendored upstream `code-review`
- vendored upstream `create-branch`
- vendored upstream `create-pr`

Keep repository-specific policy in repo docs such as `RULES.md`, `AGENTS.md`,
or `CLAUDE.md`, not in this skill.

## Phase 1: intake

1. Validate that the input is a GitHub issue URL.
2. Read the issue with `gh issue view <url> --json number,title,body,labels,url`.
3. Confirm the issue has the `codex-ready` label.
4. Confirm the issue body contains concrete acceptance criteria.
5. Read `RULES.md`, `AGENTS.md`, and `CLAUDE.md` if they exist.
6. Read any repo docs that define build, test, lint, checkstyle, migration, or
   release rules.

If the issue is not ready, stop and report the missing gate exactly.

## Phase 2: analysis, seams, and approvals

1. Summarize the requested change and list the acceptance criteria.
2. Identify unresolved behavior decisions.
3. Identify candidate test seams.
4. Identify changes that require human approval before implementation:
   - database schema or data migration
   - public or external API contract changes
   - infra, deployment, auth, secrets, permissions, or destructive operations
5. If one of those changes is required and the approval is not already explicit
   in the issue or repo rules, stop and ask for approval.
6. If requirements, behavior, or test seams are still unclear, call the Skill
   tool with `grilling`.

The `grilling` round inside this workflow must settle:

- product or behavior ambiguities
- edge cases needed for acceptance
- pre-agreed seams for any TDD work that will follow

When the user answers the `grilling` questions and shared understanding is
reached, continue this same workflow automatically. Do not ask for a new
command.

If `grilling` cannot settle a required decision or seam, stop and report the
blocker.

## Phase 3: workspace preparation

1. Fetch the latest `origin/dev`.
2. Confirm `origin/dev` exists and is usable as the workflow base.
3. Create an isolated sibling worktree from the latest `origin/dev`.
4. Call the Skill tool with `create-branch`.
5. Verify the resulting branch is appropriate for this issue workflow before
   implementation begins.

Never silently continue from an obviously wrong base branch.

## Phase 4: automatic implementation

Follow the vendored upstream `implement` skill as the implementation procedure
for this workflow.

Because `implement` is user-invoked, do not stop and ask the user to run
`/implement`. Continue automatically in this same workflow using the gathered
context and the vendored `implement` procedure.

Implementation context:

- issue URL and number
- issue title
- acceptance criteria
- resolved decisions from `grilling`
- pre-agreed test seams
- relevant repo rule files and docs
- worktree path
- branch name
- fixed point `origin/dev`

Implementation procedure:

1. Restate the exact change to build from the issue and the resolved
   `grilling` decisions.
2. Use `tdd` where possible, at the pre-agreed seams.
3. Run typechecking regularly, single test files regularly, and the full test
   suite once at the end.
4. Also run any repo-required build, lint, formatter, checkstyle, migration,
   or release gates discovered earlier.
5. Create a reviewable commit on the current branch using the repo's commit
   convention and issue reference.
6. Use `code-review` against the fixed point `origin/dev`.
7. Fix clear review findings that do not require a new product decision.
8. After any review fix, rerun the affected checks and the required full gates,
   then create a follow-up commit if needed.
9. Finish only when the worktree is clean, required gates are green, and the
   committed diff is ready for PR creation.

Do not pause here for another user command.

## Phase 5: PR close-out

When the `implement` procedure succeeds:

1. Confirm the branch has committed work relative to `origin/dev`.
2. Confirm required quality gates are green from the implementation phase.
3. Summarize the implementation result, key validation commands, and commit SHAs.
4. Ask the user: `push하고 Draft PR 만들까요?`
5. Only if the user explicitly approves, call the Skill tool with `create-pr`,
   making clear that this workflow requires draft PR state.
6. If draft PR state is required, verify the created PR state is draft. If the
   available PR path cannot produce a draft automatically, stop and report that
   gap instead of silently creating the wrong PR state.
7. Verify the created PR state before declaring success.

Do not push or create the PR before the user explicitly approves this final
remote mutation step.

## Stop conditions

Stop instead of pushing or opening a PR when any of these happen:

- `codex-ready` is missing
- acceptance criteria are missing
- required human approval for DB, API, infra, auth, or destructive work is missing
- `grilling` did not resolve the needed decisions or seams
- `create-branch` or `create-pr` produced a result that does not fit the workflow
- the implementation phase reports unresolved P0 or P1 issues
- required tests, build, lint, or checkstyle gates are red
- self review says the current approach is wrong and needs a different design
- the user does not approve push and Draft PR creation
- draft PR creation is required but the available PR path cannot create draft state correctly

P0 and P1 mean blocker-level findings:

- P0: data loss, corruption, security, or deploy-breaking risk
- P1: clear correctness or contract failure against the issue's acceptance criteria

## Success output

When the workflow succeeds, report:

- issue number and URL
- worktree path
- branch name
- agreed seams
- validation commands run
- commit SHAs created
- PR URL
- whether the PR was created as draft