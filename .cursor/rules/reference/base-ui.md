---
description: Full detail for 19-base-ui — Parent* / Base* classes, InsetsMode, ViewBinding lifecycle, Flow collection, Fragment member order
globs: ["**/ui/**/*.kt", "**/base/**/*.kt", "**/presentation/**/*.kt", "**/feature*/**/*.kt", "**/core/ui/**/*.kt"]
alwaysApply: false
---

# Base UI classes, ViewBinding lifecycle, and Flow collection

Full detail for `19-base-ui.mdc`. Do not delete lines from this file — edit here and keep the rule stub in sync.

When `uiFramework` is `compose`, do not scaffold `ParentFragment` / View Binding bases — use [compose-ui.md](compose-ui.md) (`*Screen` / `AppTheme` / `:app` `MainActivity`). This file stays the XML `Parent*` contract.

## Base hierarchy

```
:core-ui
  ParentActivity / ParentFragment     ← ViewBinding + lifecycle hooks
  ParentDialogDismissal → ParentDialog
  ParentSheetDismissal  → ParentSheet   ← BottomSheetDialogFragment

:presentation (optional product layer)
  BasePermissionFragment → BaseFragment → Feature Fragment
  BaseActivity / BaseDialog / BaseSheet
```

**Templates (copy + replace `YOUR.PACKAGE`):**  
`.cursor/skills/project/sonicc-setup-new-project/templates/base/`  
See that folder’s `README.md`. Same shape as the company template; `ParentSheet` is null-safe (not `!!`).

### ParentFragment
- `bindingFactory: (LayoutInflater) -> T`
- `onCreateView`: inflate `_binding`, call **`onViewInflated()`** (open, optional — early view setup before `onViewCreated`), return root
- `onViewCreated(view, bundle)` → `initObservers()` → `onViewCreated(Bundle?)` → abstract `onViewCreated()`
- Clear `_binding` in `onDestroyView`; throw if accessed outside view lifecycle

### ParentActivity (edge-to-edge via `InsetsMode`)

```kotlin
enum class InsetsMode {
    EDGE_TO_EDGE,  // no padding — content under both bars
    TOP,           // status bar only
    BOTTOM,        // nav bar / IME only
    TOP_BOTTOM,    // both (default)
}
```

- `onCreate` order (fixed): `initSplashScreen()` → `super.onCreate()` → `setupInsets(getInsetsMode())` (= `enableEdgeToEdge()` + `setContentView(binding.root)` + `ViewCompat.setOnApplyWindowInsetsListener`) → `onCreated()` → `initObservers()`
- Insets listener: `systemBars` left/right always; `top` only for `TOP` / `TOP_BOTTOM`; `bottom` = IME bottom when keyboard visible else system-bar bottom, only for `BOTTOM` / `TOP_BOTTOM`; returns `WindowInsetsCompat.CONSUMED`
- `open fun getInsetsMode(): InsetsMode = InsetsMode.TOP_BOTTOM` — override per Activity (`StartupActivity` / `MainActivity`) when the design needs `EDGE_TO_EDGE`; never toggle padding per destination
- `open fun initSplashScreen() {}` — `StartupActivity` overrides with `installSplashScreen()`; `MainActivity` does not
- `enableMaterialDynamicTheme()` — `DynamicColors.applyToActivityIfAvailable(this)`; no `GlobalContext` checks (Koin starts in Application before any Activity — see `07`, `23`)
- Abstract `onCreated()`; optional `initObservers()`
- Status-bar icon tint: `updateStatusBar(darkIcons = true/false)` from `ActivityExtensions` in `onCreated()` — do not set `statusBarColor` / `navigationBarColor` unless asked (`09`)

### ParentDialog
- Dismissal base: `onDismissCallback` + `safeShow` / `safeDismiss`
- Dialog: `MaterialAlertDialogBuilder` + ViewBinding; null-safe binding; clear on destroy

### ParentSheet
- Dismissal base: `BottomSheetDialogFragment` + `dismissCallback` + `safeShow` / `safeDismiss`
- Sheet: ViewBinding via factory; **null-safe** `_binding` (same as Fragment — never `!!`)
- `onSheetCreated()`, optional `initObservers()`
- Prefer expanded/skip-collapsed helpers in `onStart` when needed — keep Binding lifecycle strict

### Base* (presentation)
- Thin wrappers over Parent* for shared product helpers
- Put ads/billing injects on `BaseFragment` / `BaseActivity` only when the app has those modules — keep Parent* free of ads
- Permission helpers live on `BasePermissionFragment`; dialog copy must use `:core-ui` strings before release

## Flow collection (`:core-ui` extensions)

