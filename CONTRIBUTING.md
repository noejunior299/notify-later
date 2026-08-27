# Contributing to Notify Later

Thank you for considering a contribution to Notify Later! This document is the entry point for contributors.

For detailed guidance, see `docs/contributing/`:

- [Getting Started](docs/contributing/getting-started.md) — clone, tooling, running, testing
- [Architecture](docs/contributing/architecture.md) — project structure and where code lives
- [Pull Requests](docs/contributing/pull-requests.md) — expectations for PRs

You can also read:

- [Android Architecture](docs/android/architecture.md)
- [Android Development Guide](docs/android/development.md)
- [Permissions](docs/android/permissions.md)

## Quick Start

```bash
git clone https://github.com/noejunior299/notify-later.git
cd notify-later
# Open android/ in Android Studio (Hedgehog or newer recommended)
```

Requirements:

- JDK 17 (required by Android Gradle Plugin 9.3.1)
- Android Studio Hedgehog or newer (or matching `agp` compatible IDE)
- Android SDK with Platform 37 and Build Tools
- Kotlin 2.2.10 (managed by Gradle version catalog)

Run:

```bash
cd android
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Open `android/` in Android Studio and run on an emulator or device (minSdk 29).

## Where to Work

- Application code: `android/`
- Product and architectural documentation: `docs/`
- Issues and workflow: `.github/`

Do not add an `ios/` implementation yet. Document iOS plans in `docs/` if needed.

## Principles

Please align contributions with the project's core principles (see [Principles](docs/product/principles.md)):

- Privacy First, Local First
- User Control
- No Dark Patterns
- Calm UI — no distraction, no gamification

## How to Contribute

1. Find or open an issue.
2. Comment to claim it if you plan to work on it.
3. Fork and create a feature branch: `feat/short-description` or `fix/short-description`.
4. Make focused, minimal changes. Avoid unrelated refactors.
5. Ensure `./gradlew testDebugUnitTest` passes and the project builds.
6. Ensure no hard-coded user-facing strings; use `res/values/strings.xml` + `res/values-pt/strings.xml`.
7. Open a pull request using the PR template.

See [Pull Requests](docs/contributing/pull-requests.md) for details on commit messages, naming, and review.

## Commit & PR Expectations

- Commits: clear, imperative subject line (e.g., `feat: add onboarding navigation`). Group logically.
- PRs: fill out the template, link the issue, describe before/after, include screenshots for UI changes.
- Keep PRs small and reviewable. One concern per PR.

## Issue Reporting

Use the issue templates:

- Bug report — Android version, device, app version, reproduction steps, expected vs actual, logs/screenshots.
- Feature request — problem, proposal, alternatives, why it matters.
- Documentation — what is missing and proposed improvement.

## Code Style

- Kotlin official code style (`kotlin.code.style=official`).
- Jetpack Compose + Material 3 conventions.
- Minimize dependencies; justify any new library.
- No analytics, backend, auth, or payments in the MVP.

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).

## Code of Conduct

This project adheres to the [Code of Conduct](.github/CODE_OF_CONDUCT.md). Please be respectful and constructive.

## Questions?

Open a documentation issue or a discussion on the repository. For security issues, see [SECURITY.md](SECURITY.md).
