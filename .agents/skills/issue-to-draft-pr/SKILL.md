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
3. Identify candidate test seams. For expected persistence changes, plan the
   [Oracle MCP SQL assessment](references/oracle-mcp-validation.md): affected
   queries, representative inputs, and required versus advisory status. Set performance
   targets only when the issue/user requires them. Use the project connection
   `scenepick-root` on MCP server `oracle-local`, verifying the target and access
   prerequisites in the reference before any database query.
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
- fixed point `origin/dev` and its recorded SHA
- SQL assessment scope, gate decision, and preserved baseline inputs when applicable

Implementation procedure:

1. Restate the exact change to build from the issue and the resolved
   `grilling` decisions. For performance improvements, preserve the base SQL
   and bindings before editing and capture a baseline when the approved MCP
   environment is ready. Otherwise record the missing prerequisite.
2. Use `tdd` where possible, at the pre-agreed seams.
3. Run typechecking regularly, single test files regularly, and the full test
   suite once at the end.
4. Also run any repo-required build, lint, formatter, checkstyle, migration,
   or release gates discovered earlier.
5. Recheck the full task diff for SQL/MyBatis or schema/index behavior changes.
   Follow [Oracle MCP SQL analysis](references/oracle-mcp-validation.md) before
   review: check SELECT results and actual plans. Compare before/after measurements
   only for performance improvements. Verify in-scope fixes with real mapper tests
   and repeat affected observations.
   Write `build/oracle-mcp-evidence.md`, including `not-applicable` or unavailable
   reasons when needed. Apply the reference's required/advisory gate decision;
   missing tools or skipped tests are never evidence of successful validation.
6. Create a reviewable commit on the current branch using the repo's commit
   convention and issue reference. Associate the evidence report with that HEAD
   only after verifying its measurement inputs still match.
7. Use `code-review` against the fixed point `origin/dev`.
8. Fix clear review findings that do not require a new product decision.
9. After any review fix, rerun the affected checks and the required full gates.
   Rerun affected Oracle MCP observations if SQL, bindings, mappings, schema,
   fixtures or measurement conditions changed; then create a follow-up commit.
   Refresh the evidence report and bind it to final HEAD after checking freshness.
10. Finish only when the worktree is clean, required gates are green, and the
    committed diff is ready for PR creation.

Do not pause here for another user command.

## Phase 5: PR close-out

When the `implement` procedure succeeds:

1. Confirm the branch has committed work relative to `origin/dev`.
2. Confirm required quality gates are green from the implementation phase.
   Confirm the SQL assessment report is current for the final commit and its
   required/advisory gate decision is satisfied. Advisory gaps must remain visible.
3. Summarize the implementation result, key validation commands, and commit SHAs.
4. Check whether the user already explicitly approved this push and Draft PR.
   If approval is absent, ask: `push하고 Draft PR 만들까요?` Do not ask again
   when the existing authorization covers this final remote action.
5. With that explicit approval, call the Skill tool with `create-pr`,
   passing draft PR state, the fixed base SHA, final HEAD, and the explicit
   `build/oracle-mcp-evidence.md` report path and gate decision. Have `create-pr`
   embed its results, interpretation, tests and limitations in the existing Evidence
   section; an ignored local file link alone is not sufficient.
6. If draft PR state is required, verify the created PR state is draft. If the
   available PR path cannot produce a draft automatically, stop and report that
   gap instead of silently creating the wrong PR state.
7. Verify the created PR state, base and head, and read back its body. Confirm
   Oracle MCP results/gaps are present and match the final report before success.

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
- required SQL assessment criteria are unmet, or unsafe access/cleanup remains unresolved
- the SQL evidence report is missing, stale, or contradicts the intended PR claims
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
- Oracle MCP applicability, required/advisory status, measured results and gaps in the PR
- commit SHAs created
- PR URL
- whether the PR was created as draft