# Android Architecture — Notify Later

## Stack

- Kotlin 2.2.10, AGP 9.3.1, Compose BOM 2026.02.01, Material 3
- `minSdk 29`, `targetSdk 37`, `compileSdk 37`
- Java 11 source/target, JDK 17 for Gradle

## Package Structure

```
ao.techify.notifylater/
├── NotifyLaterApplication.kt
├── core/
│   ├── database/        # Room database, DAOs, entities (when added)
│   ├── notifications/   # Notification helpers, channel setup, renderer (future)
│   ├── permissions/     # Permission checks and request launchers
│   ├── scheduler/       # WorkManager / AlarmManager wrappers (future)
│   └── settings/        # DataStore / SharedPreferences wrappers
├── data/
│   ├── local/           # Room DAOs, DataStore impl
│   ├── model/           # Data-layer models / mappers
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Pure Kotlin domain models
│   ├── repository/      // Interfaces
│   └── usecase/         # Use cases (one concern each)
├── feature/
│   ├── onboarding/      # Onboarding flow
│   ├── home/            # Home screen
│   ├── applications/    # App list + details
│   ├── rules/           # Rule configuration (future)
│   ├── queue/           # Queue / history (future)
│   └── settings/        # Settings, language, about
└── navigation/          # NavHost, routes, navigation helpers
```

Conventions:

- `core/` has no feature logic and no dependency on `feature/`.
- `domain/` has no Android framework import unless unavoidable; it is unit-testable on the JVM.
- `data/` depends on `domain/repository` interfaces; features depend on `domain/`.
- `feature/<name>/` owns its composables, ViewModel, and `Route` object.
- `navigation/` composes all feature routes into a single `NavHost`.

## Notification Capture Components (Phase 1 Spike)

> Phase 1 is a technical spike and not the final notification delivery implementation.

```
core/notifications/
├── NotifyLaterNotificationListener.kt  # NotificationListenerService adapter/orchestrator
├── NotificationCapture.kt             # pure capture/transformation rules (JVM-testable)
├── RawNotification                    # framework-independent input structure
├── NotificationRenderer.kt            # re-publishes captured notifications (NotificationCompat)
├── NotificationAccess.kt              # listener-access detection + system settings intent
├── CaptureLogger.kt                   # debug-build-only logging
└── NotificationHelper.kt              # channel id constants
```

Flow: service maps `StatusBarNotification` into `RawNotification`, `NotificationCapture` filters (self-notifications, blank keys) and sanitizes content into the domain `Notification`, the in-memory `NotificationRepositoryImpl` stores it for the debug section in Settings, and `NotificationRenderer` re-publishes a copy.

Safety rule enforced everywhere: **the original notification is never cancelled during Phase 1**. Any failure (capture, save, render, missing POST_NOTIFICATIONS) leaves the original untouched.

Singletons are exposed through `NotifyLaterApplicationHolder` (no DI framework). See `docs/architecture/notification-pipeline.md` for full details and limitations.

## Navigation

```
Onboarding
    ↓
Home
 ├── Applications
 │    └── Application Details
 ├── Queue
 └── Settings
```

- Single `NavHost` in `navigation/AppNavHost.kt`.
- Routes are typed objects (`@Serializable` when using Navigation Compose type-safe routes).
- Onboarding is shown once; after completion, `Home` is the start destination (persisted in settings/DataStore).
- Deep links are not needed in the MVP.

## Internationalization

- `res/values/strings.xml` — English (default/fallback).
- `res/values-pt/strings.xml` — Portuguese (Brazilian / neutral Portuguese).
- Every user-facing string must be a `stringResource` or `context.getString`. No hard-coded literals in Kotlin/Screen code. Lint is expected to flag violations.
- Language choice during onboarding and in Settings. Implementation uses Android per-app language / `AppCompatDelegate` or `LocaleManager` depending on API; foundation provides the resources and UI hook.
- Architecture allows additional `values-xx/` without code changes.

## Dependency Policy

Do not add dependencies unless justified. Avoid:

- DI frameworks (Hilt/Koin) until scale demands them — use constructor injection for now.
- Networking (Retrofit/OkHttp) — no backend in MVP.
- Analytics, crash SDKs with telemetry beyond basic crash reporting (and only if privacy-preserving and opt-in).

When adding a library, document the rationale in the PR.

## Testing

- `src/test/` — JVM unit tests for `domain/`, `data/`, ViewModels with fakes.
- `src/androidTest/` — instrumented tests only when Compose/Android framework is required.
- Run ` ./gradlew testDebugUnitTest` from `android/` and ensure `android.yml` CI stays green.

## Build

- Single-module `android/app` Gradle project. Multi-module split may come later if warranted.
- Version catalog at `gradle/libs.versions.toml`.
- Preserve generated configuration (`.gitignore`, `gradle.properties`, `settings.gradle.kts`) unless there is a reason to change.
