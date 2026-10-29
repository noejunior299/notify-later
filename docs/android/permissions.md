# Permissions — Notify Later

## Philosophy

Notify Later follows **No Dark Patterns**: permissions are requested only when needed, explained clearly, and equally easy to decline.

## Two Different Mechanisms (Do Not Confuse Them)

Notify Later depends on two distinct Android mechanisms that are often conflated:

### 1. Notification Listener Access (special access)

- Grants the ability to **read** all posted notifications via `NotificationListenerService`.
- NOT a runtime permission. There is no dialog API. It is granted only when the user enables the app under *Settings > Notifications > Device & app notifications* (or equivalent OEM path).
- Implementation: `core/notifications/NotifyLaterNotificationListener.kt`, registered with `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`.
- State detection: `core/notifications/NotificationAccess.kt` queries `Settings.Secure "enabled_notification_listeners"` and `NotificationManagerCompat.getEnabledListenerPackages`. UI reads this on every screen entry so revoked access is detected without relying on cached UI state.
- UX: an explicit card/button opens `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`; the user leaves the app to grant it. Revocable at any time from the same screen; the service unbinds automatically. Settings refreshes the state on resume.

### 2. POST_NOTIFICATIONS (runtime permission, Android 13+ / API 33+)

- Grants the ability to **post** notifications as Notify Later.
- Requested at runtime from the Settings permission card via `ActivityResultContracts.RequestPermission`.
- The listener checks this permission before cancelling an original notification. Without it, the original is left untouched. The delivery worker also checks it and retries instead of dropping a queued notification.
- Having listener access does NOT imply POST_NOTIFICATIONS, and vice versa. They must be treated independently everywhere in the codebase.
- Both permissions are required end-to-end: listener access to capture, POST_NOTIFICATIONS to re-deliver.

## Current Permission Matrix

| Capability | Mechanism | Status |
|-----------|-----------|--------|
| Observe other apps' notifications | Notification Listener (special access) | Implemented; required for capture |
| Post Notify Later notifications | `POST_NOTIFICATIONS` (API 33+) | Implemented with in-app request; required for re-delivery |
| Delayed delivery | WorkManager (deferrable work) | Implemented; not an exact alarm |
| Location, contacts, storage, camera, microphone | — | Never needed; out of scope |

Delivery timing is best-effort: Doze, App Standby, and OEM battery policies can defer WorkManager jobs. The app never holds an original notification unless it can persist it and re-post it later.

## Security Relevance

Because notification content is sensitive, permission handling is security-critical. See `SECURITY.md`. Any bypass or over-collection must be treated as a security issue and reported privately.

## References

- Architecture: `docs/architecture/notification-pipeline.md`
- Design: `docs/product/design.md` — Privacy Is Visible
