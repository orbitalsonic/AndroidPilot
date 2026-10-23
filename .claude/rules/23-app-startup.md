---
description: Application class, SplashScreen, and AndroidX Startup
paths:
  - "**/App.kt"
  - "**/AndroidManifest.xml"
  - "**/themes.xml"
  - "**/splash.xml"
  - "**/MainActivity.kt"
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
- Same wait-for-lazy pattern on **MainActivity** / **EntranceFragment** (or compose **EntranceScreen**) only when resolving **lazy** deps at start
- **Cold-start graph (mandatory):** before shipping, verify the first resolve path cannot hit `No definition found` — `App` loads needed **eager** `module`s via `KoinModules` → start destination VM (Entrance / legacy Splash) and **every** ctor dependency is registered (`viewModel` / `factory` / `single`). Adding a dep to Entrance/Splash/MainActivity without registering it on the eager list is a **Critical** crash (see `07`, `sonicc-review-architecture`)

## Theme vs Koin

- Sequence: **Koin first**, then theme / billing / SDK work inside `runOnKoinStarted`
- **Forbidden:** `GlobalContext.getOrNull()` (or other GlobalContext probes) as a theme/DI gate
- Activity `enableMaterialDynamicTheme()` may apply DynamicColors directly — no GlobalContext check
- Never call `startKoin` from Activity — theme toggles recreate the Activity and will crash with `KoinAppAlreadyStartedException`

## Splash

- Use AndroidX SplashScreen API (`androidx.core:core-splashscreen`)
- Define splash theme in **`:core-ui` `res/values/splash.xml`** (dedicated file — not only inside `themes.xml`)
- Style name: `Theme.App.Starting` parent `Theme.SplashScreen`
- Set `windowSplashScreenAnimatedIcon` (and optional background) + `postSplashScreenTheme` → main Material3 DayNight app theme
- Manifest: **Application** → product theme (`Theme.App`); **launcher Activity** → `@style/Theme.App.Starting` (`10-manifest`)
- Install splash in Activity base before `setContentView`: `installSplashScreen()` / project `installSplashTheme()` helper

```xml
<!-- :core-ui/src/main/res/values/splash.xml -->
<resources>
    <style name="Theme.App.Starting" parent="Theme.SplashScreen">
        <item name="windowSplashScreenAnimatedIcon">@drawable/splash_logo</item>
        <item name="postSplashScreenTheme">@style/Theme.App</item>
    </style>
</resources>
```

## MainActivity (xml)

- Extends `ParentActivity` / `BaseActivity` in `:presentation`
- `ParentActivity.includeTopPadding` default **`false`**
- Destination listener: `includeTopPadding = false` for Entrance (fullscreen feel); **`true`** for other destinations
- Block back on initial funnel destinations (Entrance, Language, OnBoarding, WelcomeBack, …) — no `popBackStack`; elsewhere `navController.popBackStack()`
- NavController: lazy from `supportFragmentManager` + `fcvContainerMain` (`17-navigation`)

## AndroidX Startup / WorkManager

- If WorkManager is **not** used: remove default `WorkManagerInitializer` via Startup provider `tools:node="remove"` to avoid startup DB crashes
- Do not re-enable WorkManager initializer without fixing the crash root cause
- If WorkManager **is** required later: add it deliberately with correct dependencies and keep rules

## Manifest related

- `allowBackup` explicit + backup/extraction XML
- `supportsRtl="true"`
- App Locales / language persistence metadata when multi-language is supported
- Child order inside `<application>`: MainActivity → other activities → services → receivers → meta-data (`10-manifest`)

## Forbidden

- Fat Application classes that initialize every SDK eagerly **before** Koin is started
- Calling `get()` / inject on **lazyModule** bindings before `runOnKoinStarted`
- First-screen ViewModel / UseCase / repo / manager used at cold start but registered only as `lazyModule` or omitted from the eager `KoinModules` list
- Hardcoding API keys in Application or themes
- Leaving Startup initializers that crash cold start
- Defining splash theme only ad hoc in Kotlin — use `splash.xml`
- Applying splash theme on the `<application>` tag (launcher Activity only)
- `startKoin` in Activity; putting cold-start bindings only in `lazyModules`
- `GlobalContext.getOrNull()` (or any GlobalContext probe) to gate theme / DI
