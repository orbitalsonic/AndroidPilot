---
name: sonicc-setup-new-project
description: Sonic Cursor Skill: Bootstrap a new Android multi-module app (domain, data, core-*, xml `:presentation` or compose `:feature-*`) with StartupActivity (funnel) + MainActivity (app), StartupFragment start destination, Parent* (InsetsMode edge-to-edge) or Compose StartupNavGraph / MainNavGraph, PlatformFirebase, Remote Config → SharedPreferences cache, and mandatory firebase-messaging on core-platform. Use when starting a greenfield project. Do not use on an existing production app — use sonicc-setup-old-project. Confirms and persists project settings first including uiFramework.
---

# Setup New Project

Follow `.cursor/rules/` — especially `00-global`, `02-project-structure`, `07-dependency-injection`, `08-gradle`, `09-resources-xml`, `17-navigation`, `19-base-ui`, `22-platform-firebase`, `23-app-startup`, `26-data-persistence`, `28-compose-ui` (when compose).

## Preconditions (ask if missing — then persist)

Confirm with the user before scaffolding. Write answers to **`.cursor/project-settings.json`**:

```json
{
  "writeTestsWithFeatures": true,
  "orientation": "both",
  "themeModes": "both",
  "applicationId": "com.company.app",
  "appName": "App Display Name",
  "uiFramework": "xml",
  "figmaDesignSystemUrl": ""
}
```

| Setting                        | Allowed values                    | Effect                                                                                                   |
|--------------------------------|-----------------------------------|----------------------------------------------------------------------------------------------------------|
| `applicationId` / root package | e.g. `com.company.app`            | Module namespaces, package roots                                                                         |
| `appName`                      | display name                      | Launcher label / strings                                                                                 |
| `writeTestsWithFeatures`       | `true` / `false`                  | Whether later skills add UseCase/ViewModel tests with features                                           |
| `orientation`                  | `portrait` / `landscape` / `both` | Layout orientation support (default `both`)                                                              |
| `themeModes`                   | `day` / `night` / `both`          | Theme resource folders (default `both`)                                                                  |
| `figmaDesignSystemUrl`         | Figma `/design/` URL or `""`      | Optional; set when user picks design-system option **a**                                                 |
| `uiFramework`                  | `xml` / `compose`                 | **Ask first.** xml = View Binding + `:presentation`. compose = Jetpack Compose + `:feature-*` (AnimeHub) |

Also ask:

0. **`uiFramework`** — `xml` or `compose`. Show both options. Default is `xml`. Compose follows AnimeHub (`:feature-*`, `:app` `StartupNavGraph.kt` + `MainNavGraph.kt`) — see `28-compose-ui` + [templates/compose/](templates/compose/).
1. **Ads** — always place `:gmaAds` from GitHub (see **Step — Place `:gmaAds`**). Ask: **Implement ads on screens now?**
    - **yes** — after modules/DI exist, run **`sonicc-implement-admob-ads`** (wire existing screens only; report changed Fragments/`*Screen`s)
    - **no** — keep `:gmaAds` in the project (builds) but do **not** wire screens
      Never convert ads to MVI unless the user **explicitly** asks. See `21-ads-billing` + [reference/ads-gma.md](../../../rules/reference/ads-gma.md).
2. **Firebase Cloud Messaging** is **mandatory** for every new project: add `firebase-messaging` to the catalog + `implementation` on `:core-platform` only (dependency — no `FirebaseMessagingService` or push UI). See **`sonicc-implement-firebase-messaging`**.
3. **Design system (Figma)** — ask the user to pick:
    - **a)** Provide a Figma link to build the design system (`figma.com/design/...`)
    - **b)** Ignore design system now (later)

   If **a**: save the URL as `figmaDesignSystemUrl` in `project-settings.json`. After Step 6 (`:core-ui` exists), run **`sonicc-setup-design-system`** with that URL (theme-first tokens in `:core-ui`).  
   If **b**: leave `figmaDesignSystemUrl` empty; scaffold default Material3 `themes.xml` / `colors.xml` in Step 6. User can invoke `/sonicc-setup-design-system` later.

All later skills **must read** `.cursor/project-settings.json` and obey it.

## Module set (mandatory)