- Fragment helpers: `templates/base/FragmentExtensions.kt` → `…core.ui.extensions.FragmentExtensions.kt`  
  Uses **`viewLifecycleOwner`** for `collectWhen*` / `repeatWhen` / `launchWhen*` (survives only with the view — avoids duplicate collectors after navigate away / back)  
  Safe nav: `navigateTo` / `popFrom` / `navigateRootTo` — only when `isAdded` and current destination matches
  Collectors: `collectWhenStarted` / `collectWhenCreated` / `launchWhenResumed` on **viewLifecycleOwner**
- Activity helpers: `templates/base/ActivityExtensions.kt` → `…core.ui.extensions.ActivityExtensions.kt`
- Context helpers: `templates/base/ContextExtensions.kt` → `…core.ui.extensions.ContextExtensions.kt`  
  `context?.showToast("…")` / `context?.showToast(R.string.x)` — prefer `@StringRes` for user-facing copy
- Image helpers: `templates/base/ImageViewExtensions.kt` → `…core.ui.extensions.ImageViewExtensions.kt`  
  Always `siv.loadImage(source)` (Glide) for programmatic loads — never `setImageResource` / scattered `Glide.with` in UI
- State: `collectWhenStarted` / `repeatOnLifecycle(STARTED)`
- Effects: `collectWhenCreated` (or STARTED with SharedFlow that must not drop — follow project helper)
- Reuse extensions — do not copy-paste collectors
- Naming: `<Receiver>Extensions.kt` only — never a shared `FlowCollectionExtensions.kt` / `LifecycleFlowExtensions.kt`
- Prefer `navigateTo` / `popFrom` / `navigateRootTo` over raw `findNavController().navigate` / `popBackStack` in Fragments
- `ParentActivity.getInsetsMode()` defaults to **`TOP_BOTTOM`**; override per Activity, not per destination (`23-app-startup`)
- `ActivityExtensions.updateStatusBar(darkIcons)` toggles light/dark status-bar icons (`WindowInsetsControllerCompat`); `onBackPressedDispatcher { }` registers back handling (`StartupActivity` passes an empty block)
- Prefer `screenStarted() = launchWhenResumed { handleIntent(ScreenStarted) }` when start work must wait for resume (paywalls, etc.)

## Feature Fragment responsibilities

- Extend `BaseFragment` (or `ParentFragment` if no Base layer yet)
- `initObservers()` / collect state + effects
- Dispatch Intents only — no business logic
- Permissions via Effects → base helpers
- Navigate only in UI layer

## Feature Fragment member order (mandatory)

Keep class members in this order. Omit unused lifecycle overrides.

```kotlin
class FeatureFragment : BaseFragment<FragmentFeatureBinding>(FragmentFeatureBinding::inflate) {

    private val viewModel: FeatureViewModel by viewModel()
    // other properties / lazy adapters…

    override fun onViewCreated() {
        screenStarted()

        // Click listeners inline here — do NOT extract a setupClicks() / setupListeners() helper
        binding.mbAction.setOnClickListener { viewModel.handleIntent(FeatureIntent.ActionClicked) }
    }

    // override fun onStart() { … }   // only if needed
    // override fun onResume() { … }  // only if needed

    private fun screenStarted() {
        viewModel.handleIntent(FeatureIntent.ScreenStarted)
    }

    override fun initObservers() {
        collectWhenStarted(viewModel.state, ::renderState)
        collectWhenCreated(viewModel.effect, ::handleEffect)
    }

    private fun renderState(state: FeatureState) { /* bind UI */ }

    private fun handleEffect(effect: FeatureEffect) { /* navigate / one-shots */ }

    // override fun onPause() { … }        // only if needed
    // override fun onStop() { … }         // only if needed
    // override fun onDestroyView() { … }  // only if needed (unregister listeners, then super)
}
```

1. Properties / ViewModels / adapters
2. `onViewCreated()` — `screenStarted()` (or other start intents) then **inline** `setOnClickListener` → `handleIntent`; optional non-click UI helpers only when non-trivial
3. `onStart` / `onResume` — only when required
4. Private helper method implementations (`screenStarted`, camera/setup helpers, …) — **never** a dedicated `setupClicks()` / `setupListeners()` that only wires clicks
5. `initObservers()` — `collectWhenStarted` / `collectWhenCreated`
6. `renderState` then `handleEffect`
7. `onPause` / `onStop` / `onDestroyView` — only when required (`onDestroyView` last among them)

## Forbidden

- Holding View / Binding / Fragment in ViewModel
- `findViewById` / Data Binding
- Skipping Parent* for ad-hoc Binding lifecycle
- Accessing dialog/sheet binding after `onDestroyView`
- Hardcoding ads SDK into Parent* classes
- Collecting on Fragment `lifecycleScope` instead of `viewLifecycleOwner`
- Shared `FlowCollectionExtensions.kt` — use `FragmentExtensions` + `ActivityExtensions`
- `setImageResource` / raw `Glide.with` for dynamic/list/remote images — use `loadImage`
- Extracting click wiring into `setupClicks()` / `setupListeners()` — keep `setOnClickListener` inline in `onViewCreated()`
