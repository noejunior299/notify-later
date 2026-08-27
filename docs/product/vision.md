# Vision — Notify Later

## Tagline

> **Your notifications belong to you.  
> You decide when they reach you.**

Portuguese:

> **As suas notificações pertencem a si.  
> Você decide quando elas chegam.**

## What Is Notify Later?

Notify Later is an open-source, privacy-first, local-first Android application that allows users to decide **when** notifications from other applications should reach them.

Applications should not dictate when users give them attention. Notify Later returns that control to the user.

## The Problem

- Every installed app can interrupt you at any time.
- Notifications are optimized for the sender's goals, not the receiver's attention.
- Users either tolerate constant interruptions or silence everything and miss what matters.
- Existing tools often require accounts, cloud sync, or access to notification content on remote servers.

## The Ideal Experience

> Configure your notification rules once, then forget about the app.

If Notify Later is working correctly, users should not need to open Notify Later very often.

The app should feel:

- **Calm** — quiet, low visual noise, no urgency
- **Minimal** — few decisions, clear defaults
- **Trustworthy** — privacy is visible, data stays local
- **Native** — feels like part of Android, not a foreign layer

## Non-Goals for the MVP

- Not a notification reader or inbox that encourages more time in-app.
- Not a social network, messenger, or analytics dashboard.
- Not a backend service, account system, or cloud platform.
- Not a monetization vehicle. No ads, no subscriptions inside the app.

Sponsorship may happen externally (GitHub Sponsors, etc.), but never as an in-app paywall.

## Target User

Anyone who wants fewer interruptions without missing what matters. Initially, Android users comfortable installing an open-source app via APK or Play, who value privacy and control.

## Platform Strategy

- **Android first** — Kotlin, Jetpack Compose, Material 3, `NotificationListenerService`.
- **iOS planned** — Swift / SwiftUI, using iOS notification APIs. Planned as a separate `ios/` implementation sharing product concepts but using platform-native code. No forced KMP in early phases.

See [Roadmap](roadmap.md) and [Architecture Overview](../architecture/overview.md).