| Module              | Required     | Role                                                                                                                                          |
|---------------------|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `:app`              | Must         | `App`, DI aggregation, full manifest — **res:** `mipmap`, `xml`, launcher `drawable` only — **no** `values/` / layouts / menus / nav |
| `:domain`           | Must         | Entities, repository interfaces, use cases                                                                                                    |
| `:data`             | Must         | Repository impls, DataSources, SharedPref + RC cache                                                                                          |
| `:presentation`     | xml only     | Screens, MVI — **res:** `layout` (+ land), `menu`, `navigation` only. **Omit when compose**                                                   |
| `:feature-startup` | compose only | `StartupActivity` funnel: `StartupScreen` (`STARTUP_ROUTE` start) + later `StartupLanguage` / `StartupPremium` / `StartupIntro` / `StartupWelcomeBack` screens |
| `:core-common`      | Required     | `Constants` (TAGs), `EventsProvider`                                                                                                          |
| `:core-ui`          | Required     | **All other resources** (themes/strings/colors/fonts/anim/drawable/splash), Parent*, extensions                                               |
| `:core-platform`    | Required     | `InternetManager`, `PlatformFirebase`, dispatchers DI, **Firebase BOM + analytics / crashlytics / messaging** — manifest `ACCESS_NETWORK_STATE` only when needed |
| `:gmaAds`           | Must         | AdMob module from [orbitalsonic/Admob-Ads](https://github.com/orbitalsonic/Admob-Ads) — place always; screen wiring only if user said **yes** |
| `:core-database`    | On demand    | **When Room is added** — not empty on greenfield                                                                                              |
| `:core-network`     | On demand    | **When Retrofit/API is added** — not empty on greenfield                                                                                      |

```
app (Composition Root) — no values resources
 |
 ↓
xml: presentation → domain ← data
compose: feature-* → domain ← data
 |
 ↓
core-common / core-ui / core-platform / gmaAds
```

## Step — Place `:gmaAds` (mandatory)

Do this once modules and host types (`Constants.TAG_ADS`, `InternetManager`, `SharedPrefManager`) exist (after core/data scaffold). Full detail: [ads-gma.md](../../../rules/reference/ads-gma.md).

1. Download `gmaAds` from `https://github.com/orbitalsonic/Admob-Ads` — exact copy; do not invent a parallel ads module.
2. `include(":gmaAds")`; UI modules that show ads depend on `:gmaAds` when wiring (presentation / feature / app as needed).
3. Rename package / `namespace` only → `{applicationId}.gmaAds`.
4. Remap Gradle (`:core` / `:data` → `:core-common` + `:core-platform` + `:data`) and host-only imports. Catalog: `play-services-ads`, UMP.
5. Register `gmaAdsModule` (`lazyModule` — deferred); AdMob App ID in `:app` manifest (sample for debug).
6. During place: **do not** edit engine or catalog inside `:gmaAds`.
7. If user said **yes** to implement → run **`sonicc-implement-admob-ads`**. If **no** → stop after place (module only).

## Step 1 — Gradle

Follow `08-gradle.mdc` + [reference/gradle.md](../../../rules/reference/gradle.md) (canonical `:app` / library scripts) and **`sonicc-gradle-organize`** for catalog + dependency sections.

1. `settings.gradle.kts` — `include` all modules above (including `:gmaAds`) in **alphabetical** ascending order
2. Root plugins `apply false` via catalog; **latest stable AGP 9.3+**. Do **not** apply `org.jetbrains.kotlin.android` — AGP has built-in Kotlin. `compileSdk { version = release(37) { minorApiLevel = 1 } }`; `targetSdk = 37`; `compileOptions` `VERSION_21`; first build `versionName = "1.0.1"`.
3. Catalog sections/naming per `08-gradle.mdc` / `sonicc-gradle-organize`
4. Dependency graph: UI modules (`:presentation` or `:feature-*`) **never** → `:data`; `domain` → coroutines only
5. **xml:** View Binding on UI modules; Safe Args on `:presentation`. **compose:** Compose Compiler plugin (`kotlin-compose`) + `buildFeatures { compose = true }` on `:app`, `:core-ui`, `:feature-*` — **not** `kotlin-android`; Compose BOM + Navigation Compose + Coil 3 + `koin-androidx-compose` in catalog (latest stable). No View Binding on feature modules.
6. **Remove** `:app` `src/main/res/values/` (and night) — move themes/strings/colors into `:core-ui`
7. `:app` may keep only `mipmap` / `xml` / launcher `drawable` — **no** layouts, menus, nav, `strings.xml` / `themes.xml` / `colors.xml`
8. **Manifests:** only `:app` (full), `:gmaAds`, `:core-platform` (network state) as needed — **no** empty manifests on every module (`10-manifest`)
9. **Every module** gets a `.gitignore`: libraries → `/build`; `:app` → `/build` + `/release` (see `02-project-structure`)
10. **Firebase (mandatory):** catalog BOM + analytics / crashlytics / **messaging** on **`:core-platform`**; **config** + `kotlinx-coroutines-play-services` on **`:data`**. Latest stable. **No** `FirebaseMessagingService` / FCM manifest / token UI during setup (see **`sonicc-implement-firebase-messaging`**)

Organize dependency sections with **`sonicc-gradle-organize`**.

### Module scripts (scaffold from reference)

Copy the `:app` and library shapes from [reference/gradle.md](../../../rules/reference/gradle.md) — module order `plugins` → `android` → (`base` on `:app`) → `dependencies`.

**`:app` must include**

| Item                    | Rule                                                                                                                                                                                                                                                                                              |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `android` section order | `defaultConfig` → `signingConfigs` → `buildTypes` → `buildFeatures` → `compileOptions` → `bundle`                                                                                                                                                                                                 |
| `signingConfigs`        | Always — search `*.jks` in root then `app/`; set `storeFile` if found; else empty strings; do not invent passwords                                                                                                                                                                                |
| `bundle`                | Always `language { enableSplit = false }`                                                                                                                                                                                                                                                         |
| `base.archivesName`     | `AppName-Account-v{versionCode}({versionName})` from `project-settings.json` `appName` + account when known                                                                                                                                                                                       |
| First versions          | `versionCode = 1`, `versionName = "1.0.1"`                                                                                                                                                                                                                                                        |
| R8                      | `:app` release `optimization { enable = true }` (code **and** resource shrinking). Keep rules in `src/main/keepRules/*.keep` on `:app` and on library modules that need them. Copy [templates/keepRules/](templates/keepRules/). **No** `proguardFiles` / `isMinifyEnabled` / `isShrinkResources` |

**Library modules** (`:presentation` xml, `:feature-*` compose, `:data`, `:domain`, `:core-*`): same relative order; **omit** `signingConfigs`, `bundle`, `base`, and app-only `defaultConfig` fields. xml UI modules get View Binding; compose UI modules get `compose = true`; `:domain` / `:core-common` may omit `buildFeatures`.

## Step 2 — Application + DI

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin()
        onKoinStarted()
    }

    private fun startKoin() {
        startKoin {
            androidContext(this@App)
            modules(KoinModules().getEagerModules())
            lazyModules(KoinModules().getLazyModules())
        }
    }

    private fun onKoinStarted() {
        getKoin().runOnKoinStarted {
            applyAppTheme()
            // billing connect, ads preload, etc. — lazyModule bindings
        }
    }
}
```

- **Eager `module`** (cold-start): `appModule`, `coreModule`, `corePlatformModule`, `dataModule`, `useCaseModule`, `startupPresentationModule` (xml) or `startupFeatureModule` (compose)
- **Deferred `lazyModule`**: secondary features, `gmaAdsModule` / placement modules, optional heavy graphs (`07-dependency-injection`)
- Theme / billing that needs **lazy** bindings: inside **`runOnKoinStarted`** (`23-app-startup`); eager-only work can run right after `startKoin`
- Manifest: `android:name=".App"`, **application** `android:theme="@style/Theme.App"` (product theme), **`StartupActivity`** = launcher with `android:theme="@style/Theme.App.Starting"`, **`MainActivity`** `exported="false"` + `launchMode="singleTask"`; child order StartupActivity → MainActivity → services → receivers → meta-data (`10-manifest`); `supportsRtl="true"`
- Orientation: follow `project-settings.json` — default portrait **and** landscape; do not lock unless `orientation` is single-mode and product requires lock
- Theme modes: create `values` / `values-night` per `themeModes`
- UseCases + repo interfaces → `:domain`; DataSources + repo impls → `:data` (`dataModule` with `//// DataSources` / `//// Repositories`)

