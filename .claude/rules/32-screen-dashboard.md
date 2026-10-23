---
description: Dashboard + BottomNavigation nested nav and backpress
paths:
  - "**/dashboard/**"
  - "**/DashboardFragment*"
  - "**/DashboardScreen*"
---

# Dashboard screen

## BottomNavigation

When the screen has a bottom nav (layouts often from **`sonicc-figma-to-xml`**):

1. Host nested `NavHostFragment` with `nav_graph_dashboard` (`defaultNavHost="false"`)
2. Tab destinations **only** in that graph; ids match `menu` item ids
3. `setupBottomNavigation()`: `setupWithNavController` + `onNavDestinationSelected`
4. Root graph holds `dashboardFragment`; child → root navigation via `navigateRootTo` (`17-navigation`)
5. Theme: BNV icon/label colors from `bottomNavigationStyle` on `Base.Theme.App` (`09-resources-xml`) — do not invent one-off tints on the view unless Figma forces it

XML checklist (if missing): `fragment_dashboard.xml`, `menu_bottom_nav_dashboard.xml`, `nav_graph_dashboard.xml`, tab `fragment_*.xml` — see **`sonicc-figma-to-xml`** “Dashboard + BottomNavigation”.

```kotlin
private val navController by lazy {
    (childFragmentManager.findFragmentById(binding.fcvContainerDashboard.id) as NavHostFragment).navController
}
```

Optional: `startInAppUpdate()` from Dashboard when product uses Play In-App Updates.

## Backpress (mandatory when bottom nav exists)

Example tabs: Home, Trending, Search, Setting

1. If current destination is **not** a bottom-nav tab → `popBackStack` on the nested controller
2. Else if not on **Home** (start tab) → select Home tab
3. Else if **ExitFragment** exists → `navigateRootTo(... action_dashboardFragment_to_exitFragment)`; Exit may `finishAffinity()` or `exitProcess(0)`
4. Else → double-back with toast (e.g. “Click back again to exit”)

Register back in `onResume` when using `onBackPressedDispatcher` helpers so the callback stays active.

## Helpers

```kotlin
setupBottomNavigation()
startInAppUpdate() // when applicable
```

## Forbidden

- Putting Premium / feature destinations into `nav_graph_dashboard`
- Leaving Material default BNV tints that ignore theme tokens when Figma defines selected/unselected colors
