---
name: sonic-review-architecture
description: Sonic Cursor Skill: Review Android changes against Clean Architecture, MVI, module boundaries, and project rules. Use when reviewing PRs, refactors, or asking if code follows architecture standards. Prefer sonic-review-complete for a full multi-check pass.
---

# Architecture Review

Read `.cursor/rules/` and apply systematically. Output a structured report.

Obey `.cursor/project-settings.json` when judging orientation / tests.

## Checklist

### Module boundaries

- [ ] `presentation` / `:feature-*` does not import `:data`
- [ ] `domain` has no Android UI / presentation imports
- [ ] UseCases + repository **interfaces** only in `:domain` — never in `:data`
- [ ] No circular module dependencies
- [ ] Koin uses **`module` (cold-start) and/or `lazyModule` (deferred)** per `07` — not “lazy everywhere”; Entrance deps must be eager `module`
- [ ] Theme applied after `startKoin` in Application; no `GlobalContext.getOrNull()` gates; `startKoin` never in Activity
- [ ] Each `module` / `lazyModule` has readable `//// Section` headers (SoC: DataSources / Repositories / ViewModels / area UseCases, etc.)
- [ ] `dataModule` ordered: `//// DataSources` then `//// Repositories`
- [ ] UseCase factories in domain `useCaseModule` (grouped by area)
- [ ] New modules registered in composition root (eager or lazy list; sectioned: Core / Data / Domain / Presentation or Feature / Ads)
- [ ] **Cold-start Koin graph (Critical):** walk first UI resolve path — `App` `startKoin` + `modules(eager)` (+ optional `lazyModules`) → **eager** list includes every module those screens need → **MainActivity** (if it injects) / start destination (**EntranceFragment** / **EntranceScreen** / legacy **Splash***) ViewModel → **every constructor dependency** (`UseCase`, repo, manager, dispatcher, …) has a matching `viewModel` / `factory` / `single` in a **loaded eager `module`**. Missing def → **Fail** (`No definition found` / `Could not create instance`)
- [ ] Resolving **lazyModule** bindings at start uses `runOnKoinStarted` / wait APIs (`23-app-startup`); eager cold-start injects are fine right after `startKoin` returns

### MVI

- [ ] Intent / State / Effect / ViewModel pattern on **feature screens** — skip `:gmaAds` / existing ad ViewModels / ads managers (not MVI unless the user asked to convert)
- [ ] `handleIntent` single launch + `suspend` `onX` handlers; `handleError` at end of ViewModel
- [ ] Navigation via Effects — not NavController in ViewModel
- [ ] No mutable state exposed publicly
- [ ] Fragments (xml) or `*Screen` (compose) render + dispatch intents only
- [ ] **xml:** Fragment member order (`19-base-ui`): `onViewCreated` (inline clicks, no `setupClicks`) → helpers → `initObservers` → `renderState` → `handleEffect` → teardown
- [ ] **xml:** Collectors use `viewLifecycleOwner` (`FragmentExtensions`); nav via `navigateTo` / `popFrom`
- [ ] **compose:** `*Screen` + private `*ScreenContent`; `collectAsStateWithLifecycle` + `LaunchedEffect` for effects; no `NavController` in the feature (`28-compose-ui`)
- [ ] ViewModel logs sparse — repo primary; failures via `handleError`

### Mapping

- [ ] Heavy mapping in Repo / UseCase — not Fragment/Adapter/`*ScreenContent`
- [ ] `toUi()` in ViewModel only when needed, with dispatcher for large lists
- [ ] Adapters bind `*UiItem` only

### Threading / ANR (`06-coroutines-flow`, `sonic-review-performance`)

- [ ] No disk/network/heavy map on Main
- [ ] Injected dispatchers where project uses them
- [ ] Large lists: ListAdapter + DiffUtil (xml) or Lazy list + keys (compose)

### UI (`09-resources-xml`, `19-base-ui`, `28-compose-ui`)

- [ ] **xml:** View Binding only — no findViewById / Data Binding / Compose feature screens
- [ ] **compose:** `*Screen` / `*ScreenContent`; no `fragment_*` layouts; no `GlobalContext.get()` in MainActivity
- [ ] Material widgets; portrait + landscape (per project settings)
- [ ] Clickable icons → `ButtonStyle.IconButton` (`mb`, `app:icon`) — not clickable `siv`
- [ ] `MaterialButton` solid+stroke → tint/stroke/`cornerRadius` — not `bg_shape_*` + `background` override
- [ ] Filled/text `MaterialButton` → `wrap_content` height — no fixed height + inset 0dp hacks
- [ ] Clickable language/chip selectors → `MaterialButton` + Material bg + end `app:icon` — not MTV + `bg_shape_*` / `drawableEnd`
- [ ] **xml:** Programmatic images via Glide `siv.loadImage(...)` — not `setImageResource` / raw Glide in adapters. **compose:** Coil `AsyncImage`
- [ ] Strings in single `:core-ui` file
- [ ] **xml:** Static `layoutManager` / orientations / `spanCount` in XML — not Kotlin unless dynamic. **compose:** `LazyColumn` / `LazyRow` / `LazyVerticalGrid` with keys

### Security / logging (`14-security-secrets`, `16-logging`)

- [ ] No secrets in code/commits
- [ ] `Constants.TAG*` log format
- [ ] No PII in logs

### Errors (`18-errors-result`)

- [ ] Typed failures — not raw exceptions in State
- [ ] CancellationException handled correctly

### Data patterns (`26-data-persistence`)

- [ ] Retrofit/Room/prefs follow reference patterns when present
- [ ] No DataSource dispatchers; repository owns `withContext`

## Report format

Number every actionable finding. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Summary

One-line verdict: Pass / Pass with notes / Fail

## Fix list

1. [Critical] …
2. [Warning] …
3. [Suggestion] …

## Rules referenced

- rule files that applied
```

Severity: boundary violations, Main-thread heavy work, and **cold-start missing Koin definitions** (Entrance/Splash/MainActivity inject graph) = **Critical**.

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
