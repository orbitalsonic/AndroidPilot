---
description: Application class, SplashScreen, StartupActivity + MainActivity split, and AndroidX Startup
paths:
  - "**/App.kt"
  - "**/AndroidManifest.xml"
  - "**/themes.xml"
  - "**/splash.xml"
  - "**/MainActivity.kt"
  - "**/StartupActivity.kt"
---

## Application class

- Single `Application` in `:app` — **DI bootstrap first**, then work that needs Koin
- Use **`module` + `modules(...)`** for cold-start graph and **`lazyModule` + `lazyModules(...)`** for deferred features/ads (see `07-dependency-injection`)
- Call `startKoin` **once** here — never from Activity; never guard with `GlobalContext.getOrNull()`
- Prefer lazy / on-demand init for non-critical SDKs

### Method order (mandatory)

```kotlin
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
        // billing connect, ads preload, etc. — anything that needs lazyModule bindings
    }
}
```

- Eager bindings are ready when `startKoin` returns; **`runOnKoinStarted`** for work that needs `lazyModules`
- Same wait-for-lazy pattern on **StartupActivity** / **StartupFragment** (or compose **StartupScreen**) only when resolving **lazy** deps at start
- **Cold-start graph (mandatory):** before shipping, verify the first resolve path cannot hit `No definition found` — `App` loads needed **eager** `module`s via `KoinModules` → `StartupActivity` injects → `StartupFragment` VM (legacy Splash) and **every** ctor dependency is registered (`viewModel` / `factory` / `single`). Adding a dep to Startup*/MainActivity without registering it on the eager list is a **Critical** crash (see `07`, `sonicc-review-architecture`)

## Theme vs Koin

- Sequence: **Koin first**, then theme / billing / SDK work inside `runOnKoinStarted`
- **Forbidden:** `GlobalContext.getOrNull()` (or other GlobalContext probes) as a theme/DI gate
- Activity `enableMaterialDynamicTheme()` may apply DynamicColors directly — no GlobalContext check
- Never call `startKoin` from Activity — theme toggles recreate the Activity and will crash with `KoinAppAlreadyStartedException`

## Two Activities (mandatory shape)

Reference: QiblaCompass. The app is **two Activities, each with its own nav graph** — not one Activity with a mega-graph.

| Activity | Manifest | Layout / graph | Contains |
|----------|----------|----------------|----------|
| `StartupActivity` | **Launcher** (`exported="true"`, MAIN/LAUNCHER), `@style/Theme.App.Starting` | `activity_startup.xml` (`fcvContainerStartup`) + `nav_graph_startup.xml` | `StartupFragment`, `StartupLanguageFragment`, `StartupPremiumFragment`, `StartupIntroFragment`, `StartupWelcomeBackFragment` (`29-screen-startup`) |
| `MainActivity` | `exported="false"`, `launchMode="singleTask"`, product theme | `activity_main.xml` (`fcvContainerMain` + optional `bnvMain` + banner slot) + `nav_graph_main.xml` | Home / feature tabs, `LanguageFragment`, `PremiumFragment`, `SettingsFragment`, `ExitFragment`, details (`32-screen-dashboard`) |

- Package: `presentation/startup/ui/StartupActivity.kt`, `presentation/main/ui/MainActivity.kt` (xml); compose: both in `:app` `ui/` with `StartupNavGraph` / `MainNavGraph` (`28-compose-ui`)
- `StartupActivity.moveToMain()` → `startActivity(Intent(this, MainActivity::class.java))` + `finish()`. Nothing in `MainActivity` returns to startup screens
- `StartupActivity` **blocks back** on every destination (`onBackPressedDispatcher { }` no-op). `MainActivity` owns tab/exit backpress (`32`)
- Each Activity may switch host background per destination with `addOnDestinationChangedListener` (premium vs surface)
- Do **not** create extra `SettingsActivity` / `PremiumActivity`: in-app Settings / Language / Premium are Fragments in `nav_graph_main`

