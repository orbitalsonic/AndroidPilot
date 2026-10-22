# Setup Design System — extraction and files

Used by [SKILL.md](SKILL.md). XML naming, strings, Material widgets: `09-resources-xml` + `reference/resources-xml.md`. Library Gradle: `08-gradle` + `reference/gradle.md`. Do not duplicate those tables here.

## If `:core-ui` is missing — create it, then continue

Do this in the **same turn**. Match existing `:app` AGP / `compileSdk` style.

1. Version catalog: add `android-library` plugin if absent. Root `build.gradle.kts`: `alias(libs.plugins.android.library) apply false`.
2. `settings.gradle.kts`: `include(":core-ui")`.
3. `core-ui/build.gradle.kts`: library shape from `reference/gradle.md` (no `signingConfigs` / `bundle` / `base`). `namespace` = `{applicationId}.core.ui` when `applicationId` in `project-settings.json` is non-empty; else `{ :app namespace }.core.ui`. View Binding on. `minSdk` matches `:app`.
4. `core-ui/.gitignore` containing `/build` (`02-project-structure`).
5. `core-ui/src/main/AndroidManifest.xml` — **omit** unless a permission/component is required (prefer no empty library manifest — `10-manifest`)
6. `:app` `implementation(project(":core-ui"))`.
7. If `:app` still has `res/values/` themes / strings / colors, **move** them into `:core-ui` (app keeps mipmap / xml backup only). Point the app manifest **application** theme at `:core-ui` `Theme.*`; launcher Activity uses `Theme.App.Starting`.
8. Do **not** add `:presentation`, `:domain`, `:data`, `:core-common`, `:core-platform`, Entrance, or `Parent*` from this skill.

Then write Figma tokens into that module.

## Figma reads (targeted)

Parse `figma.com/design/:fileKey/:name?node-id=A-B` → `fileKey`, `nodeId` `A:B`.

| Need | How |
|------|-----|
| Pages | `get_metadata` without `nodeId`, or `use_figma` listing `figma.root.children` |
| Primitive palette | `get_variable_defs` / `get_design_context` on Foundation **Colors** (not the whole file). These are the **only** hex values. |
| Semantic light/dark | Tokens page or `use_figma` on the semantic collection. Values are Figma `VARIABLE_ALIAS` → primitive names (Light vs Dark swap the target). Record **which primitive**, not resolved hex. |
| Type ramp | `figma.getLocalTextStyles()` — family, size, weight, lineHeight, letterSpacing |
| Spacing / radius | Foundation Spacing table + radius variables (document as inline `dp` comments; **no `dimens.xml`**) |
| Shadows | Effect styles → Material elevation only if useful; do not invent `dimens` |

Skip bulk `getLocalVariablesAsync` dumps of the entire file.

## `:core-ui` files

| File | Contents |
|------|----------|
| `values/colors.xml` | App `md_theme_*` (aliases to semantics) → General primitives (**hex only here**) → semantic tokens (`@color/primitive_*` aliases). Section headers per `09`. |
| `values-night/colors.xml` | Semantic + `md_theme_*` only (when `themeModes` is `night` or `both`). Semantics alias **different** primitives. Primitives stay in `values/`. |
| `values/attrs.xml` | Extra semantic attrs (`colorTextTertiary`, `colorTextLink`, …) if Figma has them |
| `values/themes.xml` | `Base.Theme.*` + `Theme.*` + `TextStyle.*` + `ButtonStyle.*` + `ShapeAppearance.App.*` |
| `values-night/themes.xml` | Dark `Base.Theme.*` (window/status bar light flags false) |
| `res/font/` | OFL font files for the Figma family + `font-family` XML. Keep license (`OFL.txt` next to the module, not in `res/font`). |
| `res/color/selector_*.xml` | Button/icon enabled/disabled |
| `values/strings.xml` | Only add missing `cd_*` / actions. Do not invent feature screens. |

All user-facing strings stay in the **one** `:core-ui` `strings.xml`.

## Color alias graph (mandatory)

Match Figma: primitives own hex; semantics are aliases; Material is a bridge — do **not** flatten.

```
primitive_* (hex, values/ only)
    ↑ @color alias
semantic (values/ light targets, values-night/ dark targets)
    ↑ @color alias
md_theme_* (keep — aliases semantics; Material-only roles may alias a primitive)
    ↑ theme items
colorPrimary / colorSurface / android:windowBackground / …
```

Keep **Material theming and `md_theme_*`**. `Theme.Material3.DayNight.NoActionBar` + `colorPrimary` / `colorOnPrimary` / containers / error / outline / `colorSurfaceContainer*` so MaterialButton, cards, ripples, and checkboxes do not fall back to default purple. Layouts still use `?attr/colorOnSurface` and extra attrs (`?attr/colorTextTertiary`).

When extracting, follow `VARIABLE_ALIAS` hops and write the **primitive resource name**, not the resolved hex:

