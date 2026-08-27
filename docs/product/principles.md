# Principles — Notify Later

These principles are binding for product and engineering decisions. If a proposed feature conflicts with a principle, the principle wins.

## 1. Privacy First

Notifications may contain highly sensitive information: messages, codes, financial alerts, health data.

- Core functionality must work locally on the device.
- Do not introduce a backend, account system, analytics service, telemetry, or cloud dependency for the MVP.
- Notification content must remain on-device. Any future network feature must be opt-in with explicit consent and documented privacy implications.

## 2. Local First

The application must operate without an internet connection after installation.

- Rules, queue, and delivery must function offline.
- No feature may require internet for its core value.
- Documentation and setup should also be available offline where possible (bundled help or on-device guidance).

## 3. Open Source

The project is public and should be easy for external developers to understand, build, test, and contribute to.

- Keep the repository clean, documented, and reproducible.
- Prefer standard Android tooling over exotic custom stacks.
- Explain architecture decisions in `docs/`.

## 4. User Control

Applications should not dictate when users give them attention. Notify Later gives that control back to the user.

- Users define rules; the app executes them faithfully.
- Provide clear defaults, but make every automation inspectable and reversible.
- Never silently change rules or delivery behavior without user action.

## 5. No Dark Patterns

Never manipulate users into enabling unnecessary permissions, notifications, tracking, or monetization.

- Request permissions only when needed and explain why.
- Make "decline" as easy as "accept".
- No guilt language, no hidden toggles, no pre-checked consent.

## 6. Calm UI

The application itself should not become another source of distraction.

- Quiet by default. Spacious layout, clear typography, subtle color.
- One decision at a time. Avoid dashboards that reward prolonged use.
- If Notify Later is working correctly, users should not need to open Notify Later very often.

See also [Design](design.md) for how these principles translate into visual and interaction design.

## How to Use These Principles

- For product ideas: check alignment in the issue template's "Why It Matters" section.
- For engineering: prefer the simplest local, private, maintainable solution.
- For review: a PR that violates a principle should be blocked even if the code is otherwise correct.
