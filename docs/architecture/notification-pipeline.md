# Notification Pipeline — Notify Later

> Phase 1 is a technical spike and not the final notification delivery implementation.

## Purpose

When fully implemented, Notify Later will observe notifications, decide whether to deliver them immediately or hold them, and re-deliver held notifications on a schedule — all locally.

## Conceptual Pipeline

```
Android Notification
        |
NotificationListenerService        // IMPLEMENTED (Phase 1 spike)
        |
Notification (domain model)        // IMPLEMENTED (capture + in-memory store)
        |
Rule Engine                        // NOT implemented (Phase 2)
        |
Immediate ------------------> User
        |
Delayed
        |
Notification Queue                 // NOT implemented (in-memory debug list only)
        |
Scheduler                          // NOT implemented (Phase 2)
        |
Notification Renderer              // PARTIAL: re-publishes immediately, no delay
        |
User
```

## Phase 1 Spike — What Is Implemented

### NotificationListenerService

`core/notifications/NotifyLaterNotificationListener.kt` extends `android.service.notification.NotificationListenerService`.

- Registered in `AndroidManifest.xml` with `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` and the `android.service.notification.NotificationListenerService` intent action.
- The system binds it only after the user grants **Notification access** in Android system settings. Notify Later never requests this via a runtime permission dialog; Settings links to `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.
- Access state can be queried programmatically via `core/notifications/NotificationAccess.kt` (checks `enabled_notification_listeners` secure setting plus `NotificationManagerCompat.getEnabledListenerPackages`). It is never assumed from UI state alone.

The service is an orchestrator only. Logic lives in dedicated components:

```
NotificationListenerService (adapter)
        |
RawNotification (framework values mapped to a plain structure)
        |
NotificationCapture (pure Kotlin, unit-tested)
        |
NotificationRepository (in-memory, bounded to 50 items)
        |
Domain Notification -> future Rule Engine
```

Output:

```
Domain Notification
        |
NotificationRenderer
        |
Android NotificationManager
```

### What Information Is Captured

| Field | Source | Notes |
|-------|--------|-------|
| `id` | generated | deterministic id from timestamp + key hash |
| `sourcePackage` | `sbn.packageName` | identifies the source app |
| `sourceApplicationName` | PackageManager label | falls back to package name |
| `title` / `text` | notification extras | trimmed, length-limited (200/1000 chars) |
| `timestamp` | `sbn.postTime` | original posting time |
| `originalNotificationKey` | `sbn.key` | reference to the original |
| `category` / `priority` | notification metadata | coarse metadata only |

### What Is Intentionally Ignored

- Notifications posted by Notify Later itself (self-filter prevents recursion).
- Actions, inline replies, big pictures, styles, progress, group semantics.
- Anything beyond the minimal fields above.

### Connection Baseline (Initial Active Notifications)

When `onListenerConnected()` fires, the listener snapshots `activeNotifications` keys into a thread-safe session filter (`NotificationSessionFilter`). Any subsequent event whose key belongs to that baseline is logged (`ignored initial active notification`) and never captured or re-published. This prevents unwanted "Delivered by Notify Later" copies of notifications that already existed (e.g., Gmail, Digital Wellbeing) when the listener connects or reconnects.

Race-condition handling: Android does not guarantee callback ordering, so all baseline state is lock-guarded; events arriving before the snapshot is recorded are ignored fail-safe. Blank keys fall through to the existing capture validation. The baseline lives for the connection session and is replaced on reconnection.

### Re-publication (Spike)

`core/notifications/NotificationRenderer.kt` creates one channel (`notify_later_default`) and posts a copy via `NotificationCompat`:

```
WhatsApp
Joao sent you a message
Delivered by Notify Later
```

Spike limitations:

- no per-app channels;
- app icon used as small icon (per-source icons later);
- no actions, inline replies, or rich styles beyond BigText;
- posted immediately — there is no delay yet.

## Safety Requirement (Critical)

**Data safety > functionality.** During Phase 1 the listener NEVER cancels the original system notification. If capture fails, rendering fails, POST_NOTIFICATIONS is missing (Android 13+), or any exception occurs, the original notification remains available. No code path can permanently destroy a user notification.

## Privacy Notes

- Every stage stays on-device. No network, analytics, telemetry, or backend exists in this phase.
- Storage is in-memory only (bounded list); nothing persists to disk. Room arrives in Phase 2 with retention and clear-data guarantees.
- Debug logging is enabled only in debug builds and logs package names/keys only — never title/text content.

## Status

Phase 1 spike complete (see roadmap). Next: Rule Engine + queue + scheduler (Phase 2).