## Splash

- Use AndroidX SplashScreen API (`androidx.core:core-splashscreen`)
- Define splash theme in **`:core-ui` `res/values/splash.xml`** (dedicated file — not only inside `themes.xml`)
- Style name: `Theme.App.Starting` parent `Theme.SplashScreen`
- Set `windowSplashScreenAnimatedIcon` (and optional background) + `postSplashScreenTheme` → main Material3 DayNight app theme
- Manifest: **Application** → product theme (`Theme.App`); **`StartupActivity`** → `@style/Theme.App.Starting` (`10-manifest`)
- Install splash in `StartupActivity` only: `override fun initSplashScreen() { installSplashScreen() }` (hook runs **before** `super.onCreate` in `ParentActivity` — `19-base-ui`). `MainActivity` leaves the hook empty

```xml
<!-- :core-ui/src/main/res/values/splash.xml -->
<resources>
    <style name="Theme.App.Starting" parent="Theme.SplashScreen">
        <item name="windowSplashScreenAnimatedIcon">@drawable/splash_logo</item>
        <item name="postSplashScreenTheme">@style/Theme.App</item>
    </style>
</resources>
```

## StartupActivity / MainActivity (xml)

Both extend `ParentActivity` / `BaseActivity` (`19-base-ui`, templates `startup/`):

```kotlin
class StartupActivity : BaseActivity<ActivityStartupBinding>(ActivityStartupBinding::inflate) {

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(binding.fcvContainerStartup.id) as NavHostFragment).navController
    }

    override fun initSplashScreen() {
        installSplashScreen()
    }

    override fun onCreated() {
        updateStatusBar(darkIcons = true)
        onBackPressedDispatcher { } // funnel: back blocked
        navController.addOnDestinationChangedListener(destinationListener)
    }

    fun moveToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
```

- Insets: `ParentActivity.getInsetsMode()` defaults to `InsetsMode.TOP_BOTTOM`; override per Activity (`EDGE_TO_EDGE` for full-bleed designs) — never per-destination padding flags
- `MainActivity.onCreated()`: `setupBottomNavigation()` (when tabs), `onBackPressedDispatcher { handleBackPress() }`, `startInAppUpdate()` / banner / lifecycle App Open when wired
- NavController: lazy from `supportFragmentManager` + the Activity's own container (`17-navigation`)

## AndroidX Startup / WorkManager

- If WorkManager is **not** used: remove default `WorkManagerInitializer` via Startup provider `tools:node="remove"` to avoid startup DB crashes
- Do not re-enable WorkManager initializer without fixing the crash root cause
- If WorkManager **is** required later: add it deliberately with correct dependencies and keep rules

## Manifest related

- `allowBackup` explicit + backup/extraction XML
- `supportsRtl="true"`
- App Locales / language persistence metadata when multi-language is supported (`AppLocalesMetadataHolderService`)
- Child order inside `<application>`: `StartupActivity` → `MainActivity` → other activities → services → receivers → meta-data (`10-manifest`)

## Forbidden

- Fat Application classes that initialize every SDK eagerly **before** Koin is started
- Calling `get()` / inject on **lazyModule** bindings before `runOnKoinStarted`
- First-screen ViewModel / UseCase / repo / manager used at cold start but registered only as `lazyModule` or omitted from the eager `KoinModules` list
- Hardcoding API keys in Application or themes
- Leaving Startup initializers that crash cold start
- Defining splash theme only ad hoc in Kotlin — use `splash.xml`
- Applying splash theme on the `<application>` tag or on `MainActivity` (`StartupActivity` only)
- Single-Activity mega graph that mixes startup funnel and Home destinations; extra per-feature Activities (Settings / Premium) instead of Fragments in `nav_graph_main`
- `startKoin` in Activity; putting cold-start bindings only in `lazyModules`
- `GlobalContext.getOrNull()` (or any GlobalContext probe) to gate theme / DI
