---
name: sonic-mvi
description: OrbitalSonic Claude Skill: Scaffold presentation-layer MVI only (Intent/State/Effect/ViewModel). XML: Fragment/DI/nav_graph_main. Compose: `:feature-*` *Screen + NavGraph. No domain or data. Use when adding a new screen with ViewModel and layout already exists (or after figma-to-*). For full UI+MVI+domain in one invoke use sonic-create-screen. For new UseCases/repositories use sonic-clean-architecture. Do not use for ads / :gmaAds.
---

# Create MVI Feature (presentation only)

Follow `.claude/rules/` (especially `00-global`, `01-feature-checklist`, `03-android-architecture`, `04-mvi-presentation`, `06-coroutines-flow`, `07-dependency-injection`, `17-navigation`, `18-errors-result`, `19-base-ui`, `28-compose-ui` when `uiFramework` is compose).

Obey `.claude/project-settings.json` when present (`writeTestsWithFeatures`, `orientation`, `themeModes`, `uiFramework`).

## Preconditions

- Confirm feature name (camelCase folder, e.g. `userProfile`)
- Confirm layouts exist (xml: `sonic-figma-to-xml` / dialog / bottom-sheet) or composables exist (compose: `sonic-figma-to-compose`)
- Read existing similar feature for patterns (base Fragment, DI module naming, nav)
- If **new** domain capability is needed (new UseCase / repository / DataSource): run **`sonic-clean-architecture`** first (or after) — **do not** invent domain/data files inside this skill’s required path
- **Do not** use this skill for ads / `:gmaAds` / AdMob. Ads keep the project’s existing managers / ad ViewModels (`21-ads-billing`) unless the user **explicitly** asks to convert ads to MVI

## Package layout

**xml** (`uiFramework` `xml`) — `:presentation` only:

```
presentation/<featureName>/
  di/<Feature>PresentationModule.kt
  intent/<Feature>Intent.kt
  state/<Feature>State.kt
  effect/<Feature>Effect.kt
  viewModel/<Feature>ViewModel.kt
  ui/<Feature>Fragment.kt
  adapter/     # if list
  mapper/      # if domain → UI mapping
```

**compose** (`uiFramework` `compose`) — new Gradle module `:feature-<kebab>` (AnimeHub):

```
feature-<kebab>/src/main/java/…/feature/<name>/
  <Feature>Screen.kt
  di/<Feature>FeatureModule.kt
  intent/ state/ effect/ viewModel/
  components/   # optional
```

Copy [templates/compose/feature-module.gradle.kts](../sonic-setup-new-project/templates/compose/feature-module.gradle.kts). `include` in settings. `:app` depends on the module. Register `*FeatureModule` in `KoinModules` (Startup → eager; others → lazy/`featureList`). Add a `composable` in `:app` `StartupNavGraph.kt` (startup funnel screens) or `MainNavGraph.kt` (everything else). See `28-compose-ui`.

## Which host? (decide first)

| Screen | Host | Naming |
|--------|------|--------|
| Splash / init, first-time language, startup paywall, onboarding, returning-user menu | `StartupActivity` · `nav_graph_startup` / `StartupNavGraph` | **`Startup` prefix**: `StartupLanguageFragment` + `fragment_startup_language.xml` + `startupLanguageFragment`; `Startup*ViewModel` in **eager** `startupPresentationModule` (`29`) |
| Home, tabs, settings, details, in-app language (Apply + search), in-app premium | `MainActivity` · `nav_graph_main` / `MainNavGraph` | Plain names: `LanguageFragment` + `fragment_language.xml`; `lazyModule` unless it is the Home start tab (`30`, `32`, `33`) |

Never share one Fragment between hosts via flags — the startup twin and the in-app twin are separate classes (`30-screen-language`, `33-screen-premium`). Leaving the funnel is `(activity as? StartupActivity)?.moveToMain()`, not a nav action.

## Kotlin checklist

1. **Intent** — sealed class; UI dispatches only via `handleIntent`
2. **State** — data class, defaults, computed `show*` flags
3. **Effect** — navigation, permissions, `@StringRes` errors
4. **ViewModel** — follow `04-mvi-presentation` structure:
    - `handleIntent` = single `viewModelScope.launch(exceptionHandler) { when … }`
    - handlers = `private suspend fun onX()`
    - `exceptionHandler` → `handleError`; **`handleError` last** in the class
    - sparse logs (repo primary; ViewModel failures/`Log.w` guards only)
    - Inject **existing** UseCases / domain repos only — do not create new ones here
5. **UI (xml)** — extend `Parent*` / `Base*`; View Binding only; member order per `19-base-ui`: `onViewCreated` (`setupRecyclerView` / `setupViewPager` / `setupBottomNavigation` as needed, then `screenStarted` — prefer `launchWhenResumed { handleIntent(ScreenStarted) }` when work must wait for resume; **inline** `setOnClickListener` — no `setupClicks()`) → helpers → `initObservers` → `renderState` → `handleEffect` → teardown; `collectWhenStarted` / `collectWhenCreated` via **`viewLifecycleOwner`**; navigate with `navigateTo` / `popFrom` / `navigateRootTo`
   - Paywall / Premium: sticky footer + 3s delayed close (`33-screen-premium`)
   - Bottom nav: tabs top-level in `nav_graph_main`, BNV + backpress owned by `MainActivity` (`32-screen-dashboard`, `17-navigation`); nested `DashboardFragment` only as the optional variant
   - Startup funnel screens (`Startup*`): `29`–`31`, `33` + [reference/app-flow.md](../../rules/reference/app-flow.md); back blocked by `StartupActivity`
