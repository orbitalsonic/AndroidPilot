# XML layouts, drawables, strings, colors, and themes

Full detail for `09-resources-xml.md`. Do not delete lines from this file — edit here and keep the rule stub in sync.

## Layout naming (snake_case)

| Prefix | Usage |
|--------|-------|
| `fragment_` | Screen layouts |
| `activity_` | Activities |
| `item_` | RecyclerView rows |
| `dialog_` | Dialogs |
| `bottom_sheet_` | Bottom sheets |
| `layout_` | Reusable include blocks |

Example: `fragment_home.xml`, `item_language.xml`

## Resource module placement (mandatory)

| Module | Allowed `res/` |
|--------|----------------|
| `:presentation` | `layout` (+ `layout-land`), `menu`, `navigation` **only** |
| `:app` | `mipmap`, `xml`, launcher-related `drawable` only |
| `:core-ui` | `anim`, `drawable`, `color`, `font`, `values` / `values-night`, `raw`, splash, etc. |

Do not put themes/strings/colors in `:app` or layouts in `:core-ui`.

## View ID naming (prefix + camelCase)

| Prefix | Widget |
|--------|--------|
| `mb` | MaterialButton |
| `mtv` | MaterialTextView |
| `siv` | ShapeableImageView |
| `mcv` | MaterialCardView |
| `cl` | ConstraintLayout |
| `rcv` | RecyclerView |
| `fcv` | FragmentContainerView |
| `bnv` | BottomNavigationView |
| `cpi` | CircularProgressIndicator |
| `lpi` | LinearProgressIndicator |

Pattern: `{prefix}{Role}{Context}` → `mtvHeadingHome`, `sivLogoEntrance`, `mbContinueLogin`

## Drawable naming

| Prefix | Usage |
|--------|-------|
| `ic_svg_` | Vector icons |
| `ic_png_` | Raster icons |
| `bg_shape_` | Shape / selector backgrounds (XML shapes) |
| `bg_svg_` | Backgrounds imported as SVG/vector |
| `bg_` | Other backgrounds / selectors (when not shape/svg) |
| `fg_` | Foreground overlays / gradients |
| `img_svg_` / `img_png_` | Large illustrations |
| `flag_` | Language / country flags when used |

Examples: `bg_shape_rounded_primary.xml`, `bg_svg_onboarding_header.xml`, `fg_gradient_header.xml`

### Gradient / shape drawables (mandatory)

Android only accepts gradient `android:angle` values that are **multiples of 45** (`0`, `45`, `90`, `135`, `180`, `225`, `270`, `315`). Any other value (e.g. `30`, `60`, `100`) causes **InflateException** at runtime.

When converting Figma gradients:

1. Round the design angle to the **nearest multiple of 45**
2. Prefer exact Figma angle when it is already 0/45/90/…
3. Document in a comment if you rounded (e.g. Figma `30°` → `45`)

```xml
<!-- BAD — crashes on inflate -->
<gradient
    android:angle="30"
    android:startColor="?attr/colorPrimary"
    android:endColor="?attr/colorSurface"
    android:type="linear" />

<!-- GOOD -->
<gradient
    android:angle="45"
    android:startColor="?attr/colorPrimary"
    android:endColor="?attr/colorSurface"
    android:type="linear" />
```

Same rule for `bg_shape_*`, `fg_*`, and any `<shape>` / `<layer-list>` that embeds `<gradient>`.

## Fonts and motion

- Fonts in shared UI module (`res/font/`) + `preloaded_fonts.xml` when using downloadable/preloaded fonts
- Transitions: `res/anim/` (and `anim-ldrtl/` for RTL flips)
- Nav transitions: mandatory `slide_in_right` / `slide_out_left` / `slide_in_left` / `slide_out_right` on actions — see `17-navigation`
- Night-specific assets: `drawable-night/`, `values-night/` — avoid duplicating full layouts for dark mode

## Spacing & text sizes (no dimens.xml)

- **Never create `dimens.xml`** (or other dimens resource files)
- Use inline `dp` / `sp` in layouts and styles
- All sizes must be **multiples of 4**: `4dp`, `8dp`, `12dp`, `16dp`, `20dp`, `24dp`, `32dp`, `40dp`, `48dp`, …
- Text: prefer multiples of 4 (`12sp`, `14sp` only if design requires; prefer `12sp`/`16sp`/`20sp`/`24sp`/`28sp`)
- Prefer shared `TextStyle.*` / `ButtonStyle.*` over one-off sizes when possible

