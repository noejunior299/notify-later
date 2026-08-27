# Android Development Guide — Notify Later

## Requirements

- **JDK 17** — required by Android Gradle Plugin 9.3.1. JDK 21 also works if Gradle is configured, but 17 is the baseline in CI.
- **Android Studio** Hedgehog or newer (or any IDE compatible with AGP 9.3.1 and Kotlin 2.2.10).
- **Android SDK** — Platform 37, Build Tools, Platform Tools.
- **Git**

Check:

```bash
java -version   # should report 17
./gradlew --version  # from android/
```

## Clone

```bash
git clone https://github.com/noejunior299/notify-later.git
cd notify-later
```

## Open the Project

Open the **`android/`** directory in Android Studio (not the repository root). The Gradle project root is `android/`.

## Run

- **Android Studio:** select `app` run configuration, choose emulator or device (minSdk 29), Run.
- **Command line:**

```bash
cd android
./gradlew assembleDebug
# APK at android/app/build/outputs/apk/debug/app-debug.apk
```

## Test

```bash
cd android
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest   # requires emulator/device, Compose tests
```

CI runs `testDebugUnitTest` on every push/PR. Keep `main` green.

## Project Conventions

- **Package:** `ao.techify.notifylater`
- **Application ID:** `ao.techify.notifylater` (stable, see `docs/architecture/overview.md`)
- **Kotlin code style:** `official` (`kotlin.code.style=official` in `gradle.properties`)
- **Compose + Material 3:** use `MaterialTheme`, `colorScheme`, `typography`. Avoid custom theming unless in `ui/theme/`.
- **Strings:** never hard-code user-facing text. Add to `app/src/main/res/values/strings.xml` and `values-pt/strings.xml`, then use `stringResource` / `getString`.
- **Commits:** imperative subject line, e.g., `feat: add onboarding navigation`, `docs: clarify permissions`. See `docs/contributing/pull-requests.md`.
- **Branches:** `feat/<short>`, `fix/<short>`, `docs/<short>`, `chore/<short>`.

## Where Code Lives

- App code: `android/app/src/main/java/ao/techify/notifylater/` — see `docs/android/architecture.md` for package guidance.
- Docs: `docs/`
- Build: `android/build.gradle.kts`, `android/app/build.gradle.kts`, `android/gradle/libs.versions.toml`

## Common Tasks

### Add a String

1. Add to `android/app/src/main/res/values/strings.xml` (English).
2. Add same key to `android/app/src/main/res/values-pt/strings.xml` (Portuguese).
3. Reference via `stringResource(R.string.key)` in Compose or `context.getString(R.string.key)`.

### Add a Screen

1. Create `feature/<name>/` package with `*Screen.kt`, `*ViewModel.kt`, `*Route.kt` (route object).
2. Register the route in `navigation/AppNavHost.kt`.
3. Wire navigation actions (avoid passing large objects as nav args; pass IDs and load from repository).

### Add a Dependency

1. Add version to `gradle/libs.versions.toml` if new.
2. Add library entry and plugin alias handling if needed.
3. Document justification in the PR. Avoid backend/analytics/cloud libs in MVP.

## Troubleshooting

- **Gradle sync fails — JDK:** ensure `JAVA_HOME` points to JDK 17. Check `android/gradle/gradle-daemon-jvm.properties` if present.
- **SDK not found:** set `ANDROID_HOME` or ensure Android Studio SDK is installed; `android/local.properties` is gitignored and generated locally.
- **Compose preview not rendering:** ensure `androidx.compose.ui.tooling.preview` is imported and preview is annotated with `@Preview`.
- **Tests fail:** run `./gradlew testDebugUnitTest --info` for details; ensure no hard-coded strings break lint in future checks.

## Useful Commands

```bash
./gradlew tasks
./gradlew assembleDebug
./gradlew testDebugUnitTest --tests "*ExampleUnitTest*"
./gradlew lintDebug  # when lint is configured
```

See also `docs/contributing/getting-started.md`.
