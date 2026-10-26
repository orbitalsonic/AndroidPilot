---
description: Full detail for 07-dependency-injection — Koin module vs lazyModule, section headers, cold-start graph
globs: ["**/di/**/*.kt", "**/*Module*.kt", "**/App.kt"]
alwaysApply: false
---

# Dependency injection conventions

Full detail for `07-dependency-injection.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

## Framework

Use the DI framework already in the project. Default stack: **Koin**.

- Bootstrap in Application class — keep Application minimal
- Aggregate modules in a single composition root (e.g. `KoinModules`)
- Prefer constructor injection and `by inject()` / `by viewModel()` over service locators
- If the project already has a limited `DIComponent` / `KoinComponent` façade on base Fragments, follow it — do not expand it for new features without need

## `module` vs `lazyModule` (choose by usage)

Use **both** when appropriate. Do **not** force one style for the whole app.

| Declare with | Load with | When |
|--------------|-----------|------|
| `module { }` | `modules(...)` | **Eager** — needed at cold start / first frame |
| `lazyModule { }` | `lazyModules(...)` | **Deferred** — secondary screens, heavy/optional graphs |

### Use `module` (eager)

Cold-start graph — bindings resolved by `App`, `StartupActivity` / `MainActivity` (if they inject), the `StartupActivity` funnel (`StartupFragment`, `StartupLanguageFragment`, `StartupPremiumFragment`, `StartupIntroFragment`, `StartupWelcomeBackFragment` — legacy **Splash**) or the Home start tab without waiting:

- `appModule`, `coreModule`, `corePlatformModule`
- `dataModule` (SharedPref, RC cache, repos used at Startup)
- `useCaseModule` factories used by the Startup funnel / MainActivity / Home tab
- `startupPresentationModule` (xml) / `startupFeatureModule` (compose) — **every** `Startup*ViewModel`, plus `mainPresentationModule` / Home-tab module when `MainActivity` or the first tab injects
- Dispatchers, InternetManager, and any other ctor deps on the first-screen ViewModel chain

Eager modules are ready when `startKoin { modules(...) }` returns — no `runOnKoinStarted` required for those bindings.

### Use `lazyModule` (deferred)

Not required to paint the first screen:

- Secondary feature modules (`settingsPresentationModule`, in-app `languagePresentationModule` / `premiumPresentationModule`, non-start tabs, …)
- Ads modules (`gmaAdsModule`, banner/interstitial/native/… placement modules)
- Heavy optional feature graphs loaded after launch
- Billing UI modules that are not on the cold-start path (BillingDataSource itself may still be eager if started from `Application`)

Resolve lazy bindings after they are ready (`runOnKoinStarted` / wait APIs) — never assume they exist the instant `startKoin` returns.

### Rule of thumb

> If `StartupActivity` (any `Startup*` screen), `MainActivity`, or the Home start tab needs it on first open → **`module`**.  
> If it is a later screen, ads, or optional heavy wiring → **`lazyModule`**.

Mixing `modules(...)` + `lazyModules(...)` in the same `startKoin` block is **expected**.

### Migrating an existing project

1. Classify each existing module: cold-start → `module`; deferred → `lazyModule`
2. Split composition root into eager + lazy lists (or two getters on `KoinModules`)
3. `startKoin { modules(eager); lazyModules(deferred) }`
4. Keep `//// Section` headers while converting
5. Apply theme / billing that needs **lazy** bindings inside `runOnKoinStarted`; eager-only work can run right after `startKoin`
6. Never probe Koin via `GlobalContext`

### Bootstrap (`Application`)

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
            // billing connect, ads preload, etc. — anything that needs lazyModule bindings
        }
    }
}
```

`KoinModules` example:

```kotlin
fun getEagerModules() = listOf(
    //// Core
    appModule,
    coreModule,
    corePlatformModule,

    //// Data
    dataModule,

    //// Domain
    useCaseModule,

    //// Presentation (cold-start only)
    startupPresentationModule, // or startupFeatureModule when compose — all Startup* ViewModels
    homePresentationModule,    // MainActivity start tab
)