## Step 3 — Two Activities (xml)

Copy [templates/startup/](templates/startup/README.md) (replace `YOUR.PACKAGE`). Rules: `23-app-startup`, `29-screen-startup`, `32-screen-dashboard`. Reference app: QiblaCompass.

| | `StartupActivity` | `MainActivity` |
|---|---|---|
| File | `presentation/startup/ui/StartupActivity.kt` | `presentation/main/ui/MainActivity.kt` |
| Base | `BaseActivity` → `ParentActivity` (`InsetsMode` default `TOP_BOTTOM`) | same |
| Layout | `activity_startup.xml` — `fcvContainerStartup` + `@navigation/nav_graph_startup` | `activity_main.xml` — `fcvContainerMain` + `@navigation/nav_graph_main` (+ `bannerAdViewMain` slot + `bnvMain` when the product has tabs) |
| Manifest | launcher, `exported="true"`, `@style/Theme.App.Starting` | `exported="false"`, `launchMode="singleTask"` |
| Splash | `override fun initSplashScreen() { installSplashScreen() }` | none |
| Back | `onBackPressedDispatcher { }` — blocked on every funnel destination | `handleBackPress()` (non-tab pop → Home tab → exit) |
| Leaves via | `moveToMain()` → `startActivity(MainActivity)` + `finish()` | app exit |

