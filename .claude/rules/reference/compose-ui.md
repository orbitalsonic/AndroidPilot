# Compose UI — feature modules, screens, navigation

Full detail for `28-compose-ui.md`. Canonical app: **AnimeHub** (`E:\SohaibAhmed\Github\AnimeHub`). Do not invent a parallel Compose layout.

Applies when `.claude/project-settings.json` `uiFramework` is `"compose"`.

MVI ViewModel law is unchanged — see `04-mvi-presentation` + [mvi-presentation.md](mvi-presentation.md). This file covers **Compose packaging and UI**.

## Modules

```
app                 Application, KoinModules, StartupActivity + MainActivity, navigation/StartupNavGraph.kt + MainNavGraph.kt
domain              Entities, repository interfaces, UseCases
data                Repository impls, DataSources
core-common         Constants, EventsProvider
core-ui             strings.xml, colors/themes (XML), drawables, fonts, splash; compose: core/ui/theme/ Color.kt, Type.kt, AppTheme
core-platform       Firebase, InternetManager, dispatchers
feature-startup     StartupActivity funnel: StartupScreen (start), StartupLanguageScreen, StartupPremiumScreen, StartupIntroScreen, StartupWelcomeBackScreen (`29`)
feature-language    In-app LanguageScreen (Apply + search) — separate from StartupLanguageScreen (`30`)
feature-premium     In-app PremiumScreen — separate from StartupPremiumScreen (`33`)
feature-<name>      One screen cluster per module
```

- **No `:presentation` module** on Compose projects
- Gradle names: kebab-case (`:feature-anime-details`)
- Namespace: `{applicationId}.feature.<name>` (dots)
- `:app` `implementation` every `:feature-*`; features do **not** depend on each other
- Tab / dashboard hosts accept `@Composable () -> Unit` slots; `:app` `NavGraph` wires children (AnimeHub `DashboardScreen`)

## Feature package

```
feature-home/src/main/java/…/feature/home/
  HomeScreen.kt                 # HOME_ROUTE + HomeScreen + HomeScreenContent + items/previews
  di/HomeFeatureModule.kt       # lazyModule { … } for secondary features; module { … } if Startup/cold-start
  intent/HomeIntent.kt
  state/HomeState.kt
  effect/HomeEffect.kt
  viewModel/HomeViewModel.kt
  components/                   # optional shared items / dialogs / sheets
```

DI val name: `homeFeatureModule` (feature + `FeatureModule`). Register in `:app` `KoinModules` — Startup on **eager** list; other features on **lazy** list (`07`).

## Naming

| Kind | Rule | Example |
|------|------|---------|
| Screen | `<Feature>Screen` | `HomeScreen`, `AnimeDetailsScreen` |
| Content | private `<Feature>ScreenContent` | `HomeScreenContent` |
| States | private `<Feature>LoadingContent` / `Empty` / `Error` / `Success` | `HomeEmptyContent` |
| List row | `<Name>Item` | `AnimeItem` |
| Dialog | `<Feature><Purpose>Dialog` | `HomeFilterDialog` |
| Sheet | `<Feature><Purpose>BottomSheet` | `HomeSortBottomSheet` |
| Route | `FEATURE_ROUTE` snake string | `const val HOME_ROUTE = "home"` |
| Route with args | pattern + factory | `ANIME_DETAILS_ROUTE = "anime_details/{animeId}"` + `createRouteAnimeDetails(id)` |
| Nested tab routes | `parent/child` | `dashboard/home` |
| Theme | `{App}Theme` in `:core-ui` `core/ui/theme/` | `AppTheme` / `AnimeHubTheme` |

## Screen template

```kotlin
const val HOME_ROUTE = "home"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToDetail: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    HomeScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::handleIntent,
    )
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) { /* Material3; dispatch onIntent(HomeIntent.OnItemClick(id)) */ }
```

- Collect state with `collectAsStateWithLifecycle` — not `collectAsState`
- Effects: `LaunchedEffect(viewModel) { viewModel.effect.collect { … } }`
- `Modifier` defaults to `Modifier`; pass `modifier.fillMaxSize()` from NavHost when needed
- No `NavController` / `LocalContext` navigation in the feature — callbacks only
- ViewModel stays project MVI: `handleIntent` + `suspend onX()` + `handleError` last (`04`)

## Two NavGraphs (`:app`) — one per Activity

Same two-Activity split as XML (`23-app-startup`, `29-screen-startup`): `StartupActivity` hosts `StartupNavGraph` (funnel, `STARTUP_ROUTE` start), `MainActivity` hosts `MainNavGraph` (`HOME_ROUTE` start). Leaving the funnel is `onFinished` → `startActivity(MainActivity)` + `finish()` in `StartupActivity`, never a route into Home.

