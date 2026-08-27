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
- UX: an explicit card/button opens `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`; the user leaves the app to grant it. Revocable at any time from the same screen; the service unbinds automatically.

> Phase 1 is a technical spike and not the final notification delivery implementation.

### 2. POST_NOTIFICATIONS (runtime permission, Android 13+ / API 33+)

- Grants the ability to **post** notifications as Notify Later.
- Requested at runtime like normal dangerous permissions (relevant from Phase 2 onward, when delayed re-delivery ships). In the Phase 1 spike the renderer checks `PermissionHelper.hasPostNotificationsPermission` and fails safe by not posting if missing.
- Having listener access does NOT imply POST_NOTIFICATIONS, and vice versa. They must be treated independently everywhere in the codebase.

## Current Permission Matrix

| Capability | Mechanism | Status |
|-----------|-----------|--------|
| Observe other apps' notifications | Notification Listener (special access) | Implemented (Phase 1 spike) |
| Post Notify Later notifications | `POST_NOTIFICATIONS` (API 33+) | Checked fail-safe; formal request flow in Phase 2/3 |
| Scheduling exact delivery | WorkManager / AlarmManager | Not implemented |
| Location, contacts, storage, camera, microphone | — | Never needed; out of scope |

## Security Relevance

Because notification content is sensitive, permission handling is security-critical. See `SECURITY.md`. Any bypass or over-collection must be treated as a security issue and reported privately.

## References

- Architecture: `docs/architecture/notification-pipeline.md`
- Design: `docs/product/design.md` — Privacy Is Visible
