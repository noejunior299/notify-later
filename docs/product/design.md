# Design System — Notify Later

## Design Philosophy

Notify Later should feel quiet.

The application exists to reduce interruptions, not create another source of them. Every screen, word, and color should reinforce calm and trust.

> If Notify Later is working correctly, users should not need to open Notify Later very often.

The app is a utility that disappears. Success is measured by time *not* spent in the app, by fewer interruptions, and by user confidence that notifications will arrive when expected.

## Design Principles

### 1. Quiet by Default

- No loud colors, no excessive cards, no gradients, no gamification.
- Use whitespace generously. Let content breathe.
- Animations, if any, are subtle and purposeful (e.g., 150–200 ms easing for navigation), never attention-seeking.

### 2. One Decision at a Time

- Each screen asks one primary question.
- Onboarding progresses step-by-step; settings are grouped with clear defaults.
- Avoid overwhelming the user with bulk configuration. Provide sensible presets and progressive disclosure.

### 3. Native First

- Follow Material 3 conventions and Android platform patterns.
- Use AndroidX, Material 3 components, and system typography.
- Prefer platform navigation (back, gestures) over custom chrome.
- Respect dynamic color and dark theme.

### 4. No Dark Patterns

- Permissions, toggles, and consent must be explicit, reversible, and equally accessible.
- Do not use persuasive copy to push users toward enabling more than they need.
- Label destructive or far-reaching actions clearly.

### 5. Privacy Is Visible

- Show that data stays local. Surface storage and permission state transparently.
- When handling notification content, make it clear what is stored, for how long, and how to clear it.
- Avoid technical jargon but do not hide scope.

### 6. The App Should Disappear

- Optimize for set-and-forget use. The home screen confirms everything is working; it does not demand action.
- Avoid badges, streaks, or engagement hooks inside Notify Later itself.
- Provide quiet confirmation rather than celebration.

## Visual Direction

Minimal, calm, spacious, accessible, native Android, low visual noise, clear typography, subtle use of color.

- **Layout:** single-column, generous padding (16–24 dp), clear hierarchy with headline / body / caption.
- **Typography:** Material 3 type scale, system font. Headlines are concise; body text is legible at 16 sp with 24 sp line height.
- **Color:** restrained palette derived from Material 3 dynamic color. Color is used to communicate state (success, warning, error), not decoration.
- **Elevation:** flat or subtle (0–1 dp). Avoid heavy shadows.
- **Imagery:** no stock illustrations for decoration. Use simple icons and empty-state guidance.

## Design Tokens (Conceptual)

Defined conceptually; implemented via Material 3 `ColorScheme` and `Typography`.

| Token | Purpose |
|-------|---------|
| `Background` | App background |
| `Surface` | Card / sheet surface |
| `Primary` | Primary actions, selected state |
| `Text Primary` | Headlines and primary body |
| `Text Secondary` | Captions, helper text, timestamps |
| `Border` | Dividers and outlines (low contrast) |
| `Success` | Confirmation, delivered state |
| `Warning` | Attention without alarm (e.g., permission needed) |
| `Error` | Failure, blocked delivery |

Do not aggressively customize Material 3 yet. Extend tokens only when a real screen demands it.

## Component Guidance

- **App Bar:** standard Material 3 TopAppBar, no custom branding bar.
- **Buttons:** filled for primary, outlined or text for secondary. One primary per screen.
- **Lists:** plain lists with dividers; avoid excessive card nesting. Cards only for grouped settings.
- **Empty States:** short headline + one sentence + single CTA. See strings for wording.
- **Accessibility:** minimum 48 dp touch targets, 4.5:1 contrast, content descriptions for icons, Dynamic Type / font scaling support.

## Internationalization Considerations

- All user-facing strings via `res/values/strings.xml` and `res/values-pt/strings.xml`.
- Design must accommodate 30 %+ text expansion (Portuguese vs English).
- No hard-coded strings in Kotlin. No mixed languages in a single screen.

## What to Avoid

- Gradients, heavy shadows, glassmorphism, gamification, confetti, streaks.
- Excessive cards stacked on cards.
- Jargon or alarmist language around permissions.
- Any pattern that makes the app itself a source of distraction.
