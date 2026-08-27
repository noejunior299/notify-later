# Rules Engine — Notify Later

Conceptual document. No implementation yet.

## Goal

Give users control over *when* notifications reach them, per application and globally, without becoming a complex rules editor.

## Domain Concepts

| Concept | Description |
|---------|-------------|
| `Notification` | Normalized notification (package, channel, title, text, time, status). |
| `NotificationRule` | Associates an app (or global scope) with a `DelayPolicy`. |
| `DelayPolicy` | Describes *whether* and *how long* to hold a notification. |
| `DeliverySchedule` | Describes *when* held notifications are released (e.g., at interval, in a window, on demand). |
| `NotificationQueue` | Ordered collection of held notifications awaiting delivery. |
| `NotificationStatus` | Lifecycle: `pending` → `delivered` / `dismissed` / `expired`. |

## Rule Evaluation (Conceptual)

```
Input: Notification
  1. Resolve matching NotificationRule (app-specific overrides global)
  2. Evaluate DelayPolicy
     - if Immediate → deliver
     - if Delayed   → enqueue with DeliverySchedule
     - if Filtered  → respect user's intent (e.g., quiet hours) — no silent drop without clarity
  3. Output: routing decision + optional scheduled time
```

## Example Rules (Illustrative)

- **Global default:** deliver immediately.
- **App rule — Social:** delay 2 hours, deliver in batch at :00.
- **App rule — Work chat:** delay outside 09:00–18:00, deliver at next work window.
- **Quiet hours:** no re-delivery between 22:00 and 07:00 unless marked urgent.

The MVP should start with a small, understandable rule set. Advanced rules (categories, keywords, profiles) are future work.

## Policy Considerations

- One primary decision per screen (per `docs/product/design.md` — One Decision at a Time).
- Defaults must be sensible; customization is progressive disclosure, not upfront burden.
- Every rule must be inspectable and reversible.
- No rule may require network or cloud evaluation.

## Persistence (Future)

Rules and schedules are local (Room / DataStore). Schema must support migration and export considerations without introducing cloud sync.

## Status

Design only. Implemented in Phase 2 per `docs/product/roadmap.md`.