- `updateStatusBar(darkIcons)` in `onCreated()`; per-destination host background via `addOnDestinationChangedListener` when designs differ (premium vs surface)
- NavController: `lazy { (supportFragmentManager.findFragmentById(binding.fcvContainerX.id) as NavHostFragment).navController }`
- Wait for Koin if resolving **lazy** deps at start (`runOnKoinStarted` / equivalent)
- No `SettingsActivity` / `PremiumActivity` / `LanguageActivity` — in-app Settings / Language / Premium are Fragments in `nav_graph_main`

**compose:** copy [templates/compose/StartupActivity.kt](templates/compose/StartupActivity.kt) + [templates/compose/MainActivity.kt](templates/compose/MainActivity.kt) into `:app` `ui/`. `ComponentActivity` + `setContent { AppTheme { StartupNavGraph(onFinished = …) } }` / `{ MainNavGraph() }`. **Never** `GlobalContext.get()`.

## Step 4 — Navigation (two graphs)

**xml:** two graphs in `:presentation` `res/navigation/` (templates in [templates/startup/](templates/startup/)):

- `nav_graph_startup.xml` — `app:startDestination="@id/startupFragment"`. Class `StartupFragment` under `presentation/startup/ui/`, layout `fragment_startup.xml`. Actions out of `startupFragment` use `popUpTo="@id/startupFragment"` + `popUpToInclusive="true"`. Later destinations (added by `sonicc-create-screen` / `sonicc-create-mvi`): `startupLanguageFragment`, `startupPremiumFragment` (`isFirstTime` arg), `startupIntroFragment`, `startupWelcomeBackFragment` — nothing else
- `nav_graph_main.xml` — `startDestination` = Home (first tab). Scaffold a minimal `HomeFragment` + `fragment_home.xml` placeholder so the app launches; `sonicc-create-screen` replaces it. In-app `languageFragment` / `premiumFragment` / `settingsFragment` / `exitFragment` and details join this graph later (`30`, `33`)
- No Home / Splash as the startup start destination; no startup screens in `nav_graph_main`. Copy anims from [templates/anim/](templates/anim/) + [templates/anim-ldrtl/](templates/anim-ldrtl/) into `:core-ui`. Every forward `<action>` must include the four slide anim attrs (`17-navigation`). Nested `nav_graph_dashboard` only for the optional `DashboardFragment` variant (`32-screen-dashboard`)

**compose:** copy [templates/compose/StartupNavGraph.kt](templates/compose/StartupNavGraph.kt) + [templates/compose/MainNavGraph.kt](templates/compose/MainNavGraph.kt) to `:app` `navigation/`. `StartupNavGraph` starts at `STARTUP_ROUTE` and takes `onFinished`; `MainNavGraph` starts at `HOME_ROUTE`. Slide `enterTransition` / `exitTransition` on both `NavHost`s. No XML `nav_*.xml`. Feature screens do not receive `NavController`.

### Nav transition anims (xml — mandatory)

Create in **`:core-ui`** (copy from [templates/anim/](templates/anim/) + [templates/anim-ldrtl/](templates/anim-ldrtl/)). Skip this block when `uiFramework` is `compose`.

```
res/anim/slide_in_right.xml | slide_out_left.xml | slide_in_left.xml | slide_out_right.xml
res/anim-ldrtl/… (same four names — RTL mirrors)
```

Every forward `<action>` must include:

```xml
app:enterAnim="@anim/slide_in_right"app:exitAnim="@anim/slide_out_left"app:popEnterAnim="@anim/slide_in_left"app:popExitAnim="@anim/slide_out_right"
```

See `17-navigation.mdc`.

## Step 5 — Startup MVI

**xml:** scaffold `presentation/startup/{di,intent,state,effect,viewModel,ui}` via **`sonicc-create-mvi`**. `StartupFragment` extends `BaseFragment` (`ParentFragment` if no Base layer). Register `startupPresentationModule` on the **eager** list — it also holds `StartupLanguageViewModel` / `StartupPremiumViewModel` / `StartupIntroViewModel` / `StartupWelcomeBackViewModel` as those screens are added (`29-screen-startup`, `30`, `31`, `33`). `StartupFragment` routes per [reference/app-flow.md](../../../rules/reference/app-flow.md) and leaves with `(activity as? StartupActivity)?.moveToMain()`.

**compose:** module `:feature-startup`. Copy [templates/compose/StartupScreen.kt](templates/compose/StartupScreen.kt) + [templates/compose/feature-module.gradle.kts](templates/compose/feature-module.gradle.kts). Then **`sonicc-create-mvi`** Compose path (Intent/State/Effect/ViewModel/`startupFeatureModule`). Wire `navigateToNext` in `StartupNavGraph` when the first funnel destination exists; `navigateToMain` = `onFinished`.

- Strings only in `:core-ui`
- Tests: only if `writeTestsWithFeatures` is `true`

## Step 6 — `:core-ui` Parent* bases (xml) / Compose theme in `:core-ui` (compose)

