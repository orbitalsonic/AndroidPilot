---
name: sonicc-setup-firebase
description: Sonic Cursor Skill: One-shot Firebase setup for an app — google-services + Crashlytics Gradle plugins, dummy google-services.json placeholder, Firebase BOM, Analytics + Crashlytics on :core-platform, PlatformFirebase, then runs the three leaf skills (sonicc-setup-firebase-events, sonicc-setup-firebase-remote-config, sonicc-setup-firebase-messaging) after yes/no questions. Use when the user says setup firebase, add firebase to the project, analytics + crashlytics + remote config + messaging together, or /sonicc-setup-firebase — not for FCM push handling (sonicc-fcm) and not for adding keys / screens to an existing setup (sonicc-add-firebase-remote-config, sonicc-add-firebase-events).
---

# Setup Firebase (orchestrator)

Follow `.cursor/rules/22-platform-firebase.mdc`, `08-gradle.mdc`, `13-libraries-stack.mdc`, `14-security-secrets.mdc`, `16-logging.mdc`, `02-project-structure.mdc`, `00-global.mdc`.  
Gradle sections: [`reference/gradle.md`](../../../rules/reference/gradle.md). Event catalog: [events.md](../events.md).  
Leaf skills this one runs: **`sonicc-setup-firebase-events`**, **`sonicc-setup-firebase-remote-config`**, **`sonicc-setup-firebase-messaging`**. Their rules apply unchanged — this skill only adds the shared base and the order.

Obey `.cursor/project-settings.json` when present (`applicationId`, `appName`, `uiFramework`, `writeTestsWithFeatures`).