5b. **UI (compose)** — `*Screen` + private `*ScreenContent` (`28-compose-ui`): `koinViewModel()`, `collectAsStateWithLifecycle`, `LaunchedEffect(viewModel) { effect.collect }`; `onNavigate*` lambdas; Coil `AsyncImage`; no `NavController` in the feature
6. **Mapping** — heavy work in Repo/UseCase; `toUi()` in ViewModel with dispatcher if large lists
7. **Logs** — `Constants.TAG*` format; prefer Repository; ViewModel not every method
8. **Images** — xml: `siv.loadImage(...)` (Glide). compose: Coil `AsyncImage` / `painterResource`
9. **Orientation / themes** — layouts match `project-settings.json`

## Domain + data

**Out of scope for this skill.**

If the feature needs new UseCases, repository interfaces, DataSources, Retrofit/Room/prefs wiring:

→ Use skill **`sonic-clean-architecture`** (patterns in `26-data-persistence.md` + `rules/reference/`).

Existing UseCases may be injected into the new ViewModel.

## DI

**xml** (`*PresentationModule` in `:presentation`):

```kotlin
// Startup / cold-start → module { }; secondary screens → lazyModule { }
val featurePresentationModule = lazyModule {

    //// ViewModels
    viewModel { FeatureViewModel(get(), get()) }
}
```

**compose** (`*FeatureModule` in `:feature-*`, AnimeHub):

```kotlin
val homeFeatureModule = lazyModule {
    viewModel { HomeViewModel(get(), get()) }
}
```

Register in app composition root (`KoinModules` — `featureList` when compose). **Startup / Splash / cold-start → `module` on the eager list; other features → `lazyModule` on the lazy list.** Use `//// Section` headers (`07-dependency-injection`).

If the screen is **Startup** / start destination / cold-start Splash: use **`module`**, register every ViewModel ctor dependency (UseCases in `useCaseModule`, repos/sources in `dataModule`, etc.), and confirm those modules are on the **eager** `KoinModules` list — incomplete graph crashes on first open (`23`, `29`, `sonic-review-architecture`).

Do **not** add `useCaseModule` / `dataModule` entries here — that belongs to `sonic-clean-architecture`.

## Navigation

**xml:** destination in `nav_graph_startup.xml` (startup funnel) or `nav_graph_main.xml` (app); Safe Args property **`navArgs`** (`StartupPremiumFragment` takes `isFirstTime`). ViewModel emits Effect; Fragment calls `NavController` (or `moveToMain()` when leaving the funnel). Every `<action>` uses reference slide anims; actions out of `startupFragment` are `popUpToInclusive` (`17-navigation`).

**compose:** `FEATURE_ROUTE` on `*Screen.kt`; register `composable` in `:app` `StartupNavGraph.kt` or `MainNavGraph.kt` with the same slide enter/exit as the host `NavHost`. ViewModel emits Effect; `*Screen` calls the `onNavigate*` lambda. Nested tabs: content slots, not feature→feature deps (`28-compose-ui`).

## Strings

- All copy in single `:core-ui` `strings.xml` (+ locale folders)

## Tests

- If `writeTestsWithFeatures` is `true` (default): ViewModel test Intents → State + Effects (`runTest`); UseCase tests only if UseCases already exist and are being extended in presentation flow
- If `false`: skip tests unless user asks
- Prefer `sonic-write-unit-test` skill patterns

## Verify

- [ ] No `:presentation` → `:data` and no `:feature-*` → `:data`
- [ ] No new domain/data files created by this skill
- [ ] Portrait + landscape (or per `project-settings.json`)
- [ ] `src/main/keepRules/*.keep` keeps `state`/`intent`/`effect`/`model` packages (presentation **or** feature)
- [ ] No business logic in Fragment / `*ScreenContent` beyond render + intent dispatch
- [ ] xml: Fragment collectors use `viewLifecycleOwner`; nav via `navigateTo` / `popFrom`; member order per `19-base-ui`
- [ ] compose: `*Screen` / `*ScreenContent` split; route const; NavGraph entry; `koinViewModel`; no `NavController` in feature
- [ ] ViewModel: single `handleIntent` launch, `suspend` handlers, `handleError` last
- [ ] Module + every VM ctor dep registered in `KoinModules` — Startup on **eager** `module` list; other features `lazyModule` (Critical for cold-start)
- [ ] Icon buttons use `ButtonStyle.IconButton` when applicable
- [ ] `DiffUtil.ItemCallback`: simple `areItemsTheSame` / `areContentsTheSame` as one-liners (see `04-mvi-presentation`)