**compose:** skip ParentFragment / View Binding templates. Copy [templates/compose/core-ui.gradle.kts](templates/compose/core-ui.gradle.kts) shape onto `:core-ui` (add `kotlin-compose` + `compose = true` + Compose BOM). Copy [templates/compose/Color.kt](templates/compose/Color.kt), [Type.kt](templates/compose/Type.kt), [Theme.kt](templates/compose/Theme.kt) into `:core-ui` `…/core/ui/theme/`. Keep XML `strings.xml` / `colors.xml` / splash theme / drawables in `:core-ui` (`09`). If design-system **a**, run **`sonicc-setup-design-system`** (writes full XML tokens **and** maps them into `AppTheme` in the same module). Then continue Step 7.

**xml:** mirror reference hierarchy below.

```
core/ui/base/
  enums/InsetsMode.kt     # EDGE_TO_EDGE / TOP / BOTTOM / TOP_BOTTOM
  activity/ParentActivity.kt
  fragment/ParentFragment.kt
  dialog/ParentDialogDismissal.kt + ParentDialog.kt
  sheet/ParentSheetDismissal.kt + ParentSheet.kt
core-ui …/extensions/
  FragmentExtensions.kt   # viewLifecycleOwner collectWhen* / launchWhen* + navigateTo / popFrom / navigateRootTo
  ActivityExtensions.kt   # Activity collectWhen* / launchWhen* + onBackPressedDispatcher + updateStatusBar(darkIcons)
  ContextExtensions.kt    # showToast(String) / showToast(@StringRes)
  ImageViewExtensions.kt  # loadImage via Glide (ShapeableImageView / ImageView)

presentation …/base/
  activity/BaseActivity.kt
  fragment/BasePermissionFragment.kt + BaseFragment.kt
  sheets/BaseDialog.kt + BaseSheet.kt
presentation …/startup/ui/StartupActivity.kt   # Step 3 (templates/startup/)
presentation …/main/ui/MainActivity.kt         # Step 3 (templates/startup/)
```

See [templates/base/README.md](templates/base/README.md) for hierarchy and notes.

### ParentFragment

- Generic `ViewBinding` + `bindingFactory`
- Clear `_binding` in `onDestroyView`
- Hooks: `onViewInflated()` (after inflate), `initObservers()`, `onViewCreated(Bundle?)`, abstract `onViewCreated()`

### ParentActivity (+ `InsetsMode`)

- Generic `ViewBinding` + edge-to-edge via the **`InsetsMode`** enum (`EDGE_TO_EDGE` / `TOP` / `BOTTOM` / `TOP_BOTTOM` default; IME-aware bottom inset) — reference QiblaCompass `BaseActivity`
- `onCreate` order: `initSplashScreen()` → `super.onCreate` → `setupInsets(getInsetsMode())` (`enableEdgeToEdge` + `setContentView` + insets listener) → `onCreated()` → `initObservers()`
- `open fun initSplashScreen()` — only `StartupActivity` overrides with `installSplashScreen()`; `open fun getInsetsMode()` overridden per Activity (no per-destination padding flags)
- Abstract `onCreated()`; optional `initObservers()`

### ParentDialog (+ Dismissal)

- `ParentDialogDismissal` : `DialogFragment` with `onDismissCallback` + `safeShow` / `safeDismiss` helpers
- `ParentDialog` : ViewBinding via `MaterialAlertDialogBuilder.setView(binding.root)`
- Null-safe binding; clear in `onDestroyView`
- Improvements vs fragile patterns: never access binding after destroy; use `dismissAllowingStateLoss` only via `safeDismiss`

### ParentSheet (+ Dismissal)

- `ParentSheetDismissal` : `BottomSheetDialogFragment` with `dismissCallback` + `safeShow` / `safeDismiss`
- `ParentSheet` : inflate with `bindingFactory`, null-safe `_binding` (same as Fragment — **not** `!!`), `onSheetCreated()`, `initObservers()`
- Improvements: remove unused dialog imports; optional `skipCollapsed` / expanded state in `onStart` when product needs it; keep Binding lifecycle identical to Fragment

Also add: Fragment/Activity/Context/ImageView extensions (`FragmentExtensions` / `ActivityExtensions` / `ContextExtensions.showToast` / `ImageViewExtensions.loadImage` via Glide; Fragment uses `viewLifecycleOwner`), `themes.xml` (include `ButtonStyle.IconButton` parent of `Widget.Material3.Button.IconButton`), **`splash.xml`**, `strings.xml` / `colors.xml` with **app → general → screen-wise** sections (`09-resources-xml`). Add Glide to version catalog + `implementation(libs.glide)` on `:core-ui`.