```xml
<!-- values/ — primitives: hex only -->
<color name="primitive_neutral_90">#FF171717</color>
<color name="primitive_primary_50">#FF2563EB</color>

<!-- values/ — semantics: alias primitives (light) -->
<color name="color_text_neutral_primary">@color/primitive_neutral_90</color>
<color name="color_button_primary_bg">@color/primitive_primary_50</color>
<color name="color_bg_surface">@color/primitive_neutral_0</color>

<!-- values/ — md_theme_*: alias semantics -->
<color name="md_theme_primary">@color/color_button_primary_bg</color>
<color name="md_theme_surface">@color/color_bg_surface</color>
```

```xml
<!-- values-night/ — same semantic names, dark primitive targets -->
<color name="color_text_neutral_primary">@color/primitive_neutral_0</color>
<color name="color_button_primary_bg">@color/primitive_primary_40</color>
<color name="color_bg_surface">@color/primitive_neutral_100</color>
```

- Alpha tokens: Figma `#RRGGBBAA` → Android `#AARRGGBB`; semantics alias `primitive_black_a*` / `primitive_white_a*`.
- Material-only roles with no Figma semantic (`onPrimaryContainer`, `inversePrimary`) may alias a primitive per mode.
- **Forbidden:** hex on semantic or `md_theme_*` entries (except those Material-only primitive aliases). Changing a primitive must update every semantic token that points at it.

## Theme items (required)

```xml
<item name="android:windowBackground">@color/md_theme_background</item>
<item name="android:colorBackground">@color/md_theme_background</item>
<item name="colorSurface">@color/md_theme_surface</item>
<item name="android:includeFontPadding">false</item>
<item name="android:fontFamily">@font/…</item>
<item name="fontFamily">@font/…</item>
```

Also map Figma → Material: `colorPrimary`, `colorOnPrimary`, containers, error, outline, `colorSurfaceContainer*`, typography attrs → `TextStyle.*`.

**Do not** set `materialButtonStyle` / outlined / borderless theme defaults unless product asks — keep standalone `ButtonStyle.*` styles.

**Never add until the user asks:** `android:statusBarColor`, `android:navigationBarColor`, `android:windowLightStatusBar`, `android:windowLightNavigationBar`.

**BottomNavigation:** set `bottomNavigationStyle` → `Widget.App.BottomNavigationView` (or product name) with `itemIconTint` / `itemTextColor` color state lists from Figma selected/unselected icon+label colors (theme attrs). Do not leave Material default tints that ignore app tokens.

Section order in `themes.xml`: App → Shapes → Text Styles → Button Styles → extras.

Do not set unknown Material attrs that fail resource linking (verify `assembleDebug`).

## Type and buttons

- `TextStyle.Heading.H1` … `H3` (+ Medium/Semibold/Bold); `TextStyle.Title.T1` … `T3`; `TextStyle.Body.B1` … `B3`. Sizes from Figma; prefer 4sp multiples when rounding.
- `ButtonStyle.Primary` / `Secondary` / `Tonal` / `Outline` / `Ghost` / `IconButton`:

```xml
<style name="ButtonStyle.IconButton" parent="Widget.Material3.Button.IconButton">
    <item name="android:padding">8dp</item>
    <item name="iconSize">0dp</item>
    <item name="iconTint">?attr/colorIcon</item>
</style>
```

- Height `wrap_content`; fill/stroke via tint/stroke/cornerRadius — no `bg_shape_*` for that.
- `ShapeAppearance.App.Small` / `Medium` / `Large` from Figma radius tokens (typical 8 / 16 / 24).

## Layouts

Default roots inherit `windowBackground`. **Forbidden** on default screens:

```xml
android:background="?attr/colorSurface"
```

Set `android:background` only for a **different** surface (card, overlay, tinted band). Text/icons still use `?attr/colorOnSurface` / `?attr/colorTextTertiary` / etc.

## Out of scope

Full icon library, every Figma component frame, product screens, `dimens.xml`, modules other than `:core-ui`.

## Compose theme Kotlin (`:core-ui` — when `uiFramework` is `compose`)

Write in the **same module** as XML tokens — package `{applicationId}.core.ui.theme`:

| File | Contents |
|------|----------|
| `Color.kt` | Primitive `Color` vals (hex from `values/colors.xml` primitives only) |
| `Type.kt` | `Typography` mapped from Figma `TextStyle.Heading.*` / `Title.*` / `Body.*` |
| `Theme.kt` | `AppTheme` + `lightColorScheme` / `darkColorScheme` from semantic/md_theme mapping |

Rules:

- **Same tokens as XML** — do not invent a second palette for Compose
- Map semantics to Material3 slots (`primary`, `background`, `surface`, `onSurface`, …)
- **No dynamic color** when Figma tokens exist
- Poppins (or Figma family): bundle in `:core-ui` `res/font/` and use `FontFamily(Font(R.font.poppins))` in `Type.kt`
- Ensure `:core-ui` `build.gradle.kts` has `kotlin-compose` + `compose = true` + Compose BOM (see `templates/compose/core-ui.gradle.kts`)
- **Forbidden:** a separate `:core-design` module
