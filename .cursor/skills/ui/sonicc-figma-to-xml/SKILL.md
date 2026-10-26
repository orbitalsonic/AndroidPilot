---
name: sonicc-figma-to-xml
description: Sonic Cursor Skill: Create Android XML layouts and drawables from a Figma URL or freeform screen request (fragment_/activity_/item_/layout_). Covers Dashboard BottomNavigation + nested nav_graph_dashboard when the design has a bottom bar. XML only — no Kotlin/MVI. Use when the user shares a Figma link, asks for Figma-to-XML, or wants a screen layout without ViewModel scaffolding. For full screen (UI + MVI + optional domain) in one invoke use sonicc-create-screen. Loads Figma design-to-code skill, then adapts to project rules.
---

# Figma / Screen → Android XML (XML only)

Follow `.cursor/rules/09-resources-xml.mdc` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md), `12-naming-conventions.mdc`, `24-figma-assets.mdc`, and when the screen has bottom tabs: `17-navigation.mdc` + `32-screen-dashboard.mdc`.

Obey `.cursor/project-settings.json` when present (`uiFramework`, `orientation`, `themeModes`).

**If `uiFramework` is `compose`:** stop. Use `sonicc-figma-to-compose` instead (unless the user explicitly asked to convert this app to XML).

**Never** create Kotlin Fragment/ViewModel/DI here — point user to `sonicc-create-mvi` / `sonicc-create-clean-architecture` afterward.

## Workflow

### A — From Figma link

1. Parse Figma URL → `fileKey` + `nodeId` (`-` → `:` in node id)
2. **Mandatory:** load Cursor Figma plugin skill **`figma-design-to-code`** before calling `get_design_context`
3. Call `get_design_context` (+ screenshot if needed)
4. Adapt to **this** project: Material widgets, Hungarian IDs, single `:core-ui` strings, no Compose
5. Download assets with Figma MCP `download_assets`
6. Build layout XML + drawables + strings only
7. If the frame is a **Dashboard / shell with BottomNavigation** → also create menu + `nav_graph_dashboard` (see below)

### B — Freeform (no Figma)

Same output rules as below — create the requested `fragment_*` / `activity_*` / `item_*` / `layout_*` XML without Figma MCP. Same BNV / nested-graph extras when the user asks for Dashboard + bottom nav.

## Output

One (or more) layout file(s) in `:presentation` `res/layout/` (and `layout-land/` when `orientation` is `both` or `landscape` requires it):

| Type            | Name                                                       |
|-----------------|------------------------------------------------------------|
| Fragment screen | `fragment_<feature>.xml` or `fragment_<feature>_<sub>.xml` |
| Activity        | `activity_<name>.xml`                                      |
| Reusable block  | `layout_<name>.xml`                                        |
| List row        | `item_<name>.xml`                                          |

Dialog / bottom sheet: use `sonicc-create-dialog` / `sonicc-create-bottom-sheet` (they orchestrate this skill with `dialog_*` / `bottom_sheet_*` names).

## Assets — prefer SVG

Per `24-figma-assets`:

- Prefer **SVG** / `svgAssets` for icons, logos, simple vectors → `ic_svg_*` / `img_svg_*` / `bg_svg_*`
- Use `defaultFormat: "svg"` for vector nodes unless user asked for raster
- Fall back to PNG/WebP when photo, complex art, or SVG is broken/huge
- Place drawables in `:core-ui` (not app `values`)
- **Gradients in `bg_shape_*` / `fg_*`:** `android:angle` **must be a multiple of 45** (0, 45, 90, …). Round Figma angles to the nearest valid value — other angles crash on inflate (`09` / `resources-xml.md`)

## Rules

Obey **all** XML invariants in `09-resources-xml.mdc` + [reference/resources-xml.md](../../../rules/reference/resources-xml.md) (Material widgets, Hungarian IDs, IconButton padding `8dp` + `iconTint` `?attr/colorIcon`, button tint/stroke, chip selectors, no `dimens.xml`, resource module placement, sticky footers, `clRoot*` + `tools:context`, View Binding, RecyclerView in XML, gradient angles ×45, closing-tag formatting). Also `12-naming-conventions.mdc`.

Layouts go in **`:presentation`** only; drawables/strings used by the screen stay in **`:core-ui`**.

**Skill-specific (Figma / screen XML):**

- **Portrait + landscape** — responsive ConstraintLayout; add `layout-land/` if needed — **unless** `project-settings.json` `orientation` is `portrait` or `landscape` only
- Theme modes: add `values-night` colors/themes when `themeModes` is `night` or `both`
- **Theme-first background:** do not set `android:background="?attr/colorSurface"` on default screen roots — window color comes from `:core-ui` `android:windowBackground` (`09` / `reference/resources-xml.md`)
- Programmatic loads → `siv.loadImage(...)` (Glide / `ImageViewExtensions`) when documenting bind notes
- **Button fill + stroke from Figma:** apply on `MaterialButton` with `app:backgroundTint`, `app:strokeColor`, `app:strokeWidth`, `app:cornerRadius` — **do not** export/create `bg_shape_*` oval/rect (solid+stroke only), and **do not** use `android:background` + `backgroundTint="@null"` + inset hacks
    - Circle icon button: `cornerRadius` ≈ half of width/height
    - Colors → `:core-ui` `colors.xml`; skip the shape XML file entirely when Material attrs cover it
    - Only create `bg_shape_*` for non-button surfaces, gradients, or selectors Material cannot express (gradients → angle ×45)

