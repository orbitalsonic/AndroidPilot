---
name: android-preflight
description: Final verification checklist to run before declaring Android work finished — build, both themes, string resources, lifecycle and leak risks, registered permissions and components, resource parity between values and values-night, and honest reporting of what was and was not verified. Use at the end of any feature, fix, or upgrade, or when the user asks to double-check or review work before committing.
---

# Preflight — before you say it is done

Walk this list. If anything is missing, complete it before finishing.

## Build

- [ ] `gradlew assembleDebug` succeeds (`gradlew clean assembleDebug` after a Gradle/AGP change).
- [ ] `gradlew assembleRelease` succeeds when R8-sensitive code changed (reflection, Gson models,
      Firebase) — and keep rules were added to `app/src/main/keepRules/*.keep`.
- [ ] If you could not run the build, say so explicitly. Never claim it passed.

## Correctness and completeness

- [ ] No TODOs, placeholders, pseudo code, or unfinished branches.
- [ ] Every file the change touches was updated — nothing left half-migrated.
- [ ] No unused import, unused resource, unused catalog entry, or applied-but-unused plugin left behind.
- [ ] No debug logging or commented-out code left in.

## Resources

- [ ] Every user-visible string comes from `values/strings.xml` (translations noted as pending).
- [ ] Any new semantic color exists in **both** `values/colors.xml` and `values-night/colors.xml`.
- [ ] No hardcoded hex color, no `px`, no `dp` used for text.
- [ ] The screen renders correctly in BOTH light and dark theme.
- [ ] `contentDescription` set (or `importantForAccessibility="no"`), 48dp touch targets respected.

## Registration

- [ ] New activities in `AndroidManifest.xml`; new destinations in the right `*_nav_graph.xml`.
- [ ] Permissions declared with correct `maxSdkVersion` / feature flags, and handled at runtime
      including the "Don't ask again" path (`openSettingPage()`).
- [ ] Notification channels, receivers, services and workers registered where needed.
- [ ] New dependencies declared in `gradle/libs.versions.toml` and referenced as `libs.*`.

## Lifecycle and safety

- [ ] Flows collected with `repeatOnLifecycle(STARTED)` / `flowWithLifecycle`.
- [ ] UI touched after async work only when `isAdded`, `view != null`, and the binding is alive.
- [ ] No Activity/Fragment/View/Context held in a singleton, companion object, or static field.
- [ ] Timers, handlers, listeners and receivers cancelled in the matching lifecycle callback.
- [ ] Survives rotation, configuration change, process death, and back stack restore.
- [ ] No `GlobalScope`, no `!!`, no blocking work on the main thread.
- [ ] `CancellationException` is rethrown, not converted into a failure state.

## Compatibility

- [ ] Works on API 24–37; version-specific behaviour guarded with `Build.VERSION.SDK_INT`.
- [ ] Checked the platform traps relevant to the change: notification permission (13+), exact alarms
      (12+), foreground service types (14+), `PendingIntent` mutability (23+/31+), scoped storage
      (29+), edge-to-edge (35+), receiver export flags (33+).

## Architecture

- [ ] Existing architecture and coding style preserved — no Compose, no modularisation, no package
      renames, no architecture swap on an existing screen.
- [ ] No duplicate helper created where one already existed in `helpers/` or `common/`.
- [ ] ViewModel holds no `Context`; repository owns data access; no raw `SharedPreferences` outside
      `SharedPrefManager`.

## Report

State plainly:

- what changed and why
- what you verified, and how
- what you deliberately left out
- what the user should re-test manually
