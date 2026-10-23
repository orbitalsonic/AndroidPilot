---
description: Firebase, Analytics events, Remote Config cache, connectivity
paths:
  - "**/firebase/**"
  - "**/remoteConfig/**"
  - "**/network/**"
  - "**/core/platform/**"
  - "**/core/common/**"
---

## Module placement

| Concern | Module |
|---------|--------|
| `InternetManager` | `:core-platform` |
| `PlatformFirebase` object | `:core-platform` |
| **`firebase-messaging` dependency** | `:core-platform` (mandatory on new projects via `sonicc-setup-new-project`; no service in template) |
| Event name constants (`EventsProvider`) | `:core-common` |
| Remote Config DataSource | `:data` |
| RC cache in `SharedPrefManager` | `:data` |
| `RemoteConfigRepository` / `SharedPrefRepository` | domain interface + data impl |

## PlatformFirebase

Speak-Translate shape (`:core-platform` `firebase/PlatformFirebase.kt` — see `sonicc-setup-new-project` template):

```kotlin
object PlatformFirebase {
    fun Throwable.recordException(log: String)  // Log.e + Crashlytics.log + recordException
    fun postScreenView(screenName: String, screenClass: String)  // screen_view + SCREEN_NAME + SCREEN_CLASS
    fun postUiClick(screenName: String, elementName: String, elementType: String)
    fun getDeviceToken()                      // FirebaseInstallations; log Success/Failed, never the token
}
```

- **`object` — no Context field/constructor**
- Logs: `TAG_FIREBASE` per `16-logging`
- Ads: optional `fun Float.logRevenueEvent(context: Context)` — Context as an argument only; this app’s prefs keys
- Do not call Firebase SDKs from Fragments when this wrapper exists

## Firebase Cloud Messaging (new projects)

- **`sonicc-setup-new-project`** must add `firebase-messaging` to the catalog + `implementation(libs.firebase.messaging)` on **`:core-platform`** only
- Dependency only — **no** `FirebaseMessagingService`, manifest FCM entries, or token code in the template bootstrap
- Later push handling: separate feature work; optional dep-only add for existing apps → skill `sonicc-implement-firebase-messaging`

## Analytics events

- Keys in `EventsProvider` (`:core-common`). String values are lowercase snake_case (`home_screen`). Event names are the closed list in `firebase/events.md` (`screen_view`, `ui_click`, …).
- `PlatformFirebase.postScreenView(EventsProvider.HOME_SCREEN, …)` — no raw event string literals in UI
- Manifest: `google_analytics_automatic_screen_reporting_enabled` = `false`

## Remote Config + SharedPreferences cache

1. `RemoteConfigDataSource`: `Mutex` + lazy SDK; **`minimumFetchIntervalInSeconds(0L)`**; `setConfigSettingsAsync().await()` then `fetchAndActivate().await()`; **`addConfigUpdateListener`**; getters `getLong` / `getInt` / `getBoolean` / `getString` with defaults (`runCatching`)
2. `RemoteConfigRepositoryImpl` (IO dispatcher): internet check → fetch → if activated **`saveValues()` into `SharedPrefManager`** → register listener once (`listenerRegistered`)
3. Runtime feature flags: read from **prefs cache** (`SharedPrefRepository` / `SharedPrefManager`), not from Fragments talking to RC SDK
4. Offline / failed fetch: keep last cached prefs values
5. Logs: `TAG_REMOTE_CONFIG` (`Success: activated=$activated`)
6. `:data` needs `kotlinx-coroutines-play-services` for `Task.await()`

DataSource: **no** dispatcher. Repository: `withContext(ioDispatcher)`.

Adding keys later (existing cache): skill **`sonicc-add-firebase-remote-config`**.

## Connectivity

- Check `InternetManager` before RC/network when UI must show offline state
- Map offline to typed error / Effect

## Forbidden

- `PlatformFirebase` holding `Context` (ads revenue may take `Context` as a parameter)
- `addLiveUpdateListener` — use `addConfigUpdateListener`
- RC SDK calls from feature Fragments
- Parallel event-name constants outside `EventsProvider`
- Logging PII, Installation tokens, or FCM tokens