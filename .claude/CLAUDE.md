# Project Rules

You are a Senior Android Engineer. Produce production-ready code: no placeholders, no pseudo code,
no TODOs, no half-finished implementations.

> This file describes an Android **XML + ViewBinding + MVVM** template (single `:app` module).
> If the project you are in does not match this layout, follow the project's actual conventions and
> tell the user this file needs updating — do not force the project into this shape.

---

## Tech Stack

| Area | Choice |
|---|---|
| Language | Kotlin only |
| UI | XML layouts only, View Binding only |
| Architecture | MVVM baseline; MVI for state-heavy features |
| Modules | Single module (`:app`) |
| Navigation | Jetpack Navigation + Safe Args |
| DI | Koin (in the catalog; wire in `common/di` when a feature needs it) |
| Async | Coroutines |
| State | `StateFlow` (LiveData only where a screen already uses it) |
| Effects | `SharedFlow` / `Channel` |
| Images | Glide |
| Sizing | `sdp` for dimens, `ssp`/`sp` for text |
| Firebase | Analytics, Crashlytics, Remote Config |
| Splash | `androidx.core.splashscreen` |
| SDK | min 24, target 37, compile 37 · Java 17 |

Every dependency is declared in `gradle/libs.versions.toml` and referenced as `libs.*`.
Never hardcode a version or coordinate string in `build.gradle.kts`.

## Never

- Never use Java, Jetpack Compose, or modular architecture.
- Never use `GlobalScope`, `AsyncTask`, `findViewById`, Kotlin synthetics, or data binding expressions.
- Never add a second DI framework, image loader, or networking stack.
- Never add a library when an existing helper already covers the need.
- Never rename or move existing packages while implementing a feature.
- Never redesign an existing screen's architecture while adding to it.
- Never hardcode a user-visible string, a hex color, or a `px` value.
- Never claim a build or test passed if you did not run it.

---

## Architecture

MVVM by default. Escalate to MVI only for state-heavy screens or when asked. Never mix both in one feature.

```
presentation/<feature>/
    Fragment<Feature>.kt        // renders state, forwards events — no business logic
    <Feature>ViewModel.kt       // owns state, no Context, no Android framework leaks
    adapters/ interfaces/ models/ repository/
```

MVI adds `state/<Feature>UiState.kt` (immutable data class), `intent/<Feature>Intent.kt` and
`effect/<Feature>Effect.kt` (sealed interfaces), with a single `onIntent(intent)` entry point.

- Repository owns data access. A ViewModel never touches SharedPreferences, files, network, or DB directly.
- Never expose `MutableStateFlow` / `MutableSharedFlow`. Expose `asStateFlow()` / `asSharedFlow()`.
- Dependencies flow one way: presentation → (domain) → data.
- There is **no domain layer**. Introduce `domain/` only when logic is reused across screens, is
  complex enough to deserve isolated tests, or Clean Architecture is explicitly requested. It must
  then stay free of the Android framework.

## Project Structure

```
<namespace>/
├── MainApplication.kt      // theme + locale restore only — keep onCreate light
├── common/                 // firebase, network, observers, permissions, repository, viewmodel
├── helpers/                // stateless extensions: constants datetime handlers lifecycle listener
│                           // locale menu navigation permissions settings theme ui utils
├── presentation/           // MainActivity, base/, dialogs/, startup/, home/, <feature>/
└── storage/                // preferences/SharedPrefManager, provider/
```

Resources: `color/` `drawable/` (`bg_*`, `ic_*`, `flag_*`) `layout/` (`activity_*`, `fragment_*`,
`item_*`, `dialog_*`) `navigation/` `values/` `values-night/` `values-<lang>/`.

Feature-based organisation over type-based. A helper used by two features moves to `helpers/` or `common/`.
**Search `helpers/` before writing any utility** — toast, dialog, navigation, permission, date, screen
size and image loading are already covered. See the `template-helpers` skill for the catalog.

## Kotlin

- `data class` for models · `sealed` for state/intent/effect/result · `object` for stateless helpers.
- This template is extension-heavy — follow that style.
- `viewModelScope` in ViewModels, `lifecycleScope` / `repeatOnLifecycle` in UI, `Dispatchers.IO` for
  disk, network and heavy parsing. Never block the main thread.
- Avoid `!!`. Use `?.`, `?:`, `requireNotNull`, or an early return.
- Prefer `val`; expose `List`, not `MutableList`; copy state with `state.copy(...)`.
- No magic numbers or strings — use a `const val` or `helpers/constants`. Preference, intent and
  bundle keys are private constants.
- Naming: `Fragment<Feature>`, `<Feature>Activity`, `<Feature>ViewModel`, `Adapter<Item>`, `<Feature>Repository`.
- Comments explain intent, not syntax. Match the comment density of the file you are editing.

## UI

Material 3 components, ConstraintLayout for screens, `RecyclerView` + `ListAdapter` for lists.
View ids in snake_case so ViewBinding produces readable camelCase.

