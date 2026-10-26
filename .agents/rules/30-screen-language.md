---
description: Language screens — startup StartupLanguageFragment (Next only) vs in-app LanguageFragment (Apply + search)
paths:
  - "**/language/**"
  - "**/LanguageFragment*"
  - "**/LanguageScreen*"
  - "**/StartupLanguage*"
  - "**/InAppLanguage*"
---

# Language screens (two)

There are **two** language screens with different hosts, classes and layouts. Never merge them into one Fragment with flags.

| | **Startup** (first-time funnel) | **In-app** (Settings / menu) |
|---|---|---|
| Host | `StartupActivity` · `nav_graph_startup` | `MainActivity` · `nav_graph_main` |
| Class / layout | `StartupLanguageFragment` · `fragment_startup_language.xml` | `LanguageFragment` · `fragment_language.xml` |
| Package | `presentation/startup/…` | `presentation/language/…` |
| Toolbar | Title + **Next / Continue** text button (`mbContinueStartupLanguage`) at the end. **No back button** | Back (`mbBackLanguage`) + title + **Apply** (`mbApplyLanguage`) |
| Search | **None** | `TextInputLayout` / `TextInputEditText` (`tietSearchLanguage`) filtering the list + “nothing found” state (`mtvNothingFoundLanguage`) |
| Continue visibility | Hidden behind a small progress (`cpiContinueStartupLanguage`) until the native ad loads **or** a 4 s timer ends — then show Next | Apply always visible |
| On select | Highlight row; keep selection in State | Highlight row; keep selection in State (do **not** apply yet) |
| On action | Next → persist locale, `isLanguageCompleted = true`, apply locale, interstitial (if wired) → Startup Premium (first-time RC) or Startup Intro | Apply → if changed: persist + apply locale (Activity recreates); else `popFrom(R.id.languageFragment)` |
| Back press | Blocked (`StartupActivity`) | Normal pop |
| Ads (if wired) | Native (bottom container) + splash interstitial on Next | Banner (bottom) |
| Analytics | `startup_language_screen` | `language_screen` |

Shared pieces live once: `LanguageAdapter` (`ListAdapter` + DiffUtil), `LanguageUiItem`, `LanguageDataSource` (`:data`; legacy / reference apps name it **`DpLanguages`** with `getLanguagesList(languageCode)`), `GetLanguagesUseCase` / `SaveLanguageUseCase` (`:domain`). Only the Fragments, layouts and ViewModels are duplicated.

**Language list = catalog.** The supported languages (code, English name, `English (native)` display name, `flag_*` drawable, Android `values-*` folder, RTL) are fixed in [reference/languages.md](reference/languages.md) — 104 languages, English first. `DpLanguages` / `LanguageDataSource` builds its `LanguageItem` rows from that catalog only; a product may ship a subset. New language → add the catalog row first, then the data source row, then run the localize skill (`sonicc-localize` / `sonic-localize` / `sonicg-localize`) for its folder.

## Behavior

- Apply locale like the demo (AppCompat `setApplicationLocales` + prefs); `AppLocalesMetadataHolderService` in manifest for API ≤ 32
- When implementing a design that pre-selects a language, **ask** whether English should be selected by default
- Startup: Continue in the toolbar (not a sticky footer) unless Figma says otherwise; in-app: list fills between search and banner
- Search: filter on `doAfterTextChanged` → Intent `SearchChanged(query)`; filtering in ViewModel/UseCase, never in the Fragment
- Rows: flag + display name come from the data source list (catalog). Display name is `English (native)` by design and is **not** translated (`translatable="false"` if ever moved to resources); search matches English name and native name
- Saved locale = the catalog `languageCode` (`zh-CN`, `ceb`, `jv`, …). Convert to the Android folder / `Locale` only at the apply step — never rewrite the stored code

## UI helpers

```kotlin
setupRecyclerView()
screenStarted() // launchWhenResumed { handleIntent(ScreenStarted) } when appropriate
```

- List: `layoutManager` / orientation in XML; `ListAdapter` + DiffUtil
- Next / Apply: `MaterialButton` (`ButtonStyle.*`); enable when a language is selected (unless product says otherwise)

## Forbidden

- One `LanguageFragment` serving both hosts via nav args / flags
- Search field on the startup language screen
- Back button on the startup language screen
- Hardcoded language display names in Fragments / ViewModels / `*ScreenContent` — they come from the data source list (`reference/languages.md`); use `strings.xml` (`translatable="false"`) only if the product moves them to resources
- A language code that is not in `reference/languages.md` (ask, add the catalog row, then the data source)
- Two `values-*` folders for one language (`values-he` and `values-iw`, `values-id` and `values-in`) — see the catalog folder rules
- Setting `layoutManager` in Kotlin for a fixed vertical list