```xml
<!-- ✅ GOOD -->
android:layout_margin="16dp"
android:padding="8dp"
android:textSize="16sp"

<!-- ❌ BAD -->
android:layout_margin="@dimen/margin_default"
android:padding="10dp"
android:textSize="15sp"
```

## Resource file sections (mandatory order)

In `:core-ui` `strings.xml`, `colors.xml`, and `themes.xml`, organize with **XML comment section headers**. Always order:

1. **App-level** (top) — identity, Material theme tokens, base app theme
2. **General / common** — actions, toasts, dialogs, **content descriptions**, text/button styles, semantic colors
3. **Screen-wise** (bottom) — per feature/screen blocks

```xml
<!-- _____________________________ App ______________________________ -->
<!-- _____________________________ Actions ______________________________ -->
<!-- ________________________ Content Descriptions ________________________ -->
<!-- _______________________________ Home _______________________________ -->
```

When adding a string/color/style: put it in the correct section — do not append at the file end out of order. Mirror order in `values-night/` and `values-xx/` locale files.

## Strings (single source of truth)

- **All** user-facing strings live in **one** shared module file (`:core-ui` `values/strings.xml`)
- No hardcoded user-facing text in layouts or Kotlin
- Non-translatable config: `translatable="false"`

### Strings section order

| Order | Section | Examples |
|-------|---------|----------|
| 1 | App | `app_name` |
| 2 | General — Ads / Toast / Actions / Dialogs / Bottom sheets | `toast_*`, `action_*` |
| 3 | **Content Descriptions** (dedicated) | `cd_*` |
| 4 | Screen-wise UI | `<!-- Entrance -->`, `<!-- Home -->`, … |

Naming: `app_name`; `toast_*` / `action_*`; `cd_{what}`; `{screen}_title` / `{feature}_*`

### Content descriptions (mandatory section)

```xml
<!-- ________________________ Content Descriptions ________________________ -->
<string name="cd_back">Back</string>
<string name="cd_app_logo">App logo</string>
```

- Always set `android:contentDescription="@string/cd_…"` on `ShapeableImageView`, icon `MaterialButton`s, and meaningful icons
- Decorative only: `android:contentDescription="@null"` + `android:importantForAccessibility="no"`
- Never hardcode contentDescription in XML/Kotlin

## Colors

File: `:core-ui` `values/colors.xml` (+ `values-night/colors.xml` when needed).

| Order | Section | Examples |
|-------|---------|----------|
| 1 | App — Material 3 tokens | `md_theme_primary`, … |
| 2 | General / common | `colorPrimary`, `colorTextBody`, … |
| 3 | Screen / feature-wise | `premiumGradientStart`, … |

- Prefer `?attr/colorOnSurface` (and other theme color attrs) for **text / icons**
- **Default screen fill belongs on the theme**, not the layout:
  - Set `android:windowBackground` (and `android:colorBackground` / `colorSurface`) in `:core-ui` `themes.xml` / `values-night/themes.xml`
  - Do **not** set `android:background="?attr/colorSurface"` (or `@color/md_theme_surface`) on default screen roots — the window already paints that color
  - Set `android:background` on a view **only** when that region is a **different** surface (card, overlay, tinted header)
- Light/dark via `values` + `values-night`

```xml
<!-- BAD — default root repeating the window color -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/clRootHome"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorSurface">

<!-- GOOD — inherit windowBackground; tint only a different region -->
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/clRootHome"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/mcvBannerHome"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:cardBackgroundColor="?attr/colorPrimaryContainer"
        … />
```

## Themes

| File | Contents |
|------|----------|
| `themes.xml` | App base theme + common widget styles |
| `splash.xml` | `Theme.App.Starting` only (`23-app-startup`) |

Order in `themes.xml` (section comments): **App** → **Shapes** → **Text Styles** → **Button Styles** → extras / screen-specific styles only if needed.

`Base.Theme.App` (parent `Theme.Material3.DayNight.NoActionBar`):

