# Update: Replace Dependabot with Renovate

## Date
2026-05-23

## Affected Spec
N/A — dependency automation infrastructure change (no existing feature spec).

## Reason
Renovate offers richer auto-merge control, more granular package rules, and
better Gradle/KMP support than Dependabot.  The team wants auto-merge enabled
by default for all Renovate PRs and needs parity with the existing Dependabot
labelling and approval behaviour.

## Change Description

### Removed
- `.github/dependabot.yml` — Dependabot update schedule config.
- `.github/workflows/dependabot.yml` — Dependabot automation workflow
  (labelling, approval, auto-merge).

### Added
- `.github/renovate.json5` — Renovate configuration (JSON5 format, under `.github/`).
  - Weekly schedule (Monday before 06:00).
  - `dependencies` label on every Renovate PR.
  - `production` label on direct Gradle production configurations
    (`implementation`, `api`, `compileOnly`, `runtimeOnly`,
    `annotationProcessor`, `kapt`, `classpath`) and all GitHub Actions
    updates — mirrors Dependabot's `direct:production` rule.
  - `automerge: true` + `platformAutomerge: true` — enables GitHub native
    auto-merge on every Renovate PR once required CI checks pass.

- `.github/workflows/renovate.yml` — Auto-approval workflow.
  - Triggered via `pull_request_target` (secure: runs in base-branch context,
    no untrusted code checkout).
  - Hard-guarded to `renovate[bot]` author and `zomzog/my-little-bonsai`
    repository — no other actor can trigger the approval step.
  - Minimal permissions: `contents: read`, `pull-requests: write`.
  - Single step: `gh pr review --approve` using `GITHUB_TOKEN`.

### Security posture
| Concern | Mitigation |
|---|---|
| Spoofed author | `github.event.pull_request.user.login == 'renovate[bot]'` guard |
| Wrong repo | `github.repository == 'zomzog/my-little-bonsai'` guard |
| Untrusted code execution | `pull_request_target` + no code checkout |
| Over-privileged token | Minimal `contents: read` + `pull-requests: write` only |
| Unverified merge | Auto-merge activates only after required CI checks pass |

## Migration / Impact

- **Renovate GitHub App must be installed** on the repository:
  https://github.com/apps/renovate  
  Without the app, no PRs will be opened.
- Existing open Dependabot PRs should be closed manually before enabling
  Renovate to avoid duplicate updates.
- The `production` and `dependencies` labels must exist in the repository
  (they were already present via Dependabot).
- Branch protection rules with required status checks are recommended so that
  `platformAutomerge` only fires after CI passes.
