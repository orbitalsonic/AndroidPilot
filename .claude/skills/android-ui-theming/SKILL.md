---
name: android-ui-theming
description: Rules for Android XML layouts, Material 3 components, semantic color tokens, light/dark theming, dimensions (sdp/ssp), string resources and localization, accessibility, and edge-to-edge insets. Use when creating or editing any layout, drawable, style, color, vector, or string resource, or when a screen must work in both themes or multiple languages.
---

# UI, Theming and Localization

XML layouts only. View Binding only. Material Design (Material 3 components).

## Layout

Prefer `ConstraintLayout` for screens, `RecyclerView` + `ListAdapter` for lists, and Material
components (`MaterialTextView`, `MaterialButton`, `MaterialToolbar`, …).

Avoid deep nesting, unnecessary `ViewGroup`s, `findViewById`, Kotlin synthetics, and data binding
expressions.

## Naming

| Kind | Convention |
|---|---|
| Layouts | `activity_*.xml`, `fragment_*.xml`, `item_*.xml`, `dialog_*.xml` |
| Drawables | `bg_*`, `ic_*`, `flag_*` |
| View ids | snake_case (`btn_app_theme`, `mtv_version`) → readable camelCase in ViewBinding |

## Dimensions and text

- Spacing and sizes: `@dimen/_16sdp`
- Text: `@dimen/_14ssp` or `sp` — never `dp` for text
- No hardcoded `px`

## Theme — every screen works in light and dark

- The base theme parent stays a `DayNight` theme.
- Semantic colors live in `values/colors.xml`, dark overrides in `values-night/colors.xml`.
- Use semantic tokens in layouts and drawables: `surfaceColor`, `onSurfaceTitle`, `onSurfaceMessage`,
  `onSurfaceHint`, `surfaceVariant`, `cardBackground`, `onCardTitle`, `onCardMessage`, `outlineColor`,
  `rippleColor`, `navSelected`, `navUnselected`, `navIndicator`, `tab*`.
- **Never** reference a raw palette color (`neutral500`, `primary100`, `white`, `black`) from a layout
  or drawable — unless the color is intentionally identical in both themes (e.g. white text on a
  primary-colored button).
- **Never** hardcode a hex color in a layout, drawable, or vector. Add a token instead.
- New vector icons reference `@color/...` for fills that must flip with the theme.
- A new semantic color MUST be added to **both** `values/colors.xml` and `values-night/colors.xml`.

Theme behaviour: follows the system by default (`AppTheme.SYSTEM`); manual switching through
`applyAppTheme(mode)` in `helpers/theme/ThemeHelper.kt`; persisted in `SharedPrefManager.appThemeMode`
and restored in `MainApplication.onCreate`.

## Localization

- Every user-visible string goes in `values/strings.xml`. Never hardcode text in a layout or in Kotlin.
- Mark non-translatable values with `translatable="false"`.
- The project ships `values-<lang>/strings.xml` for many locales. Add the new key to the default file
  at minimum, and state explicitly that translations are pending.
- Language switching goes through `helpers/locale/LocaleHelper.applyLanguage(code)` (AppCompat per-app
  locales) and `SharedPrefManager.appLanguageCode`.

## Accessibility

- Set `contentDescription`, or `importantForAccessibility="no"` for decorative images.
- Support dynamic font scaling — no fixed-height text containers.
- Respect the 48dp minimum touch target.
- Keep sufficient contrast in BOTH themes.

## Edge to edge

Activities extend `BaseActivity`, which enables edge-to-edge and applies system bar insets. Choose
the behaviour by overriding `getInsetsMode()` with an `InsetsMode` value (`EDGE_TO_EDGE`, `TOP`,
`BOTTOM`, `TOP_BOTTOM`). Do not re-implement inset handling inside a screen.

## UI logic

Keep UI logic and state in the ViewModel. Fragments only render state and forward events.