If the user chose design-system **a**, **stop here and run `sonicc-setup-design-system`** with `figmaDesignSystemUrl` before Step 7. That replaces default Material3 colors/type with Figma tokens and sets `android:windowBackground` — do **not** paint default layouts with `?attr/colorSurface`. If **b**, keep the Step 6 default theme and continue.

## Step 7 — `:core-platform`

### Firebase (catalog + `:core-platform` / `:data`)

Follow **`sonicc-implement-firebase-messaging`** for FCM. Every new project **must** include Messaging; Analytics + Crashlytics + Remote Config match Speak-Translate.

```toml
# Firebase
firebaseBom = "…"   # latest stable
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics" }
firebase-config = { group = "com.google.firebase", name = "firebase-config" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }

# Kotlin Coroutines
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }
```

```kotlin
// Firebase — :core-platform
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)
implementation(libs.firebase.crashlytics)
implementation(libs.firebase.messaging)

// Kotlin Coroutines — :core-platform and :data (Task.await)
implementation(libs.kotlinx.coroutines.android)
implementation(libs.kotlinx.coroutines.play.services)

// Firebase — :data
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.config)
```

On **`:core-platform`**: analytics, crashlytics, messaging. On **`:data`**: config + `kotlinx-coroutines-play-services`. Do **not** add `FirebaseMessagingService`, FCM manifest entries, or token handling during setup.

### Dispatchers (no named qualifiers)

```kotlin
val corePlatformModule = module {

    //// Dispatchers
    single { Dispatchers.IO }
    single { Dispatchers.Default }

    //// Managers
    single { InternetManager(androidContext()) }
}
```

Repository injects by type:

```kotlin
class XRepositoryImpl(
    private val dataSource: XDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
)
```

Koin resolves `Dispatchers.IO` / `Dispatchers.Default` as distinct `CoroutineDispatcher` instances — **do not** use `named("io")` / `named("default")`.

> If both are type `CoroutineDispatcher`, prefer constructor defaults `= Dispatchers.IO` in repos (reference style) **or** inject only IO via DI and use `Dispatchers.Default` explicitly for CPU work. Do **not** introduce named qualifiers.

### PlatformFirebase — `object`, no Context field

Copy [templates/firebase/PlatformFirebase.kt](templates/firebase/PlatformFirebase.kt) → `:core-platform` `firebase/PlatformFirebase.kt`.  
Copy [templates/firebase/EventsProvider.kt](templates/firebase/EventsProvider.kt) → `:core-common` `EventsProvider.kt`.  
Replace `YOUR.PACKAGE`. Naming and which events to post: [events.md](../../firebase/events.md).

```kotlin
object PlatformFirebase {
    fun setEntrySource(source: String)
    fun Throwable.recordException(log: String)  // Log.e + Crashlytics.log + recordException
    fun postScreenView(screenName: String, screenClass: String)  // screen_view + SCREEN_NAME + SCREEN_CLASS
    fun postUiClick(screenName: String, elementName: String, elementType: String)
    fun getDeviceToken()  // Success/Failed, never the token
}
```

- **No** `Context` field / constructor on the object
- Event and screen constants in `:core-common` `EventsProvider` (snake_case string values)
- Manifest: `google_analytics_automatic_screen_reporting_enabled` = `false`
- Do **not** log the Installation token value (`14-security-secrets`)
- Ads revenue (`fun Float.logRevenueEvent(context: Context, threshold: Float = 0.1f)`): add when ads screens are wired (`sonicc-implement-admob-ads` / user said **yes**). Pass `Context` as an argument. Use this app's prefs name / cache key / event string — never copy Speak-Translate `rossPref` / `TaichiTroasCache`

### InternetManager

- Connectivity check used by RC / network repos

## Step 8 — SharedPreferences + Remote Config cache

### SharedPref (`data/sharedPreferences/`)

- `SharedPrefManager(context)` — **sync only, no dispatcher** (see `.cursor/rules/reference/shared-preferences.md` + `26-data-persistence.mdc`)
- Domain `SharedPrefRepository` + `SharedPrefRepositoryImpl` with `withContext(ioDispatcher)`
- Include RC cache properties (ints/bools/strings) written by RC repository

### Remote Config (cache-to-prefs architecture)

```
domain: RemoteConfigRepository { suspend fun fetchAndCache(): Boolean }
data:
  remoteConfig/dataSource/RemoteConfigDataSource.kt   # Firebase RC only
  remoteConfig/repository/RemoteConfigRepositoryImpl.kt
```

**RemoteConfigDataSource** (no dispatcher). Copy [templates/remoteconfig/RemoteConfigDataSource.kt](templates/remoteconfig/RemoteConfigDataSource.kt) (same shape as `sonicc-implement-firebase-remote-config` templates). Replace `YOUR.PACKAGE`.

