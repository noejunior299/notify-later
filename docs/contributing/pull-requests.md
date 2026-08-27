# Pull Requests — Notify Later

## Branch Naming

- `feat/short-description` — new feature or screen
- `fix/short-description` — bug fix
- `docs/short-description` — documentation only
- `chore/short-description` — build, CI, tooling
- `refactor/short-description` — no behavior change

Use lowercase, hyphens, imperative.

## Commits

- Clear, imperative subject line: `feat: add applications navigation`, `fix: handle empty queue state`.
- Keep commits logical and reviewable; avoid mixing unrelated changes.
- No secrets, no `local.properties` changes.

## Before You Open a PR

- [ ] Rebased on latest `main` and focused on one concern.
- [ ] `cd android && ./gradlew testDebugUnitTest` passes.
- [ ] No hard-coded user-facing strings — all in `res/values/strings.xml` + `values-pt/`.
- [ ] Strings present in both English and Portuguese (or marked for translator help).
- [ ] No backend, analytics, telemetry, auth, payments, or cloud deps.
- [ ] Docs updated if you changed behavior that docs describe.
- [ ] Screenshots attached if UI changed.

## PR Template

`.github/PULL_REQUEST_TEMPLATE.md` is applied automatically. Fill it out:

- **Description** — what and why, link issue with `Closes #...`
- **Type of Change**
- **Screenshots** (if UI)
- **How to Test** — steps for reviewer
- **Checklist**

## Review

- One approval required from a maintainer.
- Reviewers check: principles alignment, architecture fit, no hard-coded strings, tests, and minimal diff.
- Address comments or explain why not; push fixups and squash on merge as preferred by maintainer.

## After Merge

- Delete the feature branch.
- If you fixed an issue, verify it is closed and the fix is on `main`.

## Tips

- Keep PRs small. A 200-line PR is reviewed faster than an 800-line one.
- Separate refactors from behavior changes.
- If you are unsure, open a draft PR early for feedback.
