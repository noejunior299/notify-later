# Notification Pipeline — Notify Later

## Purpose

Notify Later observes notifications, holds supported ones for a user-configured delay, and re-delivers them locally. Nothing leaves the device.

## Implemented Pipeline

```
Android notification
        |
NotificationListenerService                 core/notifications/NotifyLaterNotificationListener.kt
        |
Capture + safety filters                    core/notifications/NotificationCapture.kt
        |
Persistent local store (dueAt + status)     data/repository/PersistentNotificationRepository.kt
        |
WorkManager job (one unique job per item)    core/scheduler/Scheduler.kt
        |
DeliverNotificationWorker                   core/scheduler/DeliverNotificationWorker.kt
        |
NotificationRenderer                        core/notifications/NotificationRenderer.kt
        |
Delayed notification shown by Android
```

The original notification is cancelled only after the captured item has been persisted and its delivery job scheduled.

## Capture Rules

The listener maps a `StatusBarNotification` into `RawNotification` and lets the pure `NotificationCapture` rules decide. A notification is **not** captured when:

- it comes from Notify Later itself (prevents re-capture loops);
- it comes from `android` or `com.android.systemui` (system chrome);
- it is ongoing or a foreground-service notification;
- it is a group summary;
- its category is `call`, `alarm`, or `service`;
- it has no notification key or a non-positive timestamp.

Title and text are trimmed and length-limited (200/1000 characters). App labels fall back to the package name. The captured model keeps the package, label, title, text, original timestamp, Android key, category, priority, status, due time, and delivery time.

## Persistence

`PersistentNotificationRepository` stores the queue in app-private `SharedPreferences` as JSON.

- Pending notifications are **never dropped**.
- Completed history is capped at 100 entries.
- Notification data and settings are excluded from cloud backup and device transfer (`backup_rules.xml`, `data_extraction_rules.xml`).
- The repository is local-only; there is no network, account, or telemetry.

Each notification moves through `PENDING` → `DELIVERED` (or `DISMISSED` / `EXPIRED` if a safety path applies). Pending updates from the same Android notification key keep their original `dueAt`, so a chatty app cannot extend its delay indefinitely or create duplicate queue rows.

## Scheduling and Delivery

The global delay is configured in Settings (30 seconds, 1 minute, 5 minutes, 15 minutes, or 1 hour; default 1 minute) and persisted by `PersistentSettingsRepository`.

For each held notification:

1. `WorkManagerNotificationScheduler` enqueues a unique `OneTimeWorkRequest` with the remaining delay.
2. `DeliverNotificationWorker` verifies the item is still pending and its due time has passed.
3. The worker checks `POST_NOTIFICATIONS`, renders the notification, and only then marks it `DELIVERED` with a delivery timestamp.
4. If posting is impossible or rendering fails, the item stays pending and WorkManager retries with backoff.

On process start, the application re-enqueues any pending items using their original due time. This is a fallback for force-stops or OEM job cleanup; WorkManager also persists its own work across process death and reboot.

## Re-publication

`NotificationRenderer` creates the `Delayed notifications` channel and posts a copy with:

- a monochrome bell small icon tinted with the brand purple;
- the original title and text (or the source app label when content is absent);
- a subtext identifying the source app and Notify Later;
- a content intent that opens the app.

Actions, inline replies, rich media, per-app channels, and source app icons are intentionally not reproduced in this MVP.

## Permissions and Safety

Two independent mechanisms are required:

| Requirement | Purpose |
|-------------|---------|
| Notification Listener access | Read incoming notifications. Granted in Android settings. |
| `POST_NOTIFICATIONS` (Android 13+) | Show the delayed copy. Requested from Settings. |

Critical safety contract:

- Without `POST_NOTIFICATIONS`, the listener leaves the original notification untouched instead of hiding it.
- If persistence or scheduling fails, the original is left untouched.
- If cancelling the original fails after scheduling, the job is cancelled and the item is marked dismissed, avoiding a duplicate later.
- Calls, alarms, services, ongoing notifications, and system chrome are never delayed.
- Debug builds log package names and keys, never notification content. Release builds emit no capture logs.

## Testing

Unit tests cover capture filters, session baseline behavior, repository deduplication, status updates, and completed-history clearing. The manual emulator pass covers:

1. Granting notification access and `POST_NOTIFICATIONS`.
2. Posting a notification from another app.
3. Confirming the original disappears and appears as pending in Queue with a countdown.
4. Waiting for the delay and confirming the re-posted notification contains the original content.
5. Confirming Logs shows `Delivered` with a delivery time.
6. Force-stopping and restarting the app with an item pending, then confirming delivery still occurs.
7. Revoking `POST_NOTIFICATIONS` and confirming the original notification remains in the shade.

## Out of Scope (Next)

- Per-app rules, quiet hours, batching, and delivery windows.
- Installed-app discovery and app management screens.
- Rich notification reproduction and source app icons.
- Exact delivery guarantees under Doze/OEM battery restrictions.
- Dismiss/expire propagation from the re-posted notification back to the queue.
