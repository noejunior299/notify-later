# Architecture Overview — Notify Later

## Repository Layout

```
notify-later/
├── android/                 # Android application — Kotlin, Compose, Material 3
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/java/ao/techify/notifylater/
│   └── gradle/
├── ios/                     # Planned — Swift / SwiftUI (not yet created)
├── docs/
│   ├── architecture/        # Cross-platform product architecture
│   ├── product/             # Vision, principles, roadmap, design
│   ├── android/             # Android-specific architecture & setup
│   └── contributing/        # Contributor guides
├── .github/                 # Issue templates, workflows, conduct
├── README.md
├── LICENSE
├── SECURITY.md
└── CONTRIBUTING.md
```

Android is the first platform. A future `ios/` will mirror product concepts with platform-native implementations. No Kotlin Multiplatform is introduced at this stage.

## Technology Direction

### Android

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3 + AndroidX
- **Persistence:** Room (when needed)
- **Scheduling:** WorkManager or AlarmManager as appropriate
- **Notification observation:** `NotificationListenerService` (when implemented, permission-gated)
- **Concurrency:** Kotlin Coroutines

No notification interception, scheduling, or rule engine is implemented in the foundation phase.

### iOS (Planned)

- Swift + SwiftUI + iOS notification APIs
- Shares product concepts and specifications with Android; code is not shared via KMP in early phases.

## Android Module Structure

```
ao.techify.notifylater/
├── NotifyLaterApplication.kt
├── core/
│   ├── database/
│   ├── notifications/
│   ├── permissions/
│   ├── scheduler/
│   └── settings/
├── data/
│   ├── local/
│   ├── model/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── feature/
│   ├── onboarding/
│   ├── home/
│   ├── applications/
│   ├── rules/
│   ├── queue/
│   └── settings/
└── navigation/
```

Guidance:

- `core/` — cross-cutting concerns (database setup, permission helpers, schedulers, settings storage). No feature logic.
- `data/` — local data sources and repository implementations mapping to `domain/repository` interfaces.
- `domain/` — pure Kotlin models, repository interfaces, and use cases. No Android framework dependencies if avoidable.
- `feature/` — one package per screen/flow. Each feature owns its Compose UI, ViewModel, and navigation destination.
- `navigation/` — single NavHost and route definitions. Features expose routes; navigation composes them.

Avoid unnecessary dependency injection frameworks in the foundation. Prefer constructor injection and simple composition. Introduce Hilt/Koin only when justified by scale.

## Core Principles Applied to Architecture

- **Privacy / Local First:** no backend or cloud layer. `data/` is local-only (Room, DataStore, files). No network client in the MVP.
- **Minimal:** few abstractions, no speculative generality. Add layers when a real feature needs them.
- **Testable:** `domain/` and `data/` are unit-testable without Android. `feature/` ViewModels are tested with fake repositories.
- **Contributor-friendly:** standard Gradle + Android Studio build; `./gradlew testDebugUnitTest` must pass on `main`.

## Application ID

```
ao.techify.notifylater
```

This identifier is stable. Repository remains `github.com/noejunior299/notify-later`, an open-source project under Techify's identity if later published.

## Domain Concepts (Documented, Not Yet Implemented)

- `Notification` — normalized representation of an Android notification.
- `NotificationRule` — per-app / global rule describing when to hold vs deliver.
- `DelayPolicy` — how long and under what conditions to delay.
- `DeliverySchedule` — time windows or intervals for re-delivery.
- `NotificationQueue` — ordered collection of held notifications.
- `NotificationStatus` — e.g., pending, delivered, dismissed, expired.

See `notification-pipeline.md` and `rules-engine.md` for the conceptual flow.

## Docs Map

- Product: `docs/product/vision.md`, `principles.md`, `roadmap.md`, `design.md`
- Architecture: `docs/architecture/overview.md` (this file), `notification-pipeline.md`, `rules-engine.md`
- Android: `docs/android/architecture.md`, `development.md`, `permissions.md`
- Contributing: `docs/contributing/getting-started.md`, `architecture.md`, `pull-requests.md`
