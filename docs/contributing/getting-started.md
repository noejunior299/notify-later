# Getting Started — Contributing to Notify Later

Welcome! This guide gets you from clone to a running app and a passing test suite.

## Prerequisites

- JDK 17 (AGP 9.3.1 requires it; see `docs/android/development.md`)
- Android Studio Hedgehog or newer
- Android SDK with Platform 37
- Git

## Clone

```bash
git clone https://github.com/noejunior299/notify-later.git
cd notify-later
```

## Open in Android Studio

1. Open Android Studio.
2. **File → Open** and select the `android/` directory (the Gradle project root).
3. Let Gradle sync. If it fails, check JDK 17 and SDK Platform 37.

## Build from Command Line

```bash
cd android
./gradlew assembleDebug
# APK: android/app/build/outputs/apk/debug/app-debug.apk
```

## Run

- **Emulator/device:** select `app` configuration in Android Studio and Run. `minSdk` is 29.
- The foundation app shows the navigation skeleton: Onboarding → Home → Applications → Queue → Settings. No notification functionality yet.

## Test

```bash
cd android
./gradlew testDebugUnitTest
```

All unit tests must pass before opening a PR. CI runs this on every push/PR (`.github/workflows/android.yml`).

Instrumented tests:

```bash
./gradlew connectedDebugAndroidTest  # requires emulator/device
```

## Where Things Live

| Path | Purpose |
|------|---------|
| `android/` | Android application (Kotlin + Compose). This is where you write code. |
| `docs/` | Product and architecture documentation. Read before coding. |
| `docs/product/` | Vision, principles, roadmap, design. |
| `docs/architecture/` | Overview, notification pipeline, rules engine. |
| `docs/android/` | Android architecture, development, permissions. |
| `.github/` | Issue templates, workflows, code of conduct, PR template. |

## Project Conventions at a Glance

- Application ID: `ao.techify.notifylater` (stable)
- Strings: never hard-code — use `res/values/strings.xml` + `res/values-pt/strings.xml`
- Navigation: single `NavHost` in `navigation/AppNavHost.kt`
- Code style: Kotlin official (`kotlin.code.style=official`)
- Do not add backend, analytics, telemetry, auth, payments, cloud sync in MVP

## Next Steps

- Read [Architecture](architecture.md) for where code goes.
- Read [Pull Requests](pull-requests.md) for branch, commit, and review expectations.
- Browse [Good First Issues](https://github.com/noejunior299/notify-later/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) if available.
