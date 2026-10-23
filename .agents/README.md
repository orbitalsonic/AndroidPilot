# OrbitalSonic AI Workflow — Gemini (v1)

Company-grade **rules** + **skills** for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `module` / `lazyModule`).

This folder is part of the [AndroidPilot](https://github.com/orbitalsonic/AndroidPilot) template for Android Studio Gemini. Cursor teammates use [`.cursor/`](../.cursor/) (`/sonic-…`). Claude Code teammates use [`.claude/`](../.claude/) (`/sonicc-…`). Keep all three in sync. Skills invoke as `@sonicg-…`.

## Rules vs Skills

|      | **Rules** (`.agents/rules/`)                   | **Skills** (`.agents/skills/`)                             |
|------|------------------------------------------------|------------------------------------------------------------|
| Role | Company law — invariants, naming, architecture | Multi-step playbooks (+ templates)                         |
| When | Always-on or path-matched while editing        | Invoked by name (`@` or agent pick) or trigger description |
| Size | Prefer clear invariants + BAD/GOOD             | Full workflows                                             |

**Long detail** lives under [`rules/reference/`](rules/reference/). `.md` rule files hold **short invariants + links** to those reference docs (so Agent context stays smaller). Do not delete reference files — edit them when rules change.

**Commands:** Prefer skills with `@` invoke. Do not grow a large `.agents/commands/` tree unless needed.

## Skill names

- Invoke as **`@sonicg-…`** (type `@sonicg` — Android Studio matches the **folder** name under `.agents/skills`, not `.skills` or `.gemini`)
- Description always starts with **OrbitalSonic Gemini Skill:**
- Skill folders are flat: `.agents/skills/sonicg-<name>/SKILL.md` (example: `sonicg-figma-to-xml`)
- Cursor twins in this repo use **`/sonic-…`** (description starts with **Sonic Cursor Skill:**)
- Claude Code twins in this repo use **`/sonicc-…`** (description starts with **OrbitalSonic Claude Skill:**)
- Do not pick Figma plugin skills ("Created by Figma") for Android screens — `@sonicg-figma-to-xml` / `@sonicg-figma-to-compose` load those internally

## Project settings

After `sonicg-setup-new-project` / `sonicg-setup-old-project` (or when joining an app), settings live in:

**[`.agents/project-settings.json`](project-settings.json)**

All feature/UI/test skills **must read and obey** this file when present:

| Key                      | Values                            | Meaning                                                                                                                                   |
|--------------------------|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `writeTestsWithFeatures` | `true` / `false`                  | Write unit/integration/E2E tests while scaffolding features                                                                               |
| `orientation`            | `portrait` / `landscape` / `both` | Which orientations layouts must support                                                                                                   |
| `themeModes`             | `day` / `night` / `both`          | Day / night / both theme resources                                                                                                        |
| `applicationId`          | string                            | Root package / applicationId                                                                                                              |
| `appName`                | string                            | Display name                                                                                                                              |
| `figmaDesignSystemUrl`   | Figma `/design/` URL or omit/`""` | Optional; Figma file for `sonicg-setup-design-system`                                                                                        |
| `uiFramework`            | `xml` / `compose`                 | App UI stack. **xml** = View Binding + `:presentation`. **compose** = Jetpack Compose + `:feature-*` (AnimeHub). Default `xml` if missing |

## Skill map

Invoke as `@sonicg-<name>`. Cursor twins use `/sonic-…`. Claude Code twins use `/sonicc-…`.

All skills live flat under `.agents/skills/sonicg-<name>/SKILL.md`.

```
sonicg-setup-new-project          Bootstrap multi-module app + persist settings (Firebase BOM + analytics/crashlytics/messaging + RC cache)
sonicg-setup-old-project          Migrate existing production app to sonicg-setup-new-project architecture; preserve product behavior; confirm settings first
sonicg-setup-design-system        Figma design-system file → :core-ui tokens/themes (+ AppTheme in core/ui/theme/ when compose)
sonicg-create-screen              Full screen: UI (sonicg-figma-to-xml|compose) + sonicg-create-mvi + sonicg-create-clean-architecture when needed
sonicg-create-mvi                 Presentation MVI only (no domain/data) — Fragment if xml, `*Screen` in `:feature-*` if compose
sonicg-create-clean-architecture  Domain + data + core pieces as needed
sonicg-figma-to-xml               XML layouts (+ Figma design-to-code); `uiFramework` xml only
sonicg-figma-to-compose           Compose screens (AnimeHub `*Screen` / `*ScreenContent`); `uiFramework` compose
sonicg-create-dialog              Dialog UI — XML if xml, Compose `*Dialog` if compose
sonicg-create-bottom-sheet        Bottom sheet UI — XML if xml, Compose `*BottomSheet` if compose
sonicg-create-custom-view         Custom View (xml) or reusable composable (compose)
sonicg-localize                   Native string localization — one worker per language (`values-*` only)
sonicg-review-architecture        Architecture / MVI / boundaries
sonicg-review-performance         ANR / lists / dispatchers
sonicg-review-security            Secrets / manifest / PII
sonicg-review-cleanup             Unused code/resources / app size / release shrink
sonicg-review-complete            Runs all review-* + summary report
sonicg-test-unit                  Write+run JVM unit/Flow tests; consent before fix
sonicg-test-integration           Write+run multi-layer tests; consent before fix
sonicg-test-e2e                   Write+run E2E on device; consent before fix
sonicg-test-complete              Full run + walkthrough; consent before fix
sonicg-gradle-organize            Catalog + android/base/dependencies section order (signingConfigs, bundle, R8 optimization/keepRules)
sonicg-gradle-update              Groovy→KTS if needed; AGP 9.3+; R8 optimization + keepRules; bump all deps (catalog + hardcodes)
sonicg-build-debug-apk            Debug APK → device install + launch
sonicg-build-release-apk          Release signing + APK → device install + launch
sonicg-build-release-bundle       Release signing + AAB (no device)
sonicg-implement-in-app-update    Play In-App Updates
sonicg-implement-in-app-review    Play In-App Review
sonicg-implement-firebase-messaging      firebase-messaging dep (:core-platform) only
sonicg-implement-firebase-remote-config  First-time RC + SharedPref cache + StartupFragment fetch
sonicg-add-firebase-remote-config        Add RC keys to existing SharedPref + Remote Config classes
sonicg-implement-firebase-events         First-time full-app Analytics (screen_view + ui_click)
sonicg-add-firebase-events               Add Analytics events for selected screens
sonicg-implement-in-app-billing   Greenfield Play billing (subs + in-app, v4 stack)
sonicg-update-in-app-billing      Migrate orbitalsonic SonicInApp v3 → v4.0.0
sonicg-add-subscription-packages  Add subscription tiers to existing billing
sonicg-add-inapp-packages         Add one-time in-app products to existing billing
sonicg-implement-admob-ads        First-time AdMob screen wiring (:gmaAds from GitHub)
sonicg-add-admob-banner           Add banner placement to existing :gmaAds
sonicg-add-admob-interstitial     Add interstitial placement
sonicg-add-admob-native           Add native placement
sonicg-add-admob-rewarded         Add rewarded placement
sonicg-add-admob-rewarded-interstitial   Add rewarded interstitial placement
sonicg-add-admob-appOpen-Entrance Wire App Open ENTRANCE (splash/consent)
sonicg-add-admob-appOpen-lifecycle       Wire App Open LIFECYCLE (resume)
```

### Typical feature flow

**Shortcut:** `@sonicg-create-screen` runs steps 3–5 in one invoke (UI → optional domain/data → MVI). Prefer it when you would otherwise attach all three leaf skills.

1. `sonicg-setup-new-project` (greenfield) or `sonicg-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `sonicg-setup-design-system` — Figma tokens/themes in `:core-ui` (Compose `AppTheme` in `core/ui/theme/` when compose)
3. `sonicg-figma-to-xml` if `uiFramework` is `xml`; `sonicg-figma-to-compose` if `compose` (or dialog / bottom-sheet)
4. `sonicg-create-mvi` — Intent/State/Effect/VM + Fragment **or** `*Screen` in `:feature-*`
5. `sonicg-create-clean-architecture` — when new domain/data is required

Data patterns (Retrofit, Room, SharedPreferences) live in **rules** + [`.agents/rules/reference/`](rules/reference/) — not separate skills.

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
| `21`–`25`              | Ads/billing (**ads are not MVI** — `:gmaAds` from GitHub; see `sonicg-implement-admob-ads` / `sonicg-add-admob-*` + `reference/ads-gma.md`), Firebase, startup, Figma assets, in-app update |
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

## Future distribution (not in v1)

**Option A:** Dedicated template repo + sync script that copies a tagged `.agents/` (and `.cursor/` / `.claude/`) into each app. VERSION / CHANGELOG / sync scripts come in a later pass — do not invent local divergent rules; update the template instead.

## How teammates use this

1. Copy `.agents/` into the project root (this folder includes [`GEMINI.md`](GEMINI.md)).
2. Open the project in Android Studio (Gemini Agent) or Gemini CLI.
3. Type `@` and pick a skill (e.g. `@sonicg-create-screen`, `@sonicg-figma-to-xml`, `@sonicg-figma-to-compose`, `@sonicg-localize`).
4. Skills point at `.agents/rules/`. Read `00-global`, `14-security-secrets`, and `16-logging` on every task; path-scoped rules apply when those files are touched.
5. Before PRs: `@sonicg-review-complete` or individual `sonicg-review-*` skills.