## Screen naming by host (mandatory)

| Figma screen | Host | Layout name |
|--------------|------|-------------|
| Splash / init, first-time language (Next only, no search), startup paywall, onboarding, returning-user menu | `StartupActivity` | `fragment_startup.xml`, `fragment_startup_language.xml`, `fragment_startup_premium.xml`, `fragment_startup_intro.xml`, `fragment_startup_welcome_back.xml` |
| In-app language (back + Apply + search), in-app premium, settings, home, details | `MainActivity` | `fragment_language.xml`, `fragment_premium.xml`, `fragment_settings.xml`, `fragment_home.xml`, … |

Startup and in-app Language / Premium are **two layouts each** — never one layout with hidden views (`29`–`33`). Activity layouts: only `activity_startup.xml` / `activity_main.xml` (`23`).

## Bottom navigation (when Figma has a bottom bar)

**Default (reference QiblaCompass):** the bar lives in **`activity_main.xml`** (`fcvContainerMain` + banner slot `bannerAdViewMain` + `bnvMain`), tabs are **top-level destinations in `nav_graph_main.xml`**, menu is `menu_bottom_nav_main.xml` (item ids = tab fragment ids). Produce the tab `fragment_*.xml` layouts + menu and add the tab destinations to `nav_graph_main` — no `fragment_dashboard.xml`. Kotlin (`setupBottomNavigation`, backpress) → `sonicc-create-mvi` + `32-screen-dashboard`.

**Optional nested variant** (only when the product needs a `DashboardFragment` shell inside a deeper flow): produce **XML resources only** as below. Follow `17-navigation`.

### Always create together (nested variant)

| File | Module | Role |
|------|--------|------|
| `fragment_dashboard.xml` | `:presentation` `layout/` | Root `clRootDashboard` + nested `FragmentContainerView` (`fcvContainerDashboard`) + `BottomNavigationView` (`bnvDashboard`) |
| `menu_bottom_nav_dashboard.xml` | `:presentation` `menu/` | One `<item>` per tab; **`android:id` = tab destination id** (e.g. `@+id/homeFragment`) |
| `nav_graph_dashboard.xml` | `:presentation` `navigation/` | **Only** bottom-tab fragments; `startDestination` = Home (or first tab); **no** Premium / detail destinations |
| Tab `fragment_*.xml` | `:presentation` `layout/` | One layout per tab screen from Figma |

Also ensure root `nav_graph_main.xml` has a `dashboardFragment` destination (add destination stub if missing — do not invent full funnel). Child → root navigations are **not** actions inside `nav_graph_dashboard` — those use `navigateRootTo` later in Kotlin.

### `fragment_dashboard.xml` shape

```xml
<!-- Nested host ABOVE the bar; defaultNavHost=false; graph = nav_graph_dashboard -->
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/fcvContainerDashboard"
    android:name="androidx.navigation.fragment.NavHostFragment"
    app:defaultNavHost="false"
    app:navGraph="@navigation/nav_graph_dashboard"
    app:layout_constraintBottom_toTopOf="@id/bnvDashboard"
    … />

<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bnvDashboard"
    android:background="?attr/colorSurfaceContainer"
    app:menu="@menu/menu_bottom_nav_dashboard"
    app:labelVisibilityMode="labeled"
    app:layout_constraintBottom_toBottomOf="parent"
    … />
```

- Do **not** hardcode `itemIconTint` / `itemTextColor` on the BNV unless Figma forces a one-off — colors come from `bottomNavigationStyle` on `Base.Theme.App` (`09` / `sonicc-setup-design-system`)
- Tab icons → `:core-ui` `ic_svg_*`; titles → `:core-ui` `strings.xml`
- Menu item ids **must equal** `nav_graph_dashboard` fragment ids (required for `setupWithNavController`)

### After BNV layouts

Tell the user to run **`sonicc-create-mvi`** for each tab (and for `DashboardFragment` in the nested variant) and wire:

- `setupBottomNavigation()` in `MainActivity` (default) or nested `navController` lazy from `childFragmentManager` + `fcvContainerDashboard` (nested variant)
- Backpress: non-tab pop → Home tab → Exit / double-back (`32-screen-dashboard`)

## Do not create

- Kotlin Fragment/ViewModel (use `sonicc-create-mvi` skill)
- Domain / data / repository layers (use `sonicc-create-clean-architecture`)
- Strings in presentation module — add to shared `strings.xml` (incl. `cd_*`)
- `dimens.xml`
- `findViewById` references
- Plain `ImageView`
- Clickable `ShapeableImageView` used as a button
- Non-tab destinations inside `nav_graph_dashboard`
- Gradient XML with `android:angle` not a multiple of 45

## After layout

- Call out any asset that fell back from SVG → raster and why
- Call out any gradient angle rounded to a multiple of 45
- If Dashboard/BNV: list the menu + `nav_graph_dashboard` + tab layouts created
- Tell user to wire via `sonicc-create-mvi` (presentation) and `sonicc-create-clean-architecture` if new domain/data is needed — or **`sonicc-create-screen`** for the full stack in one invoke