- Material color attrs + `android:windowBackground` / `android:colorBackground` / `colorSurface`
- Fonts: `android:includeFontPadding=false`, both `android:fontFamily` and `fontFamily`
- Typography / shapeAppearance refs to project `TextStyle.*` / `ShapeAppearance.App.*`
- Custom semantic attrs (`colorText*`, `colorIcon*`) as Figma requires
- **Do not** set `materialButtonStyle` (or similar) to force all buttons through one parent style unless product asks — define standalone `ButtonStyle.*` / `TextStyle.*` from Figma
- **Never add until the user asks:**
  - `android:statusBarColor`
  - `android:navigationBarColor`
  - `android:windowLightStatusBar`
  - `android:windowLightNavigationBar`

**BottomNavigation (theme-driven):**

- On `Base.Theme.App` set `bottomNavigationStyle` → e.g. `@style/Widget.App.BottomNavigationView`
- That style sets `itemIconTint` / `itemTextColor` to color state lists using theme attrs (selected ≈ `colorPrimary` or Figma semantic; unselected ≈ `colorOnSurfaceVariant` / Figma icon secondary)
- Map concrete colors from Figma during `sonicg-setup-design-system` — do not leave Material default tints that ignore app tokens
- Avoid one-off `app:itemIconTint` only on the BNV XML unless Figma forces a one-screen exception

Default window fill: `android:windowBackground` on the app theme — see Colors above. Figma design-system import: `sonicg-setup-design-system`.

### `ButtonStyle.IconButton` (mandatory when missing)

```xml
<style name="ButtonStyle.IconButton" parent="Widget.Material3.Button.IconButton">
    <item name="android:padding">8dp</item>
    <item name="iconSize">0dp</item>
    <item name="iconTint">?attr/colorIcon</item>
</style>
```

Layouts may override `app:iconTint` (`?attr/colorIconSecondary`, brand color, or `@null` for multi-color drawables).

## Orientation (mandatory)

- Support **portrait and landscape**
- ConstraintLayout / `layout-land/` when needed
- Do not lock orientation unless product requires it

## Layout rules

- Root screen: `ConstraintLayout` id `clRoot<Screen>`; `tools:context` to the Fragment class
- No root `android:background` for default screens (theme `windowBackground`)
- Material widgets: `MaterialTextView`, `MaterialButton`, `MaterialCardView`, `ShapeableImageView`
- **Images (non-clickable):** always `ShapeableImageView` — never `ImageView` / `AppCompatImageView`; IDs use `siv` prefix
- **Programmatic image loads:** always Glide via `ImageView.loadImage(...)` (`:core-ui` `ImageViewExtensions`) — never `setImageResource` / `setImageBitmap` / raw `Glide.with` in adapters/Fragments for content images
  - Template: `sonicg-setup-new-project/templates/base/ImageViewExtensions.kt`
  - Supports `@DrawableRes`, URL `String`, `Uri`, etc.; optional `placeholder` / `error` / `CachePolicy`
  - Static icons declared in XML with `app:srcCompat` / `app:icon` are fine; dynamic/list/remote/drawable-from-code → `loadImage`
- **Clickable icons / icon-only actions:** always `MaterialButton` with `ButtonStyle.IconButton` — **not** `ShapeableImageView` + click listener
  - Project style: `style="@style/ButtonStyle.IconButton"` (parent `Widget.Material3.Button.IconButton` in `:core-ui` `themes.xml`)
  - Else fallback: `style="@style/Widget.Material3.Button.IconButton"`
  - ID prefix `mb…`; use `app:icon` (not `app:srcCompat`); keep `android:contentDescription="@string/cd_…"`
  - Style defaults: padding `8dp`, `iconSize` `0dp`, `iconTint` `?attr/colorIcon`; override tint in layout when needed