- `private val fetchMutex = Mutex()` + `by lazy { FirebaseRemoteConfig.getInstance() }`
- `minimumFetchIntervalInSeconds(0L)` always
- `setConfigSettingsAsync(settings).await()` then `fetchAndActivate().await()` (needs `kotlinx-coroutines-play-services`)
- Live listener method name: **`addConfigUpdateListener(onUpdated: () -> Unit)`** — not `addLiveUpdateListener`
- Getters: `getLong(key, default)`, `getInt(key)` (default `0`), `getBoolean(key, default)`, `getString(key, default)` via `runCatching`
- Log with `TAG_REMOTE_CONFIG` (`Success: activated=$activated`)

**RemoteConfigRepositoryImpl** (dispatcher here; `listenerRegistered` lives here, not in the DataSource):

1. Check `InternetManager` — if offline, return `false` (keep last prefs cache)
2. `remoteConfigDataSource.fetchAndActivate()`
3. If **activated**: **`saveValues()`** (copy every needed RC key into `SharedPrefManager`) then `addConfigUpdateListener { saveValues() }` once
4. If fetch fails, prefs still hold last cache — app reads cache via `SharedPrefRepository`

**Read path for features:** Prefer **cached prefs** (`SharedPrefRepository` / managers) for flags used at runtime — not live RC SDK in UI.

**DI (cold-start → eager `module` — sectioned by concern):**

```kotlin
val dataModule = module {

    //// DataSources
    single { RemoteConfigDataSource() }
    single { SharedPrefManager(androidContext()) }

    //// Repositories
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(get()) }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get(), get()) }
}

val useCaseModule = module {

    //// RemoteConfig
    factory { FetchRemoteConfigUseCase(get()) }
}
```

- Interfaces + `FetchRemoteConfigUseCase` in **`:domain`**
- Register both on the **eager** list in `KoinModules` (`07`)

Wire `FetchRemoteConfigUseCase` from Startup. **Navigate when that call returns** (success or failure) **or after 5 seconds**, whichever comes first. Do not block the main thread. Do not leave the splash on the Firebase fetch timeout (~60s).

```kotlin
viewModelScope.launch {
    withTimeoutOrNull(5.seconds) { fetchRemoteConfigUseCase() }
    // navigate
}
```

This 5-second gate is only for Startup **without** an ad startup sequence. When `sonicc-implement-admob-ads` wires Startup ads, that skill replaces it: Remote Config still starts, its result is ignored, and navigation follows consent (8s) → AdMob init → ad calls (8s).

## Step 9 — Verify

