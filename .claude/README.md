# OrbitalSonic AI Workflow — Claude Code (v1)

Company-grade **rules** + **skills** for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `module` / `lazyModule`).

This folder is part of the [AndroidPilot](https://github.com/orbitalsonic/AndroidPilot) template for Claude Code. Cursor teammates use [`.cursor/`](../.cursor/) (`/sonicc-…`). Gemini teammates use [`.agents/`](../.agents/) (`@sonicg-…`). Keep all three in sync. Skills invoke as `/sonic-…`.

## Rules vs Skills

|      | **Rules** (`.claude/rules/`)                   | **Skills** (`.claude/skills/`)                             |
|------|------------------------------------------------|------------------------------------------------------------|
| Role | Company law — invariants, naming, architecture | Multi-step playbooks (+ templates)                         |
| When | Always-on or path-matched while editing        | Invoked by name (`/` or agent pick) or trigger description |
| Size | Prefer clear invariants + BAD/GOOD             | Full workflows                                             |

**Long detail** lives under [`rules/reference/`](rules/reference/). `.md` rule files hold **short invariants + links** to those reference docs (so Agent context stays smaller). Each reference doc starts with `paths:` frontmatter scoped like its stub, so it loads only when matching files are touched — never always-on. Do not delete reference files — edit them when rules change.

**Commands:** Prefer skills with `/` invoke. Do not grow a large `.claude/commands/` tree unless needed.

## Skill names

- Invoke as **`/sonic-…`** (type `/sonic` with no space after — Claude Code matches the **folder** name)
- Description always starts with **OrbitalSonic Claude Skill:**
- Skill folders are flat: `.claude/skills/sonic-<name>/SKILL.md` (example: `sonic-figma-to-xml`)
- Cursor twins in this repo use **`/sonicc-…`** (description starts with **Sonic Cursor Skill:**)
- Gemini twins in this repo use **`@sonicg-…`** (description starts with **OrbitalSonic Gemini Skill:**)
- Do not pick Figma plugin skills (`figma:figma-design-to-code`, "Created by Figma") for Android screens — `/sonic-figma-to-xml` / `/sonic-figma-to-compose` load those internally

## Project settings

After `sonic-setup-new-project` / `sonic-setup-old-project` (or when joining an app), settings live in:

**[`.claude/project-settings.json`](project-settings.json)**

All feature/UI/test skills **must read and obey** this file when present:

| Key                      | Values                            | Meaning                                                                                                                                   |
|--------------------------|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `writeTestsWithFeatures` | `true` / `false`                  | Write unit/integration/E2E tests while scaffolding features                                                                               |
| `orientation`            | `portrait` / `landscape` / `both` | Which orientations layouts must support                                                                                                   |
| `themeModes`             | `day` / `night` / `both`          | Day / night / both theme resources                                                                                                        |
| `applicationId`          | string                            | Root package / applicationId                                                                                                              |
| `appName`                | string                            | Display name                                                                                                                              |
| `figmaDesignSystemUrl`   | Figma `/design/` URL or omit/`""` | Optional; Figma file for `sonic-setup-design-system`                                                                                        |
| `uiFramework`            | `xml` / `compose`                 | App UI stack. **xml** = View Binding + `:presentation`. **compose** = Jetpack Compose + `:feature-*` (AnimeHub). Default `xml` if missing |

## Skill map

Invoke as `/sonic-<name>`. Cursor twins use `/sonicc-…`. Gemini twins use `@sonicg-…`.

All skills live flat under `.claude/skills/sonic-<name>/SKILL.md`.

```
sonic-setup-new-project          Bootstrap multi-module app + persist settings (Firebase BOM + analytics/crashlytics/messaging + RC cache)
sonic-setup-old-project          Migrate existing production app to sonic-setup-new-project architecture; preserve product behavior; confirm settings first
sonic-setup-design-system        Figma design-system file → :core-ui tokens/themes (+ AppTheme in core/ui/theme/ when compose)
sonic-create-screen              Full screen: UI (sonic-figma-to-xml|compose) + sonic-create-mvi + sonic-create-clean-architecture when needed
sonic-create-mvi                 Presentation MVI only (no domain/data) — Fragment if xml, `*Screen` in `:feature-*` if compose
sonic-create-clean-architecture  Domain + data + core pieces as needed
sonic-figma-to-xml               XML layouts (+ Figma design-to-code); `uiFramework` xml only
sonic-figma-to-compose           Compose screens (AnimeHub `*Screen` / `*ScreenContent`); `uiFramework` compose
sonic-create-dialog              Dialog UI — XML if xml, Compose `*Dialog` if compose
sonic-create-bottom-sheet        Bottom sheet UI — XML if xml, Compose `*BottomSheet` if compose
sonic-create-custom-view         Custom View (xml) or reusable composable (compose)
sonic-localize                   Native string localization — one worker per language (`values-*` only)
sonic-review-architecture        Architecture / MVI / boundaries
sonic-review-performance         ANR / lists / dispatchers
sonic-review-security            Secrets / manifest / PII
sonic-review-cleanup             Unused code/resources / app size / release shrink
sonic-review-complete            Runs all review-* + summary report
sonic-test-unit                  Write+run JVM unit/Flow tests; consent before fix
sonic-test-integration           Write+run multi-layer tests; consent before fix
sonic-test-e2e                   Write+run E2E on device; consent before fix
sonic-test-complete              Full run + walkthrough; consent before fix
sonic-gradle-organize            Catalog + android/base/dependencies section order (signingConfigs, bundle, R8 optimization/keepRules)
sonic-gradle-update              Groovy→KTS if needed; AGP 9.3+; R8 optimization + keepRules; bump all deps (catalog + hardcodes)
sonic-build-debug-apk            Debug APK → device install + launch
sonic-build-release-apk          Release signing + APK → device install + launch
sonic-build-release-bundle       Release signing + AAB (no device)
sonic-implement-in-app-update    Play In-App Updates
sonic-implement-in-app-review    Play In-App Review
sonic-implement-firebase-messaging      firebase-messaging dep (:core-platform) only
sonic-implement-firebase-remote-config  First-time RC + SharedPref cache + StartupFragment fetch
sonic-add-firebase-remote-config        Add RC keys to existing SharedPref + Remote Config classes
sonic-implement-firebase-events         First-time full-app Analytics (screen_view + ui_click)
sonic-add-firebase-events               Add Analytics events for selected screens
sonic-implement-in-app-billing   Greenfield Play billing (subs + in-app, v4 stack)
sonic-update-in-app-billing      Migrate orbitalsonic SonicInApp v3 → v4.0.0
sonic-add-subscription-packages  Add subscription tiers to existing billing
sonic-add-inapp-packages         Add one-time in-app products to existing billing
sonic-implement-admob-ads        First-time AdMob screen wiring (:gmaAds from GitHub)
sonic-add-admob-banner           Add banner placement to existing :gmaAds
sonic-add-admob-interstitial     Add interstitial placement
sonic-add-admob-native           Add native placement
sonic-add-admob-rewarded         Add rewarded placement
sonic-add-admob-rewarded-interstitial   Add rewarded interstitial placement
sonic-add-admob-appOpen-Entrance Wire App Open ENTRANCE (splash/consent)
sonic-add-admob-appOpen-lifecycle       Wire App Open LIFECYCLE (resume)
```

### Typical feature flow

**Shortcut:** `/sonic-create-screen` runs steps 3–5 in one invoke (UI → optional domain/data → MVI). Prefer it when you would otherwise attach all three leaf skills.

1. `sonic-setup-new-project` (greenfield) or `sonic-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `sonic-setup-design-system` — Figma tokens/themes in `:core-ui` (Compose `AppTheme` in `core/ui/theme/` when compose)
3. `sonic-figma-to-xml` if `uiFramework` is `xml`; `sonic-figma-to-compose` if `compose` (or dialog / bottom-sheet)
4. `sonic-create-mvi` — Intent/State/Effect/VM + Fragment **or** `*Screen` in `:feature-*`
5. `sonic-create-clean-architecture` — when new domain/data is required

Data patterns (Retrofit, Room, SharedPreferences) live in **rules** + [`.claude/rules/reference/`](rules/reference/) — not separate skills.

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
| `21`–`25`              | Ads/billing (**ads are not MVI** — `:gmaAds` from GitHub; see `sonic-implement-admob-ads` / `sonic-add-admob-*` + `reference/ads-gma.md`), Firebase, startup, Figma assets, in-app update |
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

**Option A:** Dedicated template repo + sync script that copies a tagged `.claude/` (and `.cursor/` / `.agents/`) into each app. VERSION / CHANGELOG / sync scripts come in a later pass — do not invent local divergent rules; update the template instead.

## How teammates use this

1. Copy `.claude/` into the project root (this folder includes [`CLAUDE.md`](CLAUDE.md)).
2. Open Android Studio, terminal at the repo root, run `claude`.
3. Type `/` and pick a skill (e.g. `/sonic-create-screen`, `/sonic-figma-to-xml`, `/sonic-figma-to-compose`, `/sonic-create-mvi`).
4. Path-scoped rules load when matching files are touched; `00-global`, `14-security-secrets`, and `16-logging` always apply.
5. Before PRs: `/sonic-review-complete` or individual `sonic-review-*` skills.