**Requires human approval** before adding any Firebase artifact that is not yet in the catalog (`00-global` Never #1). Ask once for the whole set, not per artifact.

## Goal

| Piece | Where | Owner |
|-------|-------|-------|
| `google-services` + `firebase-crashlytics` Gradle plugins | root `apply false`, applied on `:app` | this skill |
| `google-services.json` (real, or the dummy placeholder until Firebase console export) | `:app/` | this skill |
| Firebase BOM + `firebase-analytics` + `firebase-crashlytics` | `:core-platform` (single-module: `:app`) | this skill |
| `PlatformFirebase` object (`recordException`, `postScreenView`, `postUiClick`, `getDeviceToken`) + `Constants.TAG_FIREBASE` | `:core-platform` / `:core-common` | this skill |
| `screen_view` / `ui_click` on every screen, `EventsProvider` | all screens | `sonicc-setup-firebase-events` |
| Remote Config → `SharedPrefManager` cache, `FetchRemoteConfigUseCase`, Startup fetch | `:data` / `:domain` / Startup | `sonicc-setup-firebase-remote-config` |
| `firebase-messaging` dependency (no service) | `:core-platform` | `sonicc-setup-firebase-messaging` |

Push handling (`SonicMessagingService`, topics, notification UI) is **not** part of setup → `sonicc-fcm` when the user asks.

## Entry

| App state | Action |
|-----------|--------|
| `google-services` plugin + BOM + `PlatformFirebase` already present | Skip Step 1–3; still ask Step 0 and run only the leaf skills answered **yes** whose stack is missing. |
| Fresh `sonicc-setup-new-project` output | Firebase base already exists there — run only the missing leaf parts. |
| No Firebase | Full run. |
| Legacy helpers (`FirebaseUtils`, per-screen event names, `RemoteConfiguration` in `Application`) | Keep them working; the leaf skills own the cutover rules (`sonicc-setup-firebase-events` Step 0.5, `sonicc-setup-firebase-remote-config` Entry). |

---

## Step 0 — Yes / no questions (mandatory AskUserQuestion)

Ask these in **one** `AskUserQuestion` call, each as a yes / no question (Analytics SDK + Crashlytics are always installed — they are the base):

| # | Question | Default | Runs |
|---|----------|---------|------|
| 1 | Wire Analytics **events** on every screen now (`screen_view` + `ui_click`, `EventsProvider`)? | yes | `sonicc-setup-firebase-events` |
| 2 | Add **Remote Config** with the SharedPreferences cache and Startup fetch? | yes | `sonicc-setup-firebase-remote-config` |
| 3 | Add the **firebase-messaging** dependency (mandatory on new projects, no service)? | yes | `sonicc-setup-firebase-messaging` |
| 4 | Is the real **`google-services.json`** available now (otherwise the dummy placeholder is written and must be replaced before release)? | no | Step 2 |

Wait for the answers. Then run Steps 1–3, then the leaf skills answered **yes**, in the order of the table. Do not ask again inside a leaf skill for something already answered here (e.g. catalog approval); leaf-specific questions (RC keys, screen names) still run.

---

## Step 1 — Plugins + catalog

`libs.versions.toml` (latest stable; `sonicc-gradle-organize` section headers):

```toml
[versions]
googleServices = "…"        # com.google.gms:google-services
firebaseCrashlyticsPlugin = "…"
firebaseBom = "…"

[libraries]
# Firebase
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "firebaseCrashlyticsPlugin" }
```

Root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
```

`:app/build.gradle.kts` (after the Android application plugin; AGP 9.3+ built-in Kotlin — no `kotlin-android`):

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}
```

`:core-platform/build.gradle.kts` (single-module: `:app`):

```kotlin
// Firebase
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)
implementation(libs.firebase.crashlytics)
```

Release mapping upload: keep `mappingFileUploadEnabled` default (true) on release; set `nativeSymbolUploadEnabled` only when the app ships NDK code.

---

## Step 2 — `google-services.json`

- **Real file available (Q4 = yes):** place it at `:app/google-services.json`. Verify `client[].client_info.android_client_info.package_name` equals `applicationId` for every flavor / suffix the app builds (`.debug` suffix needs its own client entry or the plugin fails).
- **Not available (Q4 = no):** copy [templates/google-services.json](templates/google-services.json) to `:app/google-services.json`, replace `YOUR.PACKAGE` with `applicationId` and `your-project-id` with a slug of `appName`. The plugin accepts it and the build passes; Firebase calls no-op at runtime until the real file replaces it.

Then:

1. Add `// TODO(firebase): replace dummy google-services.json before release` **only** in the PR / report — not in source (`00-global` no TODOs in code).
2. Report it under **Blockers** in the final summary.
3. Never commit a real file that carries production keys for a different app; the dummy carries no secret.

---

## Step 3 — Base classes

1. Copy [`sonicc-setup-new-project/templates/firebase/PlatformFirebase.kt`](../../project/sonicc-setup-new-project/templates/firebase/PlatformFirebase.kt) → `:core-platform` `firebase/PlatformFirebase.kt` when missing. `object`, no `Context` field (`22`). Keep `recordException`, `postScreenView`, `postUiClick`, `setEntrySource`, `getDeviceToken`; drop the ads-revenue helper unless the app has ads.
2. Copy [`sonicc-setup-new-project/templates/firebase/EventsProvider.kt`](../../project/sonicc-setup-new-project/templates/firebase/EventsProvider.kt) → `:core-common` when missing (`sonicc-setup-firebase-events` trims it to real screens).
3. `Constants` (`:core-common`): `TAG_FIREBASE`, `TAG_REMOTE_CONFIG` (`16-logging`).
4. Application manifest:

```xml
<meta-data
    android:name="google_analytics_automatic_screen_reporting_enabled"
    android:value="false" />
```

5. Crashlytics: no manual init. Route caught exceptions through `Throwable.recordException(log)`; ViewModels use `handleError` (`16-logging`). Do not enable `firebase_crashlytics_collection_enabled=false` in debug unless the user asks.

---

## Step 4 — Run the leaf skills (answered yes, in order)

1. **`sonicc-setup-firebase-events`** — full-app `screen_view` / `ui_click`; its Step 0 (screen names) and Step 0.5 (remove old events) still run. Skip its Step 1 catalog work already done here.
2. **`sonicc-setup-firebase-remote-config`** — its Step 0 (RC keys) still runs; `firebase-config` + `kotlinx-coroutines-play-services` go on `:data`; Startup fetch with the 5 s navigate rule.
3. **`sonicc-setup-firebase-messaging`** — dependency on `:core-platform` only; no service, no manifest, no token code.

Each leaf skill's **Verify** list is part of this skill's verify.

---

## Step 5 — Verify

- [ ] `google-services` + `firebase-crashlytics` plugins: root `apply false`, applied on `:app`, versions in catalog
- [ ] `:app/google-services.json` present — real, or dummy reported under Blockers
- [ ] BOM + `firebase-analytics` + `firebase-crashlytics` on `:core-platform` (or `:app` single-module); no versions on BOM-managed artifacts
- [ ] `PlatformFirebase` is an `object` without `Context`; `TAG_FIREBASE` / `TAG_REMOTE_CONFIG` exist
- [ ] Automatic screen reporting off
- [ ] Leaf skills answered **yes** completed with their own verify lists; answered **no** listed as skipped
- [ ] `./gradlew :app:assembleDebug` passes with the current `google-services.json`
- [ ] No Firebase SDK calls from Fragments / `*ScreenContent` (`22`)

## Output

```markdown
## Firebase setup
- Plugins / catalog: done
- google-services.json: real | dummy (replace before release)
- Analytics + Crashlytics: done (:core-platform)
- Events (sonicc-setup-firebase-events): done | skipped
- Remote Config (sonicc-setup-firebase-remote-config): done | skipped
- Messaging dep (sonicc-setup-firebase-messaging): done | skipped

## Blockers
- …
```

## Do not

- Add `FirebaseMessagingService`, FCM manifest entries, or push UI (`sonicc-fcm`)
- Add Remote Config keys or screen events to an existing setup here (`sonicc-add-firebase-remote-config`, `sonicc-add-firebase-events`)
- Pin versions on BOM-managed Firebase artifacts
- Put Firebase deps only on `:app` when `:core-platform` exists
- Commit secrets or another app's `google-services.json` (`14-security-secrets`)
- Leave the dummy JSON unreported