- **Sticky footers:** when design shows a fixed CTA (Language continue, Premium start + legal), keep those views **outside** `NestedScrollView` / list — constrain to bottom of root; scrollable content `layout_constraintBottom_toTopOf` the sticky block
- **Lists / scroll:** prefer `android:overScrollMode="ifContentScrolls"`; `clipToPadding` as needed
- **MaterialButton fill + stroke (glass / outlined / tinted surfaces):** set attrs **on the button** — **do not** create `bg_shape_*` oval/rect drawables just for solid + stroke
  - Fill: `app:backgroundTint`
  - Stroke: `app:strokeColor` + `app:strokeWidth`
  - Shape: `app:cornerRadius` (circle ≈ half of width/height) or `app:shapeAppearance`
  - **Forbidden for this case:** `android:background="@drawable/bg_shape_…"`, `app:backgroundTint="@null"`, and `insetTop`/`insetBottom` `0dp` only to force a custom drawable to fit
  - Reserve `bg_shape_*` for non-button views (roots, cards wrappers, RecyclerView items) or gradients / selectors Material attrs cannot express
- **MaterialButton height (filled / tonal / outlined text buttons):** use `android:layout_height="wrap_content"` — **do not** force a fixed height (e.g. `40dp`) with `android:insetTop` / `android:insetBottom` `0dp` to fake it
  - Let Material min height + padding/`textAppearance` define size; override via theme/`ButtonStyle.*` if product needs a shorter button globally
  - Icon-only `ButtonStyle.IconButton` stays `wrap_content` unless design needs a fixed tap target (still use tint/stroke/cornerRadius — no shape drawable)
- **Clickable chips / language selectors (text + trailing arrow):** use **`MaterialButton`** — **not** `MaterialTextView` + `drawableEnd`, and **not** `LinearLayout` + text + `ShapeableImageView`
  - ID prefix `mb…`; `android:layout_height="wrap_content"`; no `insetTop`/`insetBottom` hacks
  - Icon on the **right**: `app:icon` + `app:iconGravity="end"` (+ `app:iconPadding` as needed)
  - Surface via Material style + `backgroundTint` / stroke attrs — **do not** set `android:background="@drawable/bg_shape_…"`
  - Non-clickable text + decorative icon only: compound drawables on `MaterialTextView` are OK; interactive selectors must be buttons
- **View Binding only** — never `findViewById`, never Data Binding
- Shallow ConstraintLayout trees — avoid extra wrappers
- **Static layout config in XML** — not Kotlin — unless it must change at runtime:
  - `RecyclerView`: set `app:layoutManager`, `android:orientation`, `app:spanCount` (grids) in XML
  - `LinearLayout` / `LinearLayoutCompat`: `android:orientation` in XML
  - Do **not** call `layoutManager = LinearLayoutManager(...)` / `setOrientation(...)` in Fragment/Activity for fixed lists
  - Code only when dynamic (e.g. swap horizontal↔vertical or span count from state/config)
- RecyclerView: `ListAdapter` + `DiffUtil`; declare manager in XML as above
- Inline `dp`/`sp` multiples of 4 — **no `dimens.xml`**
- **XML closing / EOF formatting (layouts):**
  - Put a **blank line between nested container closing tags** (e.g. after `</ConstraintLayout>` before `</MaterialCardView>`, and before the root close)
  - End the file on the **root closing tag** — **no** extra blank line after `</Root>` (single trailing newline for EOF only)
  - Do **not** jam nested closes back-to-back without the blank line between major containers

```xml
<!-- BAD — no blank between nested closes; extra blank after root -->
            </LinearLayout>

        </androidx.constraintlayout.widget.ConstraintLayout>
    </com.google.android.material.card.MaterialCardView>

</androidx.constraintlayout.widget.ConstraintLayout>

<!-- GOOD — blank line between nested container closes; no blank after root -->
            </LinearLayout>

        </androidx.constraintlayout.widget.ConstraintLayout>

    </com.google.android.material.card.MaterialCardView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### RecyclerView XML example

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rcvLanguages"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:orientation="vertical"
    android:clipToPadding="false"
    android:overScrollMode="ifContentScrolls"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"
    tools:listitem="@layout/item_language" />

<!-- Grid -->
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/rcvCompassStyles"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:overScrollMode="ifContentScrolls"
    app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
    app:spanCount="2"
    tools:listitem="@layout/item_compass_style" />
```


### Icon button conversion example

