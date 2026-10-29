# Roadmap — Notify Later

This roadmap describes intended phases. Dates are not committed; order is intentional. Foundation before features.

## Phase 0 — Foundation

*Repository, architecture, documentation, localization, CI, contribution workflow.*

- Repository structure (`android/`, `docs/`, `.github/`)
- Android architecture skeleton (`core/`, `data/`, `domain/`, `feature/`, `navigation/`)
- Application ID `ao.techify.notifylater` locked in
- Material 3 + Jetpack Compose baseline
- Internationalization: English + Portuguese, no hard-coded strings
- Navigation foundation: Onboarding, Home, Applications, Application Details, Queue, Settings
- Documentation: vision, principles, design, architecture, permissions, contributing
- GitHub templates: bug report, feature request, documentation
- CI: `android.yml` builds and runs unit tests
- License Apache-2.0, SECURITY.md, CODE_OF_CONDUCT.md

Status: **complete**.

## Phase 1 — Notification Capture

*Observe and persist notification metadata safely.*

- `NotificationListenerService` (permission-gated, user-consented)
- Detect installed applications (package manager query) — pending
- Capture notifications, identify source application
- Persist notification metadata locally — implemented with an app-private store; Room migration pending
- Basic permission handling and onboarding for notification access

Success criteria: new notifications are captured, filtered safely, and persisted locally without hiding anything that cannot be re-posted.

Status: **implemented (MVP)** — listener, filters, persistent store, and unit tests. Installed-app detection and Room migration pending.

## Phase 2 — Delay Engine

*Rules, delay configuration, queue, scheduler, notification recreation.*

- Concepts: `Notification`, `NotificationRule`, `DelayPolicy`, `DeliverySchedule`, `NotificationQueue`, `NotificationStatus`
- Rules engine — per-app and global rules (see `docs/architecture/rules-engine.md`)
- Queue — ordered, inspectable, cancellable
- Scheduler — WorkManager / AlarmManager as appropriate (battery-aware)
- Notification renderer — re-post held notifications at scheduled time
- Pipeline: see `docs/architecture/notification-pipeline.md`

Status: **MVP implemented** — global and per-app configurable delay, persistent queue, WorkManager scheduler, re-delivery, and retry safety. Batching and queue cancellation pending.

## Phase 3 — User Experience

*Onboarding, management, and everyday use.*

- Onboarding: value proposition, permission requests (one at a time), language choice
- Application management: list, search, per-app settings, Application Details
- Rule configuration: presets and custom schedules
- Queue screen: pending, delivered, dismissed
- History and Settings: retention, clear data, language, about
- Accessibility pass and visual polish per `docs/product/design.md`

Status: **in progress** — onboarding, queue, history/logs, settings (delay + permissions), and app management with per-app delay presets implemented; custom schedules pending.

## Phase 4 — Reliability

*Make it trustworthy on real devices.*

- Battery optimization handling (doze, App Standby, OEM specifics documented)
- Edge cases: reboot, upgrade, permission revocation, notification channels
- Crash resilience and safe failure (never lose user intent silently)
- Permission handling: graceful degradation when notification access is denied
- Testing: unit, instrumentation, and manual test matrix across Android versions
- Performance: no excess wakeups, minimal battery impact

## Phase 5 — Community

*Public beta and contributor growth.*

- Public beta (closed then open)
- Additional translations (community-contributed, no logic changes)
- Contributor documentation improvements, issue triage, good-first-issues
- Community feedback incorporated into rules and UX

## Future

- **iOS** — separate `ios/` implementation in Swift / SwiftUI, sharing product concepts but not code. See `docs/architecture/overview.md`.
- Additional languages beyond English and Portuguese
- Advanced notification rules (categories, keywords, contacts)
- Scheduling profiles (Work, Evening, Weekend, Focus)
- Backup / export (local, user-initiated, encrypted)

## What Is Not Planned for the MVP

- Backend, account system, cloud sync, analytics, telemetry
- Payments, subscriptions, ads
- Overly broad device-admin or accessibility-service abuse — only `NotificationListenerService` as intended

For the current status, see `README.md` and the `main` branch.
