# Architecture — Contributor Guide

This is the practical counterpart to `docs/architecture/overview.md` and `docs/android/architecture.md`.

## Repository

```
notify-later/
├── android/                 # You work here
│   ├── app/src/main/java/ao/techify/notifylater/
│   └── gradle/libs.versions.toml
├── docs/
│   ├── architecture/
│   ├── product/
│   ├── android/
│   └── contributing/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   ├── workflows/android.yml
│   ├── CODE_OF_CONDUCT.md
│   ├── CONTRIBUTING.md
│   └── PULL_REQUEST_TEMPLATE.md
├── README.md
├── LICENSE
├── SECURITY.md
└── CONTRIBUTING.md
```

## Android Packages

Inside `android/app/src/main/java/ao/techify/notifylater/`:

```
NotifyLaterApplication.kt
core/         # database, notifications, permissions, scheduler, settings
data/         # local, model, repository
domain/       # model, repository (interfaces), usecase
feature/      # onboarding, home, applications, rules, queue, settings
navigation/   # AppNavHost, routes
ui/theme/     # Material 3 theme (existing)
```

Rules:

- `core/` — no feature imports.
- `domain/` — pure Kotlin, no Android imports where possible.
- `data/` implements `domain/repository` interfaces.
- `feature/<name>/` — UI + ViewModel + Route. Expose route to `navigation/`.
- `navigation/` — single NavHost composing all routes.

See `docs/android/architecture.md` for full stack and conventions.

## Navigation Model

```
Onboarding
    ↓
Home
 ├── Applications
 │    └── Application Details
 ├── Queue
 └── Settings
```

- Onboarding is the entry point until the user completes it; then Home is the start.
- Each feature defines its own route object; `navigation/AppNavHost.kt` wires them.

## Internationalization

- `res/values/strings.xml` (English, default)
- `res/values-pt/strings.xml` (Portuguese)
- Add every user-facing string in both files; reference via `stringResource` / `getString`.
- CI does not yet lint for hard-coded strings, but reviewers will block them.

## Adding Code

1. Read `docs/product/principles.md` and `docs/product/design.md` — your change must respect them.
2. Decide the package: domain logic → `domain/`; persistence → `data/` + `core/database`; UI → `feature/<name>/`.
3. Keep changes small and focused. Avoid speculative abstractions.
4. Justify any new dependency in the PR.
5. Run `./gradlew testDebugUnitTest` from `android/`.
6. Ensure no hard-coded strings.

## Documentation

- Product docs live in `docs/product/`; architecture in `docs/architecture/`.
- Android-specific setup in `docs/android/`.
- Update docs when you change behavior that docs describe.

## Future iOS

Planned as `ios/` with Swift/SwiftUI, sharing product specs but not code. Do not introduce KMP now. Keep shared concepts in `docs/architecture/` so both platforms can reference them.
