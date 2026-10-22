---
description: New feature / screen scaffolding checklist — use when adding a feature, hsg-create-screen, hsg-create-mvi, hsg-create-clean-architecture, or verifying a feature is complete
---

## New feature checklist

- [ ] Feature package: `{di,intent,state,effect,viewModel,ui}` (+ adapter/mapper as needed) — **compose:** `:feature-<kebab>` with `*Screen.kt` at feature root instead of `ui/` Fragment
- [ ] Extend `Parent*` / `Base*` UI bases (xml) **or** `*Screen` / `*ScreenContent` (`28-compose-ui`) when `uiFramework` is compose; layouts / composables work in **portrait and landscape**
- [ ] Fragment member order per `19-base-ui` when xml: `onViewCreated` (`setup*` helpers then `screenStarted` — often `launchWhenResumed`; inline clicks, no `setupClicks`) → helpers → `initObservers` → `renderState` → `handleEffect` → teardown (if any). Compose: `*Screen` collects state/effects → private `*ScreenContent` renders
- [ ] Icon actions use `ButtonStyle.IconButton` (`mb` + `app:icon`, style padding `8dp` / `iconTint` `?attr/colorIcon`) — not clickable `siv`
- [ ] Domain UseCase(s) + repository **interface** in `:domain` (never under `:data`)
- [ ] Data repository impl + DataSource if needed (heavy work / DTO mapping off Main); Room → `:core-database`, Retrofit → `:core-network` when adding those stacks
- [ ] Mapping: Repo/UseCase for heavy work; `toUi()` in ViewModel if needed — not in UI classes
- [ ] DI: **`lazyModule` / `lazyModules` only** with `//// Section` headers; theme/billing after `runOnKoinStarted` (never `GlobalContext` probes); UseCases in domain `useCaseModule`; new module listed in `KoinModules`
- [ ] If feature is **cold-start / Entrance / Splash / MainActivity inject**: verify full ctor inject graph (VM → UseCases → repos/managers) is registered before merge (`23`, `07`, `hsg-review-architecture`)
- [ ] Strings in the **single** shared strings file (+ translations); layouts in `:presentation` only (xml)
- [ ] Logs use `Constants.TAG*` format; prefer Repository; ViewModel sparse (`handleError` for failures)
- [ ] Permissions via Intent → Effect → base permission helper when needed
- [ ] Analytics: `screen_view` from the screen (`onStart` / compose entry) and `ui_click` on taps, via `EventsProvider` + `PlatformFirebase`
- [ ] R8 keep rules in `src/main/keepRules/*.keep` cover new `state`/`intent`/`effect`/`model` types
- [ ] Unit tests for UseCase / ViewModel
- [ ] Verify no `:presentation` → `:data` dependency
- [ ] Large-list path: xml `ListAdapter` + DiffUtil / compose `LazyColumn`/`LazyVerticalGrid` + stable keys; `layoutManager` in XML unless dynamic (xml only)
- [ ] Images: xml `ShapeableImageView` + Glide `loadImage`; compose Coil `AsyncImage` / `painterResource`
- [ ] If ads/premium: gate on entitlement + RC; use existing ads extensions — not raw SDK in Fragments. **Do not** wrap ads in MVI unless the user explicitly asks (`21-ads-billing`). Paywalls: sticky footer + 3s delayed close (`33-screen-premium`)
- [ ] Funnel / Dashboard: obey `29`–`32` + `reference/app-flow.md` when touching those screens
- [ ] Obey `.agents/project-settings.json` (tests / orientation / theme modes / **`uiFramework`**)
- [ ] Full screen scaffold via `hsg-create-screen` (UI + MVI + optional domain/data); or leaf skills: presentation via `hsg-create-mvi`, new domain/data via `hsg-create-clean-architecture` (feature screens only — not ads)
