---
description: XML layouts, drawables, strings, colors, and themes
paths:
  - "**/res/**/*.xml"
---

# XML resources (invariants)

**Full detail (all naming tables, examples, BAD/GOOD):** [reference/resources-xml.md](reference/resources-xml.md)

Applies to `res/**/*.xml` (strings, colors, themes, drawables, and XML layouts). When `uiFramework` is `compose`, still obey string/color/theme/drawable rules; **do not** author `fragment_*` screen layouts — use `28-compose-ui` + `hsg-figma-to-compose`.

## Resource module placement (mandatory)

| Module | Allowed `res/` |
|--------|----------------|
| `:presentation` | `layout` (+ `layout-land`), `menu`, `navigation` **only** |
| `:app` | `mipmap`, `xml`, launcher-related `drawable` only |
| `:core-ui` | Everything else — `anim`, `drawable`, `color`, `font`, `values` / `values-night`, `raw`, splash, etc. |

## Must follow

- Layout prefixes: `fragment_` / `activity_` / `item_` / `dialog_` / `bottom_sheet_` / `layout_`
- Root screen id: `clRoot<Screen>`; `tools:context` → Fragment
- View IDs: Hungarian + camelCase (`mb`, `mtv`, `siv`, `mcv`, `cl`, `rcv`, …)
- **No `dimens.xml`** — inline `dp`/`sp`, multiples of 4
- All user-facing strings in **one** `:core-ui` `strings.xml` (App → General → Content Descriptions `cd_*` → Screen-wise)
- Material widgets only; images = `ShapeableImageView` (`siv`); clickable icons = `ButtonStyle.IconButton` (`mb` + `app:icon`)
- Programmatic images: Glide `loadImage` — not `setImageResource` / raw `Glide.with` in UI
- MaterialButton solid+stroke: tint / stroke / cornerRadius on the button — **no** `bg_shape_*` for that case
- Filled/text buttons: `layout_height="wrap_content"` — no fixed height + inset hacks
- Clickable chips / language selectors: `MaterialButton` + `iconGravity="end"` — not MTV + `drawableEnd`
- View Binding only; RecyclerView manager / orientation / spanCount in XML unless dynamic; prefer `overScrollMode="ifContentScrolls"`
- Portrait + landscape; theme attrs for colors; section headers in strings/colors/themes
- Default screen color via theme `android:windowBackground` — **not** `android:background="?attr/colorSurface"` on default roots
- Sticky footers (Language continue, Premium CTA+legal): primary actions **outside** `NestedScrollView`; scrollable content above
- XML closing: blank line between nested container closes; no extra blank after root
- **Shape / gradient drawables:** `android:angle` on `<gradient>` **must be a multiple of 45** (0, 45, 90, …) — other angles inflate-crash (`resources-xml.md`)

## Themes (`:core-ui` `themes.xml`)

- Sections: App → Shapes → Text Styles → Button Styles → extras
- `Base.Theme.App`: Material color attrs + `windowBackground` + fonts — **do not** set `materialButtonStyle` to hang all buttons off the parent unless product asks
- Always: `android:includeFontPadding=false`, both `android:fontFamily` and `fontFamily`
- **Never until user asks:** `android:statusBarColor`, `android:navigationBarColor`, `android:windowLightStatusBar`, `android:windowLightNavigationBar`
- `ButtonStyle.IconButton`: padding `8dp`, `iconSize` `0dp`, default `iconTint` `?attr/colorIcon` (layouts may override)
- BottomNavigation: set `bottomNavigationStyle` on `Base.Theme.App` → project style with theme-attr `itemIconTint` / `itemTextColor` selectors (map from Figma in `hsg-setup-design-system`)

## Premium paywall layout

- Sticky footer: CTA + legal fixed at bottom; benefits/plans in `NestedScrollView` above
- Close (X): hidden until State `showCloseButton` — see `33-screen-premium` / `reference/premium-billing.md`

Read [reference/resources-xml.md](reference/resources-xml.md) before writing or changing layouts.
