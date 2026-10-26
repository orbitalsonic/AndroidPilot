---
name: sonicc-fcm
description: Sonic Cursor Skill: First-time Firebase Cloud Messaging push wiring (QiblaCompass SonicFcmManager shape) — SonicMessagingService, data-payload mapper (GENERAL / EXTERNAL_LINK / REENGAGEMENT), custom RemoteViews notification, topic subscriptions (all users, language, country, premium / non-premium), MainActivity setup + Language / Premium topic updates, POST_NOTIFICATIONS ask. Use when the user asks to implement FCM, push notifications, SonicFcmManager, FCM topics, or /sonicc-fcm — not for the dependency-only add (use sonicc-setup-firebase-messaging) and not for Analytics / Remote Config.
---

# Implement Firebase Cloud Messaging (first time, full push)

Follow `.cursor/rules/22-platform-firebase.mdc`, `10-manifest.mdc`, `20-permissions-runtime.mdc`, `14-security-secrets.mdc`, `16-logging.mdc`, `07-dependency-injection.mdc`, `08-gradle.mdc`, `00-global.mdc`.  
Prefs shape: [`.cursor/rules/reference/shared-preferences.md`](../../../rules/reference/shared-preferences.md). Language catalog: [`reference/languages.md`](../../../rules/reference/languages.md). Funnel / prefs names: [`reference/app-flow.md`](../../../rules/reference/app-flow.md).  
Reference app: QiblaCompass `common/fcm/` (`SonicFcmManager`, `FcmTopics`, `SonicMessagingService`, `NotificationSender`).

Dependency only (no service, no UI) → **`sonicc-setup-firebase-messaging`**. This skill assumes that dependency exists or adds it the same way.

Obey `.cursor/project-settings.json` when present (`uiFramework`, `applicationId`, `writeTestsWithFeatures`).

## Goal

Complete push stack, QiblaCompass shape:

1. **`SonicMessagingService`** (`FirebaseMessagingService`) — data messages only; drops when `POST_NOTIFICATIONS` is missing
2. **Payload** — `NotificationPayloadMapper` maps `message.data` → sealed `NotificationPayload` (`General`, `ExternalLink`, `Reengagement`) by `type`
3. **Notification** — `NotificationSender` renders a custom `RemoteViews` layout (title, body, optional icon + feature image via Glide, day/night text color), `NotificationNavigation` builds the tap intent (launch intent or `ACTION_VIEW` url), `NotificationChannelHelper` creates the high-importance channel
4. **Topics** — `SonicFcmManager` subscribes to **all users** (`applicationId`), **language** (catalog English name), **country** (SIM ISO → name) and **premium / non-premium**; re-points language and premium topics when they change
5. **Hosts** — `MainActivity` setup; Startup Language / in-app Language apply → `updateLanguage`; Startup Premium / in-app Premium purchase → `updatePremium`; Startup writes the re-engagement start date once

FCM is **platform work, not MVI**: plain `object` managers + Android components. No Intent / State / Effect for push. Ads stay untouched (`21-ads-billing`).

## Entry

| App state | Action |
|-----------|--------|
| `FirebaseMessagingService` + topics + manifest already wired | Stop. Report what exists. Offer only missing pieces (e.g. premium topic, re-engagement gate). |
| Only `firebase-messaging` dependency (`sonicc-setup-firebase-messaging` / `sonicc-setup-new-project`) | Continue from Step 1. |
| No FCM at all | Add the dependency per `sonicc-setup-firebase-messaging` (approval if not in catalog), then continue. |
| Legacy service with different payload keys | Do **not** break live campaigns. Keep the old keys readable; AskUserQuestion before changing the payload contract. |

Single-module app: same class names under `common/fcm/` (QiblaCompass), `:app` manifest and resources.

---

## Step 0 — Confirm scope (mandatory AskUserQuestion)

Audit `AndroidManifest.xml` (`MESSAGING_EVENT`), any `FirebaseMessagingService`, `subscribeToTopic`, `POST_NOTIFICATIONS`, billing (`isAppPurchased`) and language (`LanguageDataSource` / `DpLanguages`). Then ask, one question each:

1. **Topics** — all users + language + country + premium / non-premium (default, QiblaCompass) or a subset? Apps without billing drop the premium pair.
2. **Notification types** — `GENERAL`, `EXTERNAL_LINK`, `REENGAGEMENT` (default) or extra app-specific types (e.g. open a feature by `feature` key)?
3. **Where to ask `POST_NOTIFICATIONS`** — recommend the first Home / Dashboard open in `MainActivity` (via `BasePermissionFragment.askPostNotificationPermission`), or an existing feature that already asks (QiblaCompass asks on Prayer). Never inside the `StartupActivity` funnel unless the user says so.

Wait for answers. Then code.

---

## Step 1 — Catalog + module

Latest stable, `# Firebase` / `// Firebase` sections (`sonicc-gradle-organize`). `firebase-messaging` (not `-ktx`) when the catalog is on BOM ≥ 32.