fun getLazyModules() = listOf(
    //// Presentation / Feature (deferred)
    languagePresentationModule,
    premiumPresentationModule,
    settingsPresentationModule,

    //// Ads
    gmaAdsModule,
    bannerAdModule,
    nativeAdModule,
)
```

(Compose: put secondary `*FeatureModule`s in the lazy list; keep Startup in the eager list.)

### Theme apply — after Koin start (mandatory)

- **Do not** use `GlobalContext.getOrNull()` (or any GlobalContext probe) to gate theme / DI
- **Do** sequence work: `startKoin { modules(...); lazyModules(...) }` first, then theme
- Eager-only theme/prefs: safe immediately after `startKoin` returns
- Anything that needs **lazyModule** bindings: inside `runOnKoinStarted`
- Activity helpers (`enableMaterialDynamicTheme`) may call `DynamicColors.applyToActivityIfAvailable(this)` directly — Application always starts Koin before any Activity
- Keep `startKoin` **only** in `Application` — never in Activity (theme change recreates Activity → `KoinAppAlreadyStartedException`)

## Readable sections (mandatory)

Every `module` / `lazyModule` body must use **clear section headers** so bindings stay readable and separated by concern.

### Comment style

```kotlin
//// SectionName
```

- Exactly four slashes + space + PascalCase / Title-style name
- Blank line **before** each section (except the first)
- Blank line **after** the last binding of a section when another section follows
- One concern per section — do not mix DataSources with ViewModels in the same block
- Skip empty sections; add a section only when it has bindings
- Tiny modules (single `viewModel`) still get one section header for consistency

### Standard section names by module type

| Module                           | Section order (use only what applies)                                                                                                          |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `dataModule` / data area modules | `//// DataSources` → `//// Repositories` (optional `//// Managers` / SDK wrappers **before** DataSources if needed)                          |
| `useCaseModule`                  | Group by domain area: `//// Startup`, `//// Location`, `//// Prayer`, … — never dump all factories in one unsorted list when areas differ |
| `*PresentationModule`            | `//// ViewModels` (optional `//// Helpers` if feature-scoped singles) — **xml** |
| `*FeatureModule`                 | `//// ViewModels` — **compose** (`homeFeatureModule`, AnimeHub)                                                                          |
| `corePlatformModule`             | `//// Dispatchers` → `//// Managers` → `//// Other`                                                                                        |
| `coreModule` / UI core           | `//// Observers` / `//// Providers` / concern-named sections                                                                                   |
| `appModule`                      | `//// App` (info providers, app-scoped singles)                                                                                                |
| Ads feature modules              | `//// DataSources` → `//// Repositories` → `//// UseCases` → `//// ViewModels` → `//// Managers` / `//// Config`                       |
| `KoinModules` list               | Group returned modules: `//// Core` → `//// Data` → `//// Domain` → `//// Presentation` or `//// Feature` → `//// Ads` |

Do **not** invent cryptic headers (`//// stuff`, `//// temp`). Name the concern.

## Module organization

| Location                     | Contents                                                                                             |
|------------------------------|------------------------------------------------------------------------------------------------------|
| `app/di/`                    | App-level singles, **module aggregation** (`KoinModules`) only — not UseCases / repo interfaces    |
| `domain/di/`                 | UseCase **factories** (`useCaseModule` / `domainModule`)                                             |
| `data/di/`                   | DataSource + Repository **impl** bindings (`dataModule`)                                             |
| `presentation/<feature>/di/` | Feature ViewModels (**xml**)                                                                              |
| `feature-*/…/di/`            | `*FeatureModule` ViewModels (**compose**)                                                                 |
| `core-*/di/`                 | Platform and UI dependencies                                                                         |
| ads module `di/`             | Existing ad managers / ad ViewModels (named qualifiers per placement) — not MVI unless the user asks |

### Domain vs data ownership (DI + source)

| Create in `:domain`          | Create in `:data`                        | Never in `:data`          |
|------------------------------|------------------------------------------|---------------------------|
| Repository **interfaces**    | Repository **impls** (`*RepositoryImpl`) | UseCase classes           |
| UseCase classes (`*UseCase`) | DataSources / managers                   | Repository **interfaces** |
| `useCaseModule` factories    | `dataModule` bindings                    | —                       |

Presentation injects UseCases and repository **interfaces** — never `*RepositoryImpl` or DataSources from `:data`.

## Binding types

