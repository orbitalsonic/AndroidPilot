# OrbitalSonic AI Workflow — Cursor (v1)

Company-grade **rules** + **skills** for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `module` / `lazyModule`).

This folder is part of the [AndroidPilot](https://github.com/orbitalsonic/AndroidPilot) template. Share with the team by copying `.cursor/` into a project. Claude Code teammates use [`.claude/`](../.claude/). Gemini teammates use [`.agents/`](../.agents/). Keep all three in sync.

## Rules vs Skills

|      | **Rules** (`.cursor/rules/`)                   | **Skills** (`.cursor/skills/`)                             |
|------|------------------------------------------------|------------------------------------------------------------|
| Role | Company law — invariants, naming, architecture | Multi-step playbooks (+ templates)                         |
| When | Always-on or glob-matched while editing        | Invoked by name (`/` or agent pick) or trigger description |
| Size | Prefer clear invariants + BAD/GOOD             | Full workflows                                             |

**Long detail** lives under [`rules/reference/`](rules/reference/). `.mdc` rule files hold **short invariants + links** to those reference docs (so Agent context stays smaller). Do not delete reference files — edit them when rules change.

**Commands:** Prefer skills with `/` invoke. Do not grow a large `.cursor/commands/` tree unless needed.

## Sonic skill names

- Invoke as **`/sonicc-…`** (type `/sonicc` with no space after — Cursor matches the **folder** name)
- Description always starts with **Sonic Cursor Skill:**
- Skill folders are also `sonicc-*` (example: `ui/sonicc-figma-to-xml`)
- Claude Code twins in this repo use **`/sonic-…`**
- Gemini twins in this repo use **`@sonicg-…`** (description starts with **OrbitalSonic Gemini Skill:**)
- Do not pick Figma plugin skills (`figma:design-to-code`, "Created by Figma") for Android screens — `/sonicc-figma-to-xml` / `/sonicc-figma-to-compose` load those internally

## Project settings

After `sonicc-setup-new-project` / `sonicc-setup-old-project` (or when joining an app), settings live in:

**[`.cursor/project-settings.json`](project-settings.json)**

All feature/UI/test skills **must read and obey** this file when present:

| Key                      | Values                            | Meaning                                                                                                                                   |
|--------------------------|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `writeTestsWithFeatures` | `true` / `false`                  | Write unit/integration/E2E tests while scaffolding features                                                                               |
| `orientation`            | `portrait` / `landscape` / `both` | Which orientations layouts must support                                                                                                   |
| `themeModes`             | `day` / `night` / `both`          | Day / night / both theme resources                                                                                                        |
| `applicationId`          | string                            | Root package / applicationId                                                                                                              |
| `appName`                | string                            | Display name                                                                                                                              |
| `figmaDesignSystemUrl`   | Figma `/design/` URL or omit/`""` | Optional; Figma file for `sonicc-setup-design-system`                                                                                            |
| `uiFramework`            | `xml` / `compose`                 | App UI stack. **xml** = View Binding + `:presentation`. **compose** = Jetpack Compose + `:feature-*` (AnimeHub). Default `xml` if missing |

## Skill map

```
project/sonicc-setup-new-project          Bootstrap multi-module app + persist settings (Firebase BOM + analytics/crashlytics/messaging + RC cache)
project/sonicc-setup-old-project          Migrate existing production app to sonicc-setup-new-project architecture; preserve product behavior; confirm settings first
project/sonicc-setup-design-system        Figma design-system file → :core-ui tokens/themes (+ AppTheme in core/ui/theme/ when compose)
feature/sonicc-create-screen              Full screen: UI (sonicc-figma-to-xml|compose) + sonicc-create-mvi + sonicc-create-clean-architecture when needed
feature/sonicc-create-mvi                 Presentation MVI only (no domain/data) — Fragment if xml, `*Screen` in `:feature-*` if compose
feature/sonicc-create-clean-architecture  Domain + data + core pieces as needed
ui/sonicc-figma-to-xml                    XML layouts (+ Figma design-to-code); `uiFramework` xml only
ui/sonicc-figma-to-compose                Compose screens (AnimeHub `*Screen` / `*ScreenContent`); `uiFramework` compose
ui/sonicc-create-dialog                   Dialog UI — XML if xml, Compose `*Dialog` if compose
ui/sonicc-create-bottom-sheet             Bottom sheet UI — XML if xml, Compose `*BottomSheet` if compose
ui/sonicc-create-custom-view              Custom View (xml) or reusable composable (compose)
i18n/sonicc-localize                      Native string localization — one worker per language (`values-*` only)
review/sonicc-review-architecture         Architecture / MVI / boundaries
review/sonicc-review-performance          ANR / lists / dispatchers
review/sonicc-review-security             Secrets / manifest / PII
review/sonicc-review-cleanup              Unused code/resources / app size / release shrink
review/sonicc-review-complete             Runs all review-* + summary report
test/sonicc-test-unit                     Write+run JVM unit/Flow tests; consent before fix
test/sonicc-test-integration              Write+run multi-layer tests; consent before fix
test/sonicc-test-e2e                      Write+run E2E on device; consent before fix
test/sonicc-test-complete                 Full run + walkthrough; consent before fix
gradle/sonicc-gradle-organize             Catalog + android/base/dependencies section order (signingConfigs, bundle, R8 optimization/keepRules)
gradle/sonicc-gradle-update               Groovy→KTS if needed; AGP 9.3+; R8 optimization + keepRules; bump all deps (catalog + hardcodes)
build/sonicc-build-debug-apk              Debug APK → device install + launch
build/sonicc-build-release-apk           Release signing + APK → device install + launch
build/sonicc-build-release-bundle        Release signing + AAB (no device)
platform/sonicc-implement-in-app-update   Play In-App Updates
platform/sonicc-implement-in-app-review   Play In-App Review
platform/sonicc-implement-firebase-messaging  firebase-messaging dep (:core-platform) only
firebase/sonicc-implement-firebase-messaging  firebase-messaging dep (:core-platform) only
firebase/sonicc-implement-firebase-remote-config  First-time RC + SharedPref cache + Startup fetch
firebase/sonicc-add-firebase-remote-config  Add RC keys to existing SharedPref + Remote Config classes
firebase/sonicc-implement-firebase-events  First-time full-app Analytics (screen_view + ui_click)
firebase/sonicc-add-firebase-events       Add Analytics events for selected screens
premium/sonicc-implement-in-app-billing   Greenfield Play billing (subs + in-app, v4 stack)
billing/sonicc-update-in-app-billing      Migrate orbitalsonic SonicInApp v3 → v4.0.0
premium/sonicc-add-subscription-packages  Add subscription tiers to existing billing
premium/sonicc-add-inapp-packages         Add one-time in-app products to existing billing
admob/sonicc-implement-admob-ads          First-time AdMob screen wiring (:gmaAds from GitHub)
admob/sonicc-add-admob-banner             Add banner placement to existing :gmaAds
admob/sonicc-add-admob-interstitial       Add interstitial placement
admob/sonicc-add-admob-native             Add native placement
admob/sonicc-add-admob-rewarded           Add rewarded placement
admob/sonicc-add-admob-rewarded-interstitial  Add rewarded interstitial placement
admob/sonicc-add-admob-appOpen-Entrance   Wire App Open ENTRANCE (splash/consent)
admob/sonicc-add-admob-appOpen-lifecycle  Wire App Open LIFECYCLE (resume)
release/pre-release                Ship checklist
```

### Typical feature flow

**Shortcut:** `/sonicc-create-screen` runs steps 3–5 in one invoke (UI → optional domain/data → MVI). Prefer it when you would otherwise attach all three leaf skills.

1. `sonicc-setup-new-project` (greenfield) or `sonicc-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `sonicc-setup-design-system` — Figma tokens/themes in `:core-ui` (Compose `AppTheme` in `core/ui/theme/` when compose)
3. `sonicc-figma-to-xml` if `uiFramework` is `xml`; `sonicc-figma-to-compose` if `compose` (or dialog / bottom-sheet)
4. `sonicc-create-mvi` — Intent/State/Effect/VM + Fragment **or** `*Screen` in `:feature-*`
5. `sonicc-create-clean-architecture` — when new domain/data is required

Data patterns (Retrofit, Room, SharedPreferences) live in **rules** + [`.cursor/rules/reference/`](rules/reference/) — not separate skills.

## Rules index (`00`–`33`)

| File                   | Role                                                                                                                                                                 |
|------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `00-global`            | Always-on stack + Always/Never law                                                                                                                                   |
| `01-feature-checklist` | Feature scaffolding checklist (not always-on)                                                                                                                        |
| `02`–`03`              | Modules + Clean Architecture (+ SOLID in `03`)                                                                                                                       |
| `04`–`07`              | MVI, Kotlin, coroutines, DI (`04`/`07` → `reference/`)                                                                                                               |
| `08`–`10`              | Gradle (`08` → `reference/gradle.md`: section order, signingConfigs, bundle, `base`, R8 `optimization` / `keepRules`), resources/XML, manifest (`09` → `reference/`) |
| `11`–`13`              | Testing, naming, libraries                                                                                                                                           |
| `14`–`16`              | Security (always), compatibility, logging (always)                                                                                                                   |
| `17`–`20`              | Nav (`nav_graph_startup` + `nav_graph_main`, optional nested dashboard / `navigateRootTo`), errors, base UI (`19` → `reference/`: `InsetsMode` edge-to-edge), permissions |
| `21`–`25`              | Ads/billing (**ads are not MVI** — `:gmaAds` from GitHub; see `admob/*` skills + `reference/ads-gma.md`), Firebase, startup (`StartupActivity` + `MainActivity`), Figma assets, in-app update             |
| `26-data-persistence`  | Retrofit / Room / SharedPreferences (`:core-network` / `:core-database` when added)                                                                                  |
| `27-in-app-review`     | Play In-App Review placement (`InAppReviewManager`)                                                                                                                  |
| `28-compose-ui`        | Compose feature modules / Screen-Content / NavGraph (`uiFramework` compose) — [reference/compose-ui.md](rules/reference/compose-ui.md)                               |
| `29`–`33`              | Screen rules: Startup funnel (`StartupActivity`, `Startup*` screens), Language (startup vs in-app), Onboarding (`StartupIntroFragment`), MainActivity shell / bottom nav, Premium (startup vs in-app, sticky footer + 3s close) |

### `rules/reference/` (full detail)

| File                                                | Backed by rule            |
|-----------------------------------------------------|---------------------------|
| `resources-xml.md`                                  | `09-resources-xml`        |
| `dependency-injection.md`                           | `07-dependency-injection` |
| `gradle.md`                                         | `08-gradle`               |
| `mvi-presentation.md`                               | `04-mvi-presentation`     |
| `base-ui.md`                                        | `19-base-ui`              |
| `compose-ui.md`                                     | `28-compose-ui`           |
| `retrofit.md` / `room.md` / `shared-preferences.md` | `26-data-persistence`     |
| `premium-billing.md`                                | `21-ads-billing` / `33`   |
| `ads-gma.md`                                        | `21-ads-billing`          |
| `app-flow.md`                                       | `29`–`32` screen rules    |
| `languages.md`                                      | `30-screen-language` + localize skill (104-language catalog, `values-*` map, RTL) |

## Future distribution (not in v1)

**Option A:** Dedicated template repo + sync script that copies a tagged `.cursor/` (and `.claude/` / `.agents/`) into each app. VERSION / CHANGELOG / sync scripts come in a later pass — do not invent local divergent rules; update the template instead.

## How teammates use this

1. Open project with this `.cursor/` present.
2. Type `/` and pick a skill (e.g. `sonicc-create-screen`, `sonicc-figma-to-xml` / `sonicc-figma-to-compose`, `sonicc-create-mvi`).
3. Obey rules automatically while editing matching files.
4. Before PRs: `/sonicc-review-complete` or individual `sonicc-review-*` skills.