- [ ] `.cursor/project-settings.json` written and valid (incl. `uiFramework` + `figmaDesignSystemUrl` if option **a`)
- [ ] Every module has `.gitignore` (`/build`; `:app` also `/release`)
- [ ] No `:app/src/main/res/values/` (themes/strings/colors live in `:core-ui`); `:app` res = mipmap/xml/launcher drawable only; `:presentation` = layout/menu/navigation only
- [ ] **xml** modules: app, domain, data, presentation, core-common, core-ui, core-platform, **gmaAds** (`include` alphabetical)
- [ ] **compose** modules: app, domain, data, feature-startup, core-common, core-ui, core-platform, **gmaAds** — **no** `:presentation`, **no** `:core-design`
- [ ] `:gmaAds` placed from GitHub; package `{applicationId}.gmaAds`; host imports remapped; `gmaAdsModule` registered
- [ ] Ads screens wired only if user said **yes** (`sonicc-implement-admob-ads`); otherwise module-only
- [ ] Manifests only as needed (`:app`, `:gmaAds`, `:core-platform`); application theme = `Theme.App`; `StartupActivity` = launcher with `Theme.App.Starting` and first in `<application>`; `MainActivity` `exported="false"` + `singleTask`; no Settings / Premium / Language Activities
- [ ] `:app` `android` section order: defaultConfig → signingConfigs → buildTypes → buildFeatures → compileOptions → bundle
- [ ] `:app` has `signingConfigs` (`.jks` path if found, else empty strings) + `bundle.language.enableSplit = false` + `base.archivesName`
- [ ] `:app` release `optimization { enable = true }`; `src/main/keepRules/rules.keep` on `:app` and on `:domain` / UI modules; no `proguard-rules.pro`
- [ ] `compileSdk` 37.1 block, `targetSdk = 37`, `versionName = "1.0.1"`, `compileOptions` `VERSION_21`
- [ ] Library modules omit `signingConfigs` / `bundle` / `base`
- [ ] UseCases + repo interfaces only in `:domain`; `dataModule` has `//// DataSources` then `//// Repositories`
- [ ] DI: cold-start graph uses `module` / `modules(...)`; deferred features/ads use `lazyModule` / `lazyModules(...)`; `App` uses `startKoin` then `runOnKoinStarted` for lazy work (no `GlobalContext` probes)
- [ ] Cold-start: Startup (and MainActivity injects if any) ViewModel + full ctor chain registered on the **eager** `KoinModules` list (`07`, `23`, `29`)
- [ ] **xml:** `nav_graph_startup` startDestination = `startupFragment` (actions out of it `popUpToInclusive`) and `nav_graph_main` startDestination = Home placeholder; **compose:** `StartupNavGraph` `STARTUP_ROUTE` + `MainNavGraph` `HOME_ROUTE`
- [ ] `StartupActivity.moveToMain()` starts `MainActivity` and finishes; back blocked in `StartupActivity`; no startup destination inside `nav_graph_main`
- [ ] **xml:** `:core-ui` has `anim/` + `anim-ldrtl/` slide_* set; nav actions use the four anim attrs. **compose:** slide `enterTransition` / `exitTransition` on `NavHost`
- [ ] **xml:** `InsetsMode` enum + ParentActivity (`getInsetsMode()` default `TOP_BOTTOM`, `initSplashScreen()` hook) / ParentFragment (`onViewInflated()`) / ParentDialog / ParentSheet (+ Dismissal) exist
- [ ] **xml:** `FragmentExtensions.kt` + `ActivityExtensions.kt` (`updateStatusBar`, `onBackPressedDispatcher`) + `ContextExtensions.kt` + `ImageViewExtensions.kt` (`showToast` / `loadImage`; Fragment collectors on `viewLifecycleOwner`; `navigateTo` / `popFrom` / `navigateRootTo`)
- [ ] Themes: `includeFontPadding=false`, IconButton `8dp` + `colorIcon`, no status/nav bar attrs until asked; BNV via `bottomNavigationStyle` when design system sets colors
- [ ] **xml:** Glide on `:core-ui` (+ presentation if needed); all programmatic image binds use `loadImage`. **compose:** Coil 3; no Glide in feature modules
- [ ] **compose:** `:core-ui` `AppTheme` in `core/ui/theme/`; `:app` `MainActivity` uses `koinInject()` — never `GlobalContext.get()`
- [ ] Firebase BOM + analytics/crashlytics/messaging on `:core-platform`; `firebase-config` on `:data` (no MessagingService)
- [ ] `kotlinx-coroutines-play-services` on `:core-platform` and `:data`
- [ ] `PlatformFirebase` is `object` without a Context field; poster is `postScreenView` / `postUiClick` (`Param.SCREEN_NAME`, not one event per button)
- [ ] `getDeviceToken` logs Success/Failed without the token value
- [ ] Dispatchers registered **without** `named("io")` / `named("default")`
- [ ] RC DataSource: Mutex, lazy instance, `await()`, `addConfigUpdateListener`, getters with defaults
- [ ] RC `minimumFetchIntervalInSeconds(0)` + cache write to `SharedPrefManager` only when activate succeeds
- [ ] Startup navigates when Remote Config returns or after **5 seconds** (no ad startup sequence yet)
- [ ] UI modules (`:presentation` or `:feature-*`) ↛ `:data`
- [ ] `assembleDebug` succeeds; orientation / theme modes match `project-settings.json`
- [ ] Design system: option **a** ran `sonicc-setup-design-system` (theme `windowBackground`, no default layout `colorSurface`); or **b** left default Material3 theme

## Do not

- Use on an existing production app — use `sonicc-setup-old-project`
- Data Binding / Hilt / Compose when `uiFramework` is `xml`
- XML Fragments / View Binding feature UI when `uiFramework` is `compose`
- `GlobalContext.get()` in Compose `MainActivity`
- App-level `values` resources
- Named dispatcher qualifiers
- `PlatformFirebase` holding Context (ads revenue may take `Context` as a parameter only)
- `addLiveUpdateListener` — method is `addConfigUpdateListener`
- Logging Installation / FCM tokens
- Reading RC only from SDK in Fragments (use prefs cache)
- Hardcode secrets / lock orientation unless product requires
- Skip writing `project-settings.json`
- Add `FirebaseMessagingService` / FCM manifest / push UI during setup (dependency only)

## After setup

Next: if design system was skipped, optional `/sonicc-setup-design-system`; then startup language / startup premium / onboarding / startup welcome back (`StartupActivity`, `29`–`33`) and home + in-app language / premium twins (`MainActivity`) via **`sonicc-figma-to-xml`** (xml) or **`sonicc-figma-to-compose`** (compose) → `sonicc-create-mvi` (or `sonicc-create-screen`); new domain/data via `sonicc-create-clean-architecture`; wire Startup Effects in `nav_graph_startup.xml` (xml) or `StartupNavGraph.kt` (compose). Existing apps missing RC or Analytics: `sonicc-implement-firebase-remote-config`, `sonicc-implement-firebase-events`; later screens: `sonicc-add-firebase-events`; extra RC keys: `sonicc-add-firebase-remote-config`.