| Scope       | Use for                                          |
|-------------|--------------------------------------------------|
| `single`    | Repositories, DataSources, managers, dispatchers |
| `factory`   | UseCases (stateless, cheap to recreate)          |
| `viewModel` | ViewModels                                       |

## Examples

### Eager — `dataModule` (`module`)

```kotlin
val dataModule = module {

    //// DataSources
    single { LanguageDataSource() }
    single { SharedPrefManager(androidContext()) }
    single { RemoteConfigDataSource() }

    //// Repositories
    single<LanguageRepository> { LanguageRepositoryImpl(get()) }
    single<SharedPrefRepository> { SharedPrefRepositoryImpl(get()) }
    single<RemoteConfigRepository> { RemoteConfigRepositoryImpl(get(), get(), get()) }
}
```

- DataSources before Repositories
- Do **not** register UseCases here
- Cold-start data stays in `module`; feature-only data splits can use `lazyModule`

### Eager — `useCaseModule` (domain)

```kotlin
val useCaseModule = module {

    //// Startup
    factory { GetStartupDestinationUseCase(get()) }

    //// Location
    factory { GetLocationAndAddressUseCase(get()) }

    //// Internet
    factory { CheckInternetUseCase(get()) }

    //// Prayer
    factory { GetPrayerSettingsUseCase(get()) }
    factory { SavePrayerSettingsUseCase(get()) }
    factory { GetTodayPrayerTimesUseCase(get()) }
}
```

- Lives under `:domain` — **not** `:data`
- Sort factories by area; keep related UseCases together
- Startup-related factories must stay on the **eager** module list

### Eager — Startup presentation (whole `StartupActivity` funnel)

```kotlin
val startupPresentationModule = module {

    //// ViewModels
    viewModel { StartupViewModel(get(), get(), get()) }
    viewModel { StartupLanguageViewModel(get(), get()) }
    viewModel { StartupPremiumViewModel(get(), get()) }
    viewModel { StartupIntroViewModel(get()) }
    viewModel { StartupWelcomeBackViewModel(get()) }
}
```

### Deferred — secondary feature (in-app Language / Premium / Settings / non-start tabs)

```kotlin
val languagePresentationModule = lazyModule {

    //// ViewModels
    viewModel { LanguageViewModel(get(), get()) }
}
```

### Eager — `corePlatformModule`

```kotlin
val corePlatformModule = module {

    //// Dispatchers
    single { Dispatchers.IO }
    single { Dispatchers.Default }

    //// Managers
    single { InternetManager(androidContext()) }
}
```

### Deferred — Ads (existing architecture — not MVI)

Keep this ads DI shape. Do not replace ad ViewModels with Intent/State/Effect unless the user **explicitly** asks (`21-ads-billing`).

```kotlin
val bannerAdModule = lazyModule {

    //// DataSources
    single { DataSourceLocalBanner() }
    single { DataSourceRemoteBanner(context = get()) }

    //// Repositories
    single { RepositoryBannerImpl(get(), get()) }

    //// UseCases
    single { UseCaseBanner(get(), get(), get(), get()) }

    //// ViewModels
    viewModel { ViewModelBanner(get()) }
}
```

## Other rules

- **Every new module must be registered** in the composition root (eager or lazy list as appropriate)
- Bind interfaces to implementations: `single<XRepository> { XRepositoryImpl(get()) }`
- Dispatchers: `single { Dispatchers.IO }` and `single { Dispatchers.Default }` — **no** `named("io")` / `named("default")`; repos often use `ioDispatcher: CoroutineDispatcher = Dispatchers.IO`
- Use named qualifiers only for true parallel instances (e.g. multiple ad placements): `named("banner_home")`

### Cold-start / first screens (mandatory)

Walk and verify before release (prevents `No definition found` / `Could not create instance` on Splash/Startup):

1. `App.startKoin` → `modules(eager)` + `lazyModules(deferred)` from `KoinModules`
2. Eager list includes every module the first UI needs (`Startup*` funnel / MainActivity / Home-tab inject graph)
3. **StartupActivity** / **MainActivity** (if they use `by inject` / `koinInject`) + `StartupFragment` → ViewModel → **every** constructor parameter type has `viewModel` / `factory` / `single` in a **loaded eager** `module`

**Forbidden:** adding a ctor dependency to a `Startup*` screen / MainActivity / Home tab without registering it on the **eager** list in `KoinModules`.