```toml
# Firebase
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }
```

```kotlin
// Firebase
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.messaging)   // :core-platform (or :app if single-module)

// Image loading
implementation(libs.glide)                // only on the module that hosts NotificationSender

// Kotlin Coroutines
implementation(libs.kotlinx.coroutines.play.services)  // Task.await() in SonicFcmManager
```

Glide is already in the catalog of `:core-ui` apps — reuse that alias. If Glide is **not** in the catalog, **AskUserQuestion** before adding it (`00-global` Never #1) or render without remote images.

`:app` still needs `google-services` + `google-services.json`. Ensure `:core-common` `Constants` has `TAG_FCM` (`16-logging`) and the prefs file name used by `SharedPrefManager`:

```kotlin
const val TAG_FCM = "TAG_FCM"
const val PREFS_NAME = "AppPrefs"                 // same file SharedPrefManager opens
const val PREF_REENGAGEMENT_DATE = "reengagementDate"
```

---

## Step 2 — Copy templates

Package root = `applicationId` (`project-settings.json`). Multi-module placement:

| Template | Module / path |
|----------|---------------|
| [templates/SonicFcmManager.kt](templates/SonicFcmManager.kt) | `:core-platform` `firebase/fcm/` |
| [templates/FcmTopics.kt](templates/FcmTopics.kt) | `:core-platform` `firebase/fcm/topic/` |
| [templates/TokenType.kt](templates/TokenType.kt) | `:core-platform` `firebase/fcm/topic/` |
| [templates/NotificationType.kt](templates/NotificationType.kt) | `:core-platform` `firebase/fcm/model/` |
| [templates/NotificationPayload.kt](templates/NotificationPayload.kt) | `:core-platform` `firebase/fcm/model/` |
| [templates/NotificationPayloadMapper.kt](templates/NotificationPayloadMapper.kt) | `:core-platform` `firebase/fcm/notification/` |
| [templates/NotificationNavigation.kt](templates/NotificationNavigation.kt) | `:core-platform` `firebase/fcm/notification/` |
| [templates/NotificationChannelHelper.kt](templates/NotificationChannelHelper.kt) | `:core-platform` `firebase/fcm/notification/` |
| [templates/NotificationSender.kt](templates/NotificationSender.kt) | `:core-platform` `firebase/fcm/notification/` |
| [templates/SonicMessagingService.kt](templates/SonicMessagingService.kt) | `:core-platform` `firebase/fcm/notification/` |
| [templates/firebase_notification_view.xml](templates/firebase_notification_view.xml) | `:core-platform` `res/layout/` |
| [templates/firebase.xml](templates/firebase.xml) | `:core-platform` `res/values/` (channel id + name; the only strings allowed outside the shared file because the system shows them, not the app UI) |

Also add a monochrome small icon `ic_fcm_notification_small` (vector or density PNGs, white on transparent) to the same module — never the launcher icon.

Rules inside the templates:

- `SonicFcmManager` is an **`object` with no DI probes**: callers pass `languageCode` and `isPremium` (`07` — never `GlobalContext` / service-locator reads from the manager)
- Topic names are sanitized to the FCM regex `[a-zA-Z0-9-_.~%]` (`FcmTopics.sanitize`) — QiblaCompass lists contain spaces / accents that FCM rejects; the template fixes them
- All-users topic = `context.packageName` (`applicationId`)
- Language topic name = the catalog **English name** from `reference/languages.md` (`LanguageDataSource` / `DpLanguages` `name`); keep `FcmTopics` language pairs in sync with that catalog
- `NotificationNavigation` uses the **launch intent** (`StartupActivity` is the launcher) — never a direct `MainActivity` intent (`23-app-startup`)
- `NotificationSender` is the **only** place that touches `NotificationManager`; images load with Glide after the first `notify`
- Re-engagement gate: `days < 0` → exact day match, `days > 0` → on/after, `0` → never; days spent = now − `PREF_REENGAGEMENT_DATE`
- Logs: `TAG_FCM`, `ClassName: functionName: State: details`; **never log the FCM token** (`14-security-secrets`, `22`)

---

## Step 3 — Manifest (module that hosts the service)

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application>
    <!-- after activities, in the services block (10-manifest order) -->
    <service
        android:name=".firebase.fcm.notification.SonicMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>

    <meta-data
        android:name="com.google.firebase.messaging.default_notification_channel_id"
        android:value="@string/default_notification_channel_id" />
    <meta-data
        android:name="com.google.firebase.messaging.default_notification_icon"
        android:resource="@drawable/ic_fcm_notification_small" />
</application>
```

`exported="false"` is correct for `FirebaseMessagingService` (Play services binds it internally). Use the full class name when the module namespace differs from `applicationId`.

---

## Step 4 — Prefs

On `SharedPrefManager` (`:data`, `shared-preferences.md`) add:

```kotlin
var reengagementDate: Long
    get() = sharedPreferences.getLong(Constants.PREF_REENGAGEMENT_DATE, 0L)
    set(value) = sharedPreferences.edit { putLong(Constants.PREF_REENGAGEMENT_DATE, value) }
```

Expose through the existing `SharedPrefRepository` (domain). `StartupFragment` / `StartupScreen` (via its ViewModel / UseCase) sets it **once** when `0L` — QiblaCompass reads this key but never writes it, so the re-engagement gate never fires there; this skill fixes that.

---

## Step 5 — Hosts

### 5a MainActivity (every open)

`MainActivity.onCreated()` (xml) or `onCreate` after `setContent` (compose), off Main:

```kotlin
lifecycleScope.launch(Dispatchers.Default) {
    SonicFcmManager.setupFcm(
        context = applicationContext,
        languageCode = sharedPrefRepository.appLanguageCode,
        isPremium = sharedPrefRepository.isAppPurchased,
    )
}
```

Read `appLanguageCode` / `isAppPurchased` via the domain `SharedPrefRepository` (or the Activity's ViewModel) — never `:data` from `:presentation` / `:feature-*`. Not from `StartupActivity`: the funnel may still change language / premium.

### 5b Language (Startup + in-app)

`StartupLanguageFragment` **Next** and `LanguageFragment` **Apply** (only when the code changed), before persisting the new code (`30-screen-language`):

```kotlin
lifecycleScope.launch(Dispatchers.Default) {
    SonicFcmManager.updateLanguage(old = currentCode, new = selectedCode)
}
```

Compose: same call from the `*Screen` effect handler (`LanguageEffect.LanguageApplied(old, new)`), not inside `*ScreenContent`.

### 5c Premium (Startup + in-app)

On purchase success / restore in `StartupPremiumFragment` and `PremiumFragment` (`33-screen-premium` "After purchase", together with `isAppPurchased = true` and ad-counter reset):

```kotlin
lifecycleScope.launch(Dispatchers.Default) {
    SonicFcmManager.updatePremium(isPremium = true)
}
```

Entitlement loss (subscription expired, detected by billing) → `updatePremium(false)` from the same billing success/refresh path.

### 5d POST_NOTIFICATIONS (API 33+)

At the host agreed in Step 0, through the base helper — never a raw launcher in a random Fragment (`20-permissions-runtime`):

```kotlin
askPostNotificationPermission { granted -> /* no-op or analytics */ }
```

Compose: one `rememberLauncherForActivityResult` in that `*Screen`. Ask once; respect denial.

---

## Step 6 — R8 + Verify

`src/main/keepRules/*.keep` needs nothing extra (no reflection) unless the payload becomes `@Keep`-less serialized data.

- [ ] `firebase-messaging` in catalog + `implementation` on the hosting module
- [ ] Service registered with `MESSAGING_EVENT`, `exported="false"`; channel + icon meta-data; `POST_NOTIFICATIONS` declared
- [ ] `SonicMessagingService` drops messages without notification permission; maps only data payloads
- [ ] `NotificationPayloadMapper` returns `null` on missing `title` / `body` / type-specific key (`url`, `days`)
- [ ] Topic names pass `FcmTopics.sanitize`; all-users topic = `applicationId`
- [ ] `setupFcm` from `MainActivity` only; `updateLanguage` on both Language screens; `updatePremium` on both paywalls
- [ ] `reengagementDate` written once at Startup; sender gate works for `-N` / `+N` / `0`
- [ ] No token / PII in logs; `TAG_FCM` format
- [ ] Day/night text colors in the custom view; layout works on wide (landscape) notifications
- [ ] Unit test `NotificationPayloadMapper` + `FcmTopics.sanitize` when `writeTestsWithFeatures: true`
- [ ] Test send from Firebase console → **data** message with `type`, `title`, `body` (+ `url` / `days` / `icon` / `feature`)

## Payload contract (Firebase console / backend)

| Key | Required | Notes |
|-----|----------|-------|
| `type` | no (defaults `GENERAL`) | `GENERAL` / `EXTERNAL_LINK` / `REENGAGEMENT` |
| `title`, `body` | yes | Plain text |
| `icon` | no | Image URL (60dp square) |
| `feature` | no | Large image URL below the text |
| `url` | `EXTERNAL_LINK` | Opened with `ACTION_VIEW` |
| `days` | `REENGAGEMENT` | `-N` exact day, `N` on/after, `0` never |

## Do not

- Put FCM in MVI (Intent / State / Effect) or a ViewModel — platform `object` + Service only
- Read prefs / billing inside `SonicFcmManager` via `GlobalContext` or a DI façade — pass values in
- Call `subscribeToTopic` from `StartupActivity` / `Application`
- Log or persist the FCM token; send it to any backend without the user asking
- Use `notification` (display) messages — the custom view needs **data** messages
- Hardcode channel copy in Kotlin; add Analytics / Remote Config here (`sonicc-setup-firebase-events`, `sonicc-setup-firebase-remote-config`)
- Rewrite an existing live `FirebaseMessagingService` without approval
