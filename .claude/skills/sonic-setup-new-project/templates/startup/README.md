# Two-Activity templates (xml)

Reference: QiblaCompass. Copied by `sonic-setup-new-project` Step 3–4. Replace `YOUR.PACKAGE`.

| File                    | Copy to                                                       |
|-------------------------|---------------------------------------------------------------|
| `StartupActivity.kt`    | `:presentation` `…/presentation/startup/ui/StartupActivity.kt` |
| `MainActivity.kt`       | `:presentation` `…/presentation/main/ui/MainActivity.kt`       |
| `activity_startup.xml`  | `:presentation` `res/layout/activity_startup.xml`              |
| `activity_main.xml`     | `:presentation` `res/layout/activity_main.xml`                 |
| `nav_graph_startup.xml` | `:presentation` `res/navigation/nav_graph_startup.xml`         |
| `nav_graph_main.xml`    | `:presentation` `res/navigation/nav_graph_main.xml`            |

Rules: `23-app-startup`, `29-screen-startup`, `32-screen-dashboard`, `17-navigation`.

- `StartupActivity` = launcher (`Theme.App.Starting`), `installSplashScreen()` in `initSplashScreen()`, back blocked, `moveToMain()` → `MainActivity` + `finish()`
- `MainActivity` = `exported="false"`, `launchMode="singleTask"`; owns `bnvMain` + banner slot when the product has tabs
- `nav_graph_startup` ships with `startupFragment` only — `StartupLanguageFragment` / `StartupPremiumFragment` / `StartupIntroFragment` / `StartupWelcomeBackFragment` are added by `sonic-create-screen` / `sonic-mvi` as the product needs them (actions out of `startupFragment` must `popUpToInclusive`)
- `nav_graph_main` ships with a `homeFragment` placeholder as `startDestination` — replaced by the real Home via `sonic-create-screen`
- Manifest order: `StartupActivity` → `MainActivity` → services → receivers → meta-data (`10-manifest`)
