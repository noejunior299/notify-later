# Security Policy

## Sensitive Nature of This Project

Notify Later handles notification content, which may contain highly sensitive personal information including messages, one-time codes, financial alerts, health data, and private communications.

Security is a first-class concern.

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| latest `main` | Yes |

The project is in early development. Only the latest `main` branch receives security attention at this stage. Once versioned releases begin, a version support table will be maintained here.

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

If you discover a vulnerability that could lead to:

- notification data exposure or leakage
- unauthorized access to queued or historical notifications
- permission abuse or privilege escalation
- insecure data persistence (database, files, backups)
- bypass of user consent or notification controls
- malicious behavior that could be triggered externally
- any other issue where notification content or user privacy is at risk

please report it privately.

### How to report

1. **Email the maintainer** via the email listed on the GitHub profile of [@noejunior299](https://github.com/noejunior299), or
2. **Use GitHub's private vulnerability reporting** if enabled on the repository (`Security` -> `Report a vulnerability`), or
3. **Open a minimal issue** titled `Security disclosure requested` without technical details and request a private contact channel.

Include:

- description of the vulnerability
- steps to reproduce (if applicable)
- Android version, device, and app version
- impact assessment
- suggested fix if you have one

### What to expect

- Acknowledgment within 72 hours.
- A coordinated timeline to assess, fix, and disclose.
- Credit in the fix/release notes if you wish, or anonymity if preferred.
- Please allow up to 14 days for a fix before any public disclosure.

## Disclosure Policy

- Security fixes will be released as soon as possible with a clear advisory.
- Public disclosure will be coordinated with the reporter.
- Details will be published after a fix is available and users have had an opportunity to update.

## Privacy Commitments Relevant to Security

- Notify Later is **local-first**: core functionality works without internet, no backend, no account system, no cloud sync in the MVP.
- **No telemetry or analytics** in the MVP. This is intentional to reduce attack surface and privacy risk.
- Notification data must remain on-device and encrypted at rest where Android provides such mechanisms (e.g., EncryptedSharedPreferences / SQLCipher when adopted).
- Permissions are requested only when needed and with clear user consent. See `docs/android/permissions.md`.

## Scope

In scope:

- `android/` — the Android application
- `docs/` — if documentation recommends an insecure configuration
- Build and CI configuration that could introduce supply-chain risk

Out of scope (unless it directly affects notification privacy):

- Social engineering of maintainers
- Denial-of-service against GitHub infrastructure
- Issues requiring physical access to an unlocked device without bypass

Thank you for helping keep Notify Later trustworthy.
