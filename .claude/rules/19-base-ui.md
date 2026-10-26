---
description: Base UI classes (ParentActivity InsetsMode edge-to-edge, ParentFragment hooks), ViewBinding lifecycle, and Flow collection
paths:
  - "**/ui/**/*.kt"
  - "**/base/**/*.kt"
  - "**/presentation/**/*.kt"
  - "**/feature*/**/*.kt"
  - "**/core/ui/**/*.kt"
---

# Base UI (invariants)

**Full detail (Parent*/Base* API, Fragment member order template):** [reference/base-ui.md](reference/base-ui.md)

Applies to XML `Parent*` / `Base*` when `uiFramework` is `xml`. Compose screens follow `28-compose-ui` instead of Fragment member order.

## Must follow

- Hierarchy: `:core-ui` Parent* (Activity/Fragment/Dialog/Sheet) → optional `:presentation` Base* → Feature
- Templates: `.claude/skills/sonic-setup-new-project/templates/base/` (+ that folder’s README)
- `ParentActivity` edge-to-edge via **`InsetsMode` enum** (`EDGE_TO_EDGE` / `TOP` / `BOTTOM` / `TOP_BOTTOM`): `onCreate` = `initSplashScreen()` → `super.onCreate` → `enableEdgeToEdge()` + `setContentView` + insets listener for `getInsetsMode()` (IME-aware bottom) → `onCreated()` → `initObservers()`. Default mode `TOP_BOTTOM`; override `getInsetsMode()` per Activity — **no** `includeTopPadding` / per-destination padding flags
- `initSplashScreen()` overridden **only** in `StartupActivity` (`installSplashScreen()`); `MainActivity` keeps it empty (`23-app-startup`)
- `ParentFragment`: `onCreateView` inflates then calls `onViewInflated()` (optional early hook); `onViewCreated` → `initObservers()` → `onViewCreated(Bundle?)` → abstract `onViewCreated()`; clear `_binding` in `onDestroyView`; ParentSheet **null-safe** (no `!!`)
- Theme: apply after Koin via `runOnKoinStarted` in Application — no `GlobalContext` probes (`07`, `23`)
- Collect on **`viewLifecycleOwner`** via `FragmentExtensions`; nav via `navigateTo` / `popFrom` / `navigateRootTo`; toasts via `ContextExtensions`; images via `loadImage`; status-bar icon tint via `ActivityExtensions.updateStatusBar(darkIcons)`
- Naming: `<Receiver>Extensions.kt` only — never shared `FlowCollectionExtensions.kt`
- Feature Fragment order: properties → `onViewCreated` (`setup*` helpers then `screenStarted` — **inline** clicks, no `setupClicks`) → helpers → `initObservers` → `renderState` → `handleEffect` → teardown
- Common helpers: `setupRecyclerView`, `setupViewPager`, `setupBottomNavigation`, `screenStarted` (`launchWhenResumed { handleIntent(ScreenStarted) }`), `startInAppUpdate`
- Forbidden: Binding in ViewModel; `findViewById` / Data Binding; ads SDK in Parent*; collectors on Fragment `lifecycle` instead of `viewLifecycleOwner`; `window.statusBarColor` / nav-bar colors unless the user asks

Read [reference/base-ui.md](reference/base-ui.md) when changing base UI or feature Fragments.
