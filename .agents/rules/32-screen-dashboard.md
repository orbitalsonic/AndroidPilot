---
description: MainActivity shell — BottomNavigation on nav_graph_main, banner slot, backpress / exit; optional nested Dashboard graph
paths:
  - "**/MainActivity*"
  - "**/dashboard/**"
  - "**/DashboardFragment*"
  - "**/DashboardScreen*"
  - "**/nav_graph_main.xml"
  - "**/nav_graph_dashboard.xml"
---

# MainActivity shell (home + bottom navigation)

`MainActivity` is the second Activity (`23-app-startup`, `29-screen-startup`). It hosts `fcvContainerMain` + `nav_graph_main.xml` and — when the product has tabs — the `BottomNavigationView` **directly in `activity_main.xml`** (reference: QiblaCompass). A nested `DashboardFragment` + `nav_graph_dashboard` is the **optional** variant for apps whose tab shell sits deeper in a funnel.

## Default: BottomNavigation on `MainActivity`

1. `activity_main.xml`: vertical `LinearLayout` / `ConstraintLayout` → `fcvContainerMain` (`defaultNavHost="true"`, `@navigation/nav_graph_main`) → banner slot (`BannerAdView` `bannerAdViewMain`, `gone` until loaded) → `bnvMain`
2. Tab destinations are **top-level fragments in `nav_graph_main`**; ids match `menu_bottom_nav_main.xml` item ids; `startDestination` = Home (first tab)
3. `setupBottomNavigation()`: `setupWithNavController` + `setOnItemSelectedListener` → `navigateBottomTab(itemId)` with `NavOptions` `launchSingleTop` + `restoreState` + `popUpTo(startDestination, saveState = true)`; interstitial on tab switch when wired (`21`)
4. `addOnDestinationChangedListener`: tabs → BNV + banner **visible**; other destinations → BNV `gone` (banner per product)
5. Theme: BNV icon/label colors from `bottomNavigationStyle` on `Base.Theme.App` (`09-resources-xml`) — do not invent one-off tints on the view unless Figma forces it
6. Non-tab destinations (`LanguageFragment`, `PremiumFragment`, `SettingsFragment`, detail screens, `ExitFragment`) also live in `nav_graph_main` with the four slide anims (`17-navigation`)

```kotlin
private val navController by lazy {
    (supportFragmentManager.findFragmentById(binding.fcvContainerMain.id) as NavHostFragment).navController
}
```

Optional: `startInAppUpdate()` from `MainActivity.onCreated()` when product uses Play In-App Updates (`25`).

## Optional: nested `DashboardFragment`

When the shell must be a Fragment (e.g. a drawer host above it):

1. Host nested `NavHostFragment` with `nav_graph_dashboard` (`defaultNavHost="false"`); tab destinations **only** in that graph
2. Root graph holds `dashboardFragment`; child → root navigation via `navigateRootTo` (`17-navigation`)
3. XML checklist: `fragment_dashboard.xml`, `menu_bottom_nav_dashboard.xml`, `nav_graph_dashboard.xml` — see **`sonicg-figma-to-xml`** “Dashboard + BottomNavigation”

```kotlin
private val navController by lazy {
    (childFragmentManager.findFragmentById(binding.fcvContainerDashboard.id) as NavHostFragment).navController
}
```

## Backpress (mandatory when bottom nav exists)

Example tabs: Home, Prayer, Calendar, Settings

1. If current destination is **not** a bottom-nav tab → `navController.popBackStack()`
2. Else if not on **Home** (start tab) → select Home tab
3. Else if **ExitFragment** exists and user is not purchased → navigate to `exitFragment` (global action `action_main_to_exitFragment`); Exit may `finishAndRemoveTask()` / `finishAffinity()`
4. Else → double-back with toast (e.g. “Click back again to exit”) or finish

Register via `onBackPressedDispatcher { handleBackPress() }` in `onCreated()` (`ActivityExtensions`).

## Helpers

```kotlin
setupBottomNavigation()
startInAppUpdate() // when applicable
```

## Forbidden

- Hosting Startup funnel screens in `MainActivity` (`29-screen-startup`)
- Putting Premium / feature destinations into `nav_graph_dashboard` (nested variant)
- Leaving Material default BNV tints that ignore theme tokens when Figma defines selected/unselected colors
- Duplicating the banner slot per tab Fragment when `MainActivity` already owns it