**Every screen must work in light and dark theme.** Use semantic color tokens (`surfaceColor`,
`onSurfaceTitle`, `onSurfaceMessage`, `onSurfaceHint`, `surfaceVariant`, `cardBackground`,
`onCardTitle`, `onCardMessage`, `outlineColor`, `rippleColor`, `nav*`, `tab*`) — never raw palette
colors from a layout or drawable. A new semantic color goes into **both** `values/colors.xml` and
`values-night/colors.xml`. Details in the `android-ui-theming` skill.

Every user-visible string goes in `values/strings.xml`. Set `contentDescription` (or
`importantForAccessibility="no"`), respect 48dp touch targets, and support font scaling.

Activities extend `BaseActivity` (edge-to-edge + insets via `getInsetsMode()`); fragments extend
`BaseFragment` (owns and clears the binding). Never call `setContentView`, never override
`onCreateView`/`onDestroyView`, never keep your own `binding` field.

## Lifecycle & Safety

- Collect flows with `repeatOnLifecycle(STARTED)` or `flowWithLifecycle`.
- Before touching UI after async work: `isAdded`, `view != null`, binding not null,
  `Activity?.isActivityExist()`. Prefer the `launchWhenCreated/Started/Resumed` helpers.
- Navigate through `helpers/navigation/NavigationHelper.kt` (`navigateTo`, `popFrom`) — it guards
  `isAdded` and the current destination. Use Safe Args directions.
- Guard taps with `View.setOnRapidClickSafeListener { }`. Use `goBackPressed { }`, not `onBackPressed()`.
- Never hold an Activity, Fragment, View, or Context in a singleton, companion object, or static field.
- Cancel timers, handlers, listeners, and receivers in the matching lifecycle callback.

## Error Handling

- Never swallow an exception or leave an empty `catch`. Log with a consistent TAG, recover, or surface it.
- Return a meaningful `Result`, sealed result type, or UI state. Show a user-friendly message
  (`showToast` / `showSnackBar`) — never a raw exception message.
- Report non-fatals with `Throwable.recordException(logTag)`.
- Rethrow `CancellationException` — never turn cancellation into a failure state.
- Validate user input, permission state, nulls, API/Remote Config responses, and list indices
  (`Int.isValidPosition(list)`).
- Legacy helper files use `try/catch { printStackTrace() }`. Keep that defensive shape when editing
  them, but log with a TAG. Do not copy the pattern into new feature code.

## Performance

- `ListAdapter` + `DiffUtil` — never `notifyDataSetChanged()`. No allocations, date formatting, or
  preference reads inside `onBindViewHolder`. Use `bindingAdapterPosition`, not a captured position.
- Nothing heavy on the main thread, in `onDraw`/`onLayout`, or in scroll listeners.
- Flatten layouts with ConstraintLayout chains and guidelines instead of nesting.
- Lazy-initialise expensive components. No disk or network I/O during startup.
- Cache results when inputs do not change. Target 16ms frames.

## Compatibility

Code must run on API 24–37. Guard version-specific behaviour with `Build.VERSION.SDK_INT`.
Common traps: notification permission (13+), exact alarms (12+), foreground service types (14+),
`PendingIntent` mutability (23+/31+), scoped storage (29+), edge-to-edge and status bar color
deprecations (35+), receiver export flags (33+).

Release builds shrink with R8 (`optimization { enable = true }`) — reflection, Gson models and
Firebase need keep rules in `app/src/main/keepRules/*.keep`.

## Testing

Write testable code: logic independent of the Android framework, injected dependencies, no
static mutable state, no `Context` in ViewModels, pure functions for calculation/formatting/validation.

This template ships no test suite. Do not scaffold a testing framework unless asked. When asked,
put tests in `app/src/test` (JVM) or `app/src/androidTest`, with dependencies in the version catalog.

---

## How to Work

1. Read the closest existing feature and copy its shape before writing anything.
2. Plan: screens, state, events, data sources, permissions, manifest entries, lifecycle, API-level
   differences, and edge cases (empty, loading, error, offline, denied permission, rotation).
3. Reuse before you create. Search the project for an existing implementation first.
4. Keep the diff minimal and focused — no drive-by reformatting, renaming, or "modernising".
5. If you find a second problem while fixing the first, report it instead of silently fixing it.
6. Report honestly: what changed, what you verified and how, what you left out, what needs manual testing.

## Skills

Load these for the matching task — they carry the detailed procedure and checklists.

| Skill | Use when |
|---|---|
| `android-feature` | Building a feature, screen, dialog, or enhancement |
| `android-ui-theming` | Layouts, colors, dark theme, strings, localization, accessibility |
| `template-helpers` | Looking for an existing base class, helper, or pattern |
| `android-build-config` | Gradle, AGP 9 DSL, version catalog, R8 keep rules, manifest |
| `android-new-project` | Turning a fresh copy of the template into a new app |
| `android-upgrade` | Dependency/SDK upgrades, migrations, refactors, bug fixes |
| `android-preflight` | Final verification before declaring work done |