```kotlin
@Composable
fun StartupNavGraph(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = STARTUP_ROUTE,
        modifier = modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        composable(STARTUP_ROUTE) {
            StartupScreen(
                navigateToLanguage = {
                    navController.navigate(STARTUP_LANGUAGE_ROUTE) {
                        popUpTo(STARTUP_ROUTE) { inclusive = true }
                    }
                },
                navigateToWelcomeBack = {
                    navController.navigate(STARTUP_WELCOME_BACK_ROUTE) {
                        popUpTo(STARTUP_ROUTE) { inclusive = true }
                    }
                },
                navigateToMain = onFinished,
            )
        }
        composable(STARTUP_LANGUAGE_ROUTE) {
            StartupLanguageScreen(
                navigateToPremium = { navController.navigate(createRouteStartupPremium(isFirstTime = true)) },
                navigateToIntro = { navController.navigate(STARTUP_INTRO_ROUTE) },
            )
        }
        composable(
            route = STARTUP_PREMIUM_ROUTE,
            arguments = listOf(navArgument("isFirstTime") { type = NavType.BoolType }),
        ) {
            StartupPremiumScreen(
                navigateToIntro = { navController.navigate(STARTUP_INTRO_ROUTE) },
                navigateToMain = onFinished,
            )
        }
        composable(STARTUP_INTRO_ROUTE) { StartupIntroScreen(navigateToMain = onFinished) }
        composable(STARTUP_WELCOME_BACK_ROUTE) {
            StartupWelcomeBackScreen(
                navigateToPremium = { navController.navigate(createRouteStartupPremium(isFirstTime = false)) },
                navigateToMain = onFinished,
            )
        }
    }
}

@Composable
fun MainNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
        modifier = modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(onNavigateToDetail = { navController.navigate(createRouteDetail(it)) })
        }
        composable(
            route = DETAIL_ROUTE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            DetailScreen(
                id = entry.arguments?.getString("id"),
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}
```

- `StartupNavGraph` start is `STARTUP_ROUTE` (same invariant as XML `startupFragment`); `MainNavGraph` start is `HOME_ROUTE`. In-app `LANGUAGE_ROUTE` / `PREMIUM_ROUTE` / settings / details live in `MainNavGraph`; `BackHandler {}` on startup screens blocks back
- Slide transitions match XML `slide_in_right` / `slide_out_left` semantics
- Nested `NavHost` for bottom tabs (AnimeHub dashboard): `popUpTo(start) { saveState = true }`, `launchSingleTop`, `restoreState`

## StartupActivity + MainActivity (`:app`)

```kotlin
class StartupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                StartupNavGraph(
                    onFinished = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainNavGraph()
            }
        }
    }
}
```

- `StartupActivity` is the launcher (`Theme.App.Starting`); `MainActivity` is `exported="false"`, `singleTask` (`10-manifest`)

- **Never** `GlobalContext.get()` for theme/prefs — `koinInject()` inside a composable, or a small wrapper that collects a Flow
- Theme / DynamicColors still applied after `startKoin` in `Application` (`07`, `23`)

## Gradle (Compose modules)

`:app`, `:core-ui` (when compose), and every `:feature-*`:

```kotlin
plugins {
    alias(libs.plugins.android.library) // or android.application on :app
    alias(libs.plugins.kotlin.compose)
}
android {
    buildFeatures { compose = true }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.koin.androidx.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

`:app` also: `activity-compose`, `navigation-compose`. Images: Coil 3 (`coil-compose` + `coil-network-okhttp`) on feature modules that load images — **not** Glide.

Catalog keys (latest stable on add): `composeBom`, `navigationCompose`, `activityCompose`, `coilCompose`, plugin `kotlin-compose` (`org.jetbrains.kotlin.plugin.compose` — Compose Compiler, **not** `kotlin-android`).

Do **not** enable View Binding on Compose-only feature modules. XML `:core-ui` may still use resources without View Binding if it has no layouts.

## Lists

- `LazyColumn` / `LazyVerticalGrid` with stable `key`
- Paging: `collectAsLazyPagingItems` when the feature uses Paging 3
- Do not skip placeholder slots in a keyed grid (keep order)
- Heavy map/filter/sort stays in Repo/UseCase/ViewModel — composables bind `*UiItem` / state fields only

## Dialogs and sheets

- Dialog: `AlertDialog` / `Dialog` composable named `*Dialog` — not `DialogFragment`
- Sheet: `ModalBottomSheet` named `*BottomSheet` — not `BottomSheetDialogFragment`
- Hoist visibility in State (`showFilterSheet`) or pass `onDismiss` lambdas; ViewModel owns the flag via Intent

## Theme (`:core-ui`)

**One module for the full design system** — same as xml. XML tokens in `res/values/`; Compose in Kotlin under `core/ui/theme/`:

- `Color.kt` — primitive + semantic colors; light/dark `ColorScheme` mapping (`sonic-setup-design-system`)
- `Type.kt` — `Typography` from Figma `TextStyle.*` ramp (Poppins / design family)
- `Theme.kt` — `@Composable fun AppTheme(darkTheme, content)` wrapping `MaterialTheme` — **no dynamic color** when Figma tokens exist
- `themes.xml` / `colors.xml` still set `android:windowBackground`, splash (`Theme.App.Starting`), and Material bridge attrs

Package: `{applicationId}.core.ui.theme`. Features import `AppTheme` via `:core-ui` only — **no `:core-design` module**.

## Strings and resources

- All user-facing strings stay in **one** `:core-ui` `strings.xml` (App → General → `cd_*` → Screen-wise)
- Features `implementation(project(":core-ui"))` and `stringResource(R.string.*)`
- Drawables in `:core-ui`; Compose reads with `painterResource` / Coil

## Forbidden

- XML `fragment_*` screens, View Binding, Data Binding, `findViewById` for feature UI
- `NavController` in ViewModel or inside `*ScreenContent`
- Feature → `:data` or feature → feature Gradle deps
- `GlobalContext` in Activity / composables
- Hardcoded copy; `dimens.xml`; random hex that duplicates `AppTheme`
- Glide in Compose features (use Coil)
- Converting ads (`:gmaAds`) to Compose MVI unless the user explicitly asks
