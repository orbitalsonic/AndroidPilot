# Compose bootstrap templates

Copied by `sonicc-setup-new-project` when `uiFramework` is `compose`. Replace `YOUR.PACKAGE` and `AppTheme` / app name. Same two-Activity split as XML (`23-app-startup`, `29-screen-startup`).

| File | Copy to |
|------|---------|
| `StartupActivity.kt` | `:app` `…/ui/StartupActivity.kt` (launcher, `installSplashScreen()`) |
| `MainActivity.kt` | `:app` `…/ui/MainActivity.kt` (`exported="false"`, `singleTask`) |
| `StartupNavGraph.kt` | `:app` `…/navigation/StartupNavGraph.kt` |
| `MainNavGraph.kt` | `:app` `…/navigation/MainNavGraph.kt` |
| `Color.kt` `Type.kt` `Theme.kt` | `:core-ui` `…/core/ui/theme/` |
| `StartupScreen.kt` | `:feature-startup` `…/feature/startup/StartupScreen.kt` |
| `feature-module.gradle.kts` | each `:feature-*` `build.gradle.kts` (adjust namespace + project deps) |

MVI (`intent` / `state` / `effect` / `viewModel` / `di`) for Startup is scaffolded via `sonicc-create-mvi` Compose path. `StartupLanguageScreen` / `StartupPremiumScreen` / `StartupIntroScreen` / `StartupWelcomeBackScreen` live in `:feature-startup`; in-app `LanguageScreen` / `PremiumScreen` in `:feature-language` / `:feature-premium` (`30`, `33`). `HOME_ROUTE` (`:feature-home`) is created by `sonicc-create-screen`.
