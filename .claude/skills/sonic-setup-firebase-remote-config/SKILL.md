---
name: sonic-setup-firebase-remote-config
description: OrbitalSonic Claude Skill: First-time Firebase Remote Config with SharedPreferences cache, FetchRemoteConfigUseCase, and StartupFragment / StartupScreen fetch. Use when adding the RC stack for the first time, fetchAndActivate, or /sonic-setup-firebase-remote-config — not for extra keys on an existing cache (use sonic-add-firebase-remote-config) and not for analytics events.
---

# Implement Firebase Remote Config (first time)

Follow `.claude/rules/22-platform-firebase.md`, `26-data-persistence.md`, `08-gradle.md`, `07-dependency-injection.md`, `16-logging.md`, `00-global.md`.  
SharedPrefs shape: [`.claude/rules/reference/shared-preferences.md`](../rules/reference/shared-preferences.md).  
Setup overview: `sonic-setup-new-project` Step 8.  
**Requires human approval** before adding `firebase-config` if it is not in the catalog.

Obey `.claude/project-settings.json` when present (`writeTestsWithFeatures`).

## Goal

Complete RC stack (Speak-Translate DataSource + cache-to-prefs):

1. Fetch/activate (`minimumFetchIntervalInSeconds(0)`)
2. Cache every needed key into `SharedPrefManager`
3. Runtime reads from **prefs cache**, never RC SDK in Fragments / Screens
4. Invoke from **StartupFragment** (xml) or **StartupScreen** (compose) (non-blocking)

## Entry

| App state                                                            | Action                                                                                                             |
|----------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| DataSource + Repository + prefs cache + Startup fetch already wired | Stop. Report what exists. New keys → **`sonic-add-firebase-remote-config`**.                                             |
| Stubs from `sonic-setup-new-project`                                       | Complete missing pieces only.                                                                                      |
| No RC                                                                | Continue.                                                                                                          |
| Legacy helper (`RemoteConfiguration` in Application)                 | Do **not** rewrite unless the user approves. Offer to add cache + Startup fetch into the existing helper instead. |

Single-module: same class names, existing packages. Do **not** create `:data` / `:domain` modules without approval.

---

## Step 0 — RC keys (mandatory AskUserQuestion)

Audit ads / premium / feature flags. Propose a table (Firebase key, type, pref property). Do **not** copy Qibla compass-specific keys.

**AskUserQuestion:** confirm / add / drop keys. Wait. Then code.

Typical keys (only if the app has that surface): `appOpen`, `banner*`, `inter*`, `native*`, `counter*`, `showPremiumFirstTime`.

---

## Step 1 — Catalog + modules

Latest stable, `# Firebase` / `// Firebase` sections (`sonic-gradle-organize`).

```toml
firebaseBom = "…"   # latest stable
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-config = { group = "com.google.firebase", name = "firebase-config" }

# Kotlin Coroutines
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }
```

```kotlin
// Firebase
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.config)   // :data (or :app if single-module)

// Kotlin Coroutines
implementation(libs.kotlinx.coroutines.play.services)  // Task.await()
```

`:app` still needs `google-services` + `google-services.json`. This skill does not add FCM.

If `InternetManager` is missing, add [templates/InternetManager.kt](templates/InternetManager.kt) to `:core-platform` `network/` (or existing network package).

Ensure `:core-common` `Constants` has `TAG` and `TAG_REMOTE_CONFIG` (`16-logging`).

---

## Step 2 — SharedPref cache properties

On `SharedPrefManager` (create per `shared-preferences.md` if missing):

```kotlin
val appOpen = "appOpen"   // Firebase RC key AND prefs key

var rcAppOpen: Int
get() = sharedPreferences.getInt(appOpen, 0)
set(value) = sharedPreferences.edit { putInt(appOpen, value) }
```

Repeat per confirmed key (`getBoolean` / `getString` when type is not int). DataSource is **sync**, no dispatcher.

---

## Step 3 — Data + domain

Copy templates; set package to `applicationId`. Paths (multi-module):