```xml
<!-- BAD — clickable image pretending to be a button -->
<com.google.android.material.imageview.ShapeableImageView
    android:id="@+id/sivMenuHome"
    android:layout_width="40dp"
    android:layout_height="40dp"
    android:contentDescription="@string/cd_menu"
    android:scaleType="centerInside"
    app:srcCompat="@drawable/ic_svg_menu_home" />

<!-- GOOD — ButtonStyle.IconButton -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbMenuHome"
    style="@style/ButtonStyle.IconButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:contentDescription="@string/cd_menu"
    app:icon="@drawable/ic_svg_menu_home"
    app:layout_constraintBottom_toBottomOf="@id/mtvTitleHome"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="@id/mtvTitleHome" />
```

If `ButtonStyle.IconButton` is missing, add it under Button Styles in `:core-ui` `themes.xml` (see Themes section above). Override `app:iconTint` in the layout when the default `?attr/colorIcon` is wrong for that icon.

### MaterialButton solid + stroke (no shape drawable)

```xml
<!-- BAD — extra oval drawable + background override + inset hacks -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbCloseOcr"
    style="@style/ButtonStyle.IconButton"
    android:layout_width="44dp"
    android:layout_height="44dp"
    android:background="@drawable/bg_shape_ocr_glass_circle"
    android:insetTop="0dp"
    android:insetBottom="0dp"
    android:padding="8dp"
    app:backgroundTint="@null"
    app:icon="@drawable/ic_svg_back"
    app:iconTint="@color/white"
    … />

<!-- BAD drawable — do not create for button solid+stroke -->
<!-- bg_shape_ocr_glass_circle.xml: oval + solid + stroke -->

<!-- GOOD — tint + stroke + cornerRadius on the button -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbCloseOcr"
    style="@style/ButtonStyle.IconButton"
    android:layout_width="44dp"
    android:layout_height="44dp"
    android:layout_marginStart="16dp"
    android:layout_marginTop="16dp"
    android:contentDescription="@string/cd_ocr_close"
    android:padding="8dp"
    app:backgroundTint="@color/color_ocr_glass"
    app:cornerRadius="22dp"
    app:icon="@drawable/ic_svg_back"
    app:iconTint="@color/white"
    app:strokeColor="@color/color_ocr_glass_stroke"
    app:strokeWidth="1dp"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### MaterialButton height (filled / text actions)

```xml
<!-- BAD — fixed height + inset hacks -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbTranslateTranslate"
    style="@style/Widget.Material3.Button"
    android:layout_width="0dp"
    android:layout_height="40dp"
    android:insetTop="0dp"
    android:insetBottom="0dp"
    android:text="@string/translate_action_translate"
    … />

<!-- GOOD — wrap_content, no insets -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbTranslateTranslate"
    style="@style/Widget.Material3.Button"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:text="@string/translate_action_translate"
    android:textAppearance="@style/TextStyle.Heading.H6.Medium"
    … />
```

### Clickable language / chip selectors (text + end icon)

```xml
<!-- BAD — MaterialTextView + custom chip background + drawableEnd -->
<com.google.android.material.textview.MaterialTextView
    android:id="@+id/mtvFromLanguageTranslate"
    style="@style/TextStyle.Heading.H6.Medium"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:background="@drawable/bg_shape_translate_lang_chip"
    android:drawableEnd="@drawable/ic_svg_translate_arrow_down"
    android:ellipsize="end"
    android:maxLines="1"
    android:padding="12dp"
    android:textColor="?attr/colorOnSurface"
    app:layout_constraintHorizontal_weight="1"
    tools:text="English" />

<!-- ALSO BAD — LinearLayout + TextView + ImageView -->
<!-- Prefer neither wrapper nor MTV chip for tappable selectors -->

<!-- GOOD — MaterialButton + Material surface + icon end (no bg_shape_*) -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/mbFromLanguageTranslate"
    style="@style/Widget.Material3.Button.OutlinedButton"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:contentDescription="@string/cd_source_language"
    android:ellipsize="end"
    android:maxLines="1"
    android:textAppearance="@style/TextStyle.Heading.H6.Medium"
    app:icon="@drawable/ic_svg_translate_arrow_down"
    app:iconGravity="end"
    app:iconPadding="8dp"
    app:layout_constraintHorizontal_weight="1"
    tools:text="English" />
```

Use project `ButtonStyle.*` when available instead of raw `Widget.Material3.*`. Do **not** assign `android:background="@drawable/bg_shape_…"` — delete those chip backgrounds for button selectors.