| File                                                                               | Module                               |
|------------------------------------------------------------------------------------|--------------------------------------|
| [templates/RemoteConfigDataSource.kt](templates/RemoteConfigDataSource.kt)         | `:data` `remoteConfig/dataSource/`   |
| [templates/RemoteConfigRepository.kt](templates/RemoteConfigRepository.kt)         | `:domain` `repository/remoteConfig/` |
| [templates/RemoteConfigRepositoryImpl.kt](templates/RemoteConfigRepositoryImpl.kt) | `:data` `remoteConfig/repository/`   |
| [templates/FetchRemoteConfigUseCase.kt](templates/FetchRemoteConfigUseCase.kt)     | `:domain` `usecase/remoteConfig/`    |

**DataSource (Speak-Translate):** `Mutex` + lazy `FirebaseRemoteConfig`; `minimumFetchIntervalInSeconds(0L)`; `setConfigSettingsAsync().await()` then `fetchAndActivate().await()`; **`addConfigUpdateListener`**; getters `getLong(key, default)` / `getInt(key)` / `getBoolean(key, default)` / `getString(key, default)`.

Fill `saveValues()` from Step 0 keys (`remoteConfigDataSource.getInt(appOpen)` → `rcAppOpen = …`). Save **only when activate succeeds** (offline / failed fetch keeps last prefs). Register the live listener **once** in the repository (`listenerRegistered`) via `addConfigUpdateListener { saveValues() }`.

Needs `kotlinx-coroutines-play-services` on `:data` (and `:core-platform` if it uses `Task.await()`).

Logs: `TAG_REMOTE_CONFIG`, `ClassName: functionName: State: details`.

---

## Step 4 — DI (cold-start → eager `module`)

RC + SharedPref are fetched from Startup — use **`module`** on the eager list (`07`).

```kotlin
val dataModule = module {
    //// DataSources
    single { RemoteConfigDataSource() }
    single { SharedPrefManager(androidContext()) }

    //// Repositories
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get(), get()) }
}

val useCaseModule = module {
    //// RemoteConfig
    factory { FetchRemoteConfigUseCase(get()) }
}
```

Register on the **eager** composition-root list. Presentation must not depend on `:data`.

---

## Step 5 — StartupFragment / StartupScreen

After Koin is ready (same place as billing init if present):

```kotlin
viewModel.handleIntent(StartupIntent.FetchRemoteConfig)
```

ViewModel:

```kotlin
is StartupIntent.FetchRemoteConfig -> fetchRemoteConfigUseCase()
```

No `FetchRemoteConfig` start/success ViewModel logs (repository already logs).

Without Startup ads, navigate when this use case returns or after **5 seconds** (`sonic-setup-new-project`). When `sonic-setup-admob-ads` wires Startup ads, still start this use case, ignore its result, and navigate only after consent (8s) → AdMob init → ad calls (8s).

If there is no `StartupFragment` / `StartupScreen`: **AskUserQuestion** for the host (prefer the `StartupActivity` start destination — `nav_graph_startup` / `StartupNavGraph`). Do not fetch only from `Application` unless the user says so.

---

## Step 6 — Verify

- [ ] `firebase-config` in catalog + module `implementation`
- [ ] `kotlinx-coroutines-play-services` on `:data`
- [ ] `minimumFetchIntervalInSeconds(0L)` + `Mutex` + `.await()`
- [ ] `addConfigUpdateListener` (repository owns `listenerRegistered`)
- [ ] `saveValues()` writes every confirmed key to `SharedPrefManager` when activate succeeds
- [ ] Features read cache via `SharedPrefRepository` / manager — not RC SDK in UI
- [ ] `FetchRemoteConfigUseCase` in `:domain`; repository interface in `:domain`
- [ ] Startup (or agreed host) calls the UseCase
- [ ] `InternetManager` check before fetch
- [ ] Eager `module` for RC/prefs (cold-start); not deferred-only `lazyModule`
- [ ] Unit test `FetchRemoteConfigUseCase` with fake repo if `writeTestsWithFeatures: true`

## Do not

- `addLiveUpdateListener` (use `addConfigUpdateListener`)
- Read Remote Config from Fragments / Screens
- `PlatformFirebase` with `Context`
- `remote_config_defaults.xml` unless the user asks
- Rewrite a working legacy RC helper without approval
- Extra keys on an existing cache (`sonic-add-firebase-remote-config`)
- Analytics / `EventsProvider` (use `sonic-setup-firebase-events`)
- `FirebaseMessagingService` / FCM UI
