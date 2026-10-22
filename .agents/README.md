# Hypersoft AI Workflow — Gemini (v1)

Company-grade **rules** + **skills** for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `lazyModule`).

This folder is part of the [ai-workflow](https://github.com/hypersoftdev/ai-workflow) template for Android Studio Gemini. Cursor teammates use [`.cursor/`](../.cursor/) (`/hs-…`). Claude Code teammates use [`.claude/`](../.claude/) (`/hsc-…`). Keep all three in sync. Skills invoke as `@hsg-…`.

## Rules vs Skills

|      | **Rules** (`.agents/rules/`)                   | **Skills** (`.agents/skills/`)                             |
|------|------------------------------------------------|------------------------------------------------------------|
| Role | Company law — invariants, naming, architecture | Multi-step playbooks (+ templates)                         |
| When | Always-on or glob-matched while editing        | Invoked by name (`@` or agent pick) or trigger description |
| Size | Prefer clear invariants + BAD/GOOD             | Full workflows                                             |

**Long detail** lives under [`rules/reference/`](rules/reference/). `.md` rule files hold **short invariants + links** to those reference docs (so Agent context stays smaller). Do not delete reference files — edit them when rules change.

**Commands:** Prefer skills with `@` invoke. Do not grow a large `.agents/commands/` tree unless needed.

## Project settings

After `hsg-setup-new-project` / `hsg-setup-old-project` (or when joining an app), settings live in:

**[`.agents/project-settings.json`](project-settings.json)**

All feature/UI/test skills **must read and obey** this file when present:

| Key                      | Values                            | Meaning                                                                                                                                   |
|--------------------------|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `writeTestsWithFeatures` | `true` / `false`                  | Write unit/integration/E2E tests while scaffolding features                                                                               |
| `orientation`            | `portrait` / `landscape` / `both` | Which orientations layouts must support                                                                                                   |
| `themeModes`             | `day` / `night` / `both`          | Day / night / both theme resources                                                                                                        |
| `applicationId`          | string                            | Root package / applicationId                                                                                                              |
| `appName`                | string                            | Display name                                                                                                                              |
| `figmaDesignSystemUrl`   | Figma `/design/` URL or omit/`""` | Optional; Figma file for `hsg-setup-design-system`                                                                                            |
| `uiFramework`            | `xml` / `compose`                 | App UI stack. **xml** = View Binding + `:presentation`. **compose** = Jetpack Compose + `:feature-*` (AnimeHub). Default `xml` if missing |

## Skill map

Invoke as `@hsg-<name>`. Cursor twins use `/hs-…`. Claude Code twins use `/hsc-…`.

All skills live flat under `.agents/skills/hsg-<name>/SKILL.md`.

```
hsg-setup-new-project          Bootstrap multi-module app + persist settings (Firebase BOM + analytics/crashlytics/messaging + RC cache)
hsg-setup-old-project          Migrate existing production app to hsg-setup-new-project architecture; preserve product behavior; confirm settings first
hsg-setup-design-system        Figma design-system file → :core-ui tokens/themes (+ AppTheme in core/ui/theme/ when compose)
hsg-create-screen              Full screen: UI (hsg-figma-to-xml|compose) + hsg-create-mvi + hsg-create-clean-architecture when needed
hsg-create-mvi                 Presentation MVI only (no domain/data) — Fragment if xml, `*Screen` in `:feature-*` if compose
hsg-create-clean-architecture  Domain + data + core pieces as needed
hsg-figma-to-xml               XML layouts (+ Figma design-to-code); `uiFramework` xml only
hsg-figma-to-compose            Compose screens (AnimeHub `*Screen` / `*ScreenContent`); `uiFramework` compose
hsg-create-dialog               Dialog UI — XML if xml, Compose `*Dialog` if compose
hsg-create-bottom-sheet         Bottom sheet UI — XML if xml, Compose `*BottomSheet` if compose
hsg-create-custom-view          Custom View (xml) or reusable composable (compose)
hsg-localize                    Native string localization — one worker per language (`values-*` only)
hsg-review-architecture         Architecture / MVI / boundaries
hsg-review-performance           ANR / lists / dispatchers
hsg-review-security              Secrets / manifest / PII
hsg-review-cleanup               Unused code/resources / app size / release shrink
hsg-review-complete              Runs all review-* + summary report
hsg-test-unit                    Write+run JVM unit/Flow tests; consent before fix
hsg-test-integration              Write+run multi-layer tests; consent before fix
hsg-test-e2e                      Write+run E2E on device; consent before fix
hsg-test-complete                 Full run + walkthrough; consent before fix
hsg-gradle-organize               Catalog + android/base/dependencies section order (signingConfigs, bundle)
hsg-gradle-update                 Groovy→KTS if needed; bump all deps (catalog + hardcodes); migrate to libs.versions.toml + sections
hsg-build-debug-apk               Debug APK → device install + launch
hsg-build-release-apk             Release signing + APK → device install + launch
hsg-build-release-bundle          Release signing + AAB (no device)
hsg-implement-in-app-update       Play In-App Updates
hsg-implement-in-app-review       Play In-App Review
hsg-implement-firebase-messaging  firebase-messaging dep (:core-platform) only
hsg-implement-firebase-remote-config  First-time RC + SharedPref cache + Entrance fetch
hsg-add-firebase-remote-config    Add RC keys to existing SharedPref + Remote Config classes
hsg-implement-firebase-events     First-time full-app Analytics (screen_view + ui_click)
hsg-add-firebase-events           Add Analytics events for selected screens
hsg-implement-in-app-billing      Greenfield Play billing (subs + in-app, v4 stack)
hsg-update-in-app-billing         Migrate hypersoft inappbilling v3 → v4.0.0
hsg-add-subscription-packages     Add subscription tiers to existing billing
hsg-add-inapp-packages            Add one-time in-app products to existing billing
hsg-implement-admob-ads           First-time AdMob screen wiring (:gmaAds from GitHub)
hsg-add-admob-banner              Add banner placement to existing :gmaAds
hsg-add-admob-interstitial        Add interstitial placement
hsg-add-admob-native              Add native placement
hsg-add-admob-rewarded            Add rewarded placement
hsg-add-admob-rewarded-interstitial  Add rewarded interstitial placement
hsg-add-admob-appOpen-Entrance    Wire App Open ENTRANCE (splash/consent)
hsg-add-admob-appOpen-lifecycle   Wire App Open LIFECYCLE (resume)
```

### Typical feature flow

**Shortcut:** `@hsg-create-screen` runs steps 3–5 in one invoke (UI → optional domain/data → MVI). Prefer it when you would otherwise attach all three leaf skills.

1. `hsg-setup-new-project` (greenfield) or `hsg-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `hsg-setup-design-system` — Figma tokens/themes in `:core-ui` (Compose `AppTheme` in `core/ui/theme/` when compose)
3. `hsg-figma-to-xml` if `uiFramework` is `xml`; `hsg-figma-to-compose` if `compose` (or dialog / bottom-sheet)
4. `hsg-create-mvi` — Intent/State/Effect/VM + Fragment **or** `*Screen` in `:feature-*`
5. `hsg-create-clean-architecture` — when new domain/data is required

Data patterns (Retrofit, Room, SharedPreferences) live in **rules** + [`.agents/rules/reference/`](rules/reference/) — not separate skills.

## Rules index (`00`–`33`)

| File                   | Role                                                                                                                                                                          |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `00-global`            | Always-on stack + Always/Never law                                                                                                                                            |
| `01-feature-checklist` | Feature scaffolding checklist (not always-on)                                                                                                                                 |
| `02`–`03`              | Modules + Clean Architecture (+ SOLID in `03`)                                                                                                                                |
| `04`–`07`              | MVI, Kotlin, coroutines, DI (`04`/`07` → `reference/`)                                                                                                                        |
| `08`–`10`              | Gradle (`08` → `reference/gradle.md`: section order, signingConfigs, bundle, `base`), resources/XML, manifest (`09` → `reference/`)                                           |
| `11`–`13`              | Testing, naming, libraries                                                                                                                                                    |
| `14`–`16`              | Security (always), compatibility, logging (always)                                                                                                                            |
| `17`–`20`              | Nav (dual graphs / `navigateRootTo`), errors, base UI (`19` → `reference/`), permissions                                                                                      |
| `21`–`25`              | Ads/billing (**ads are not MVI** — `:gmaAds` from GitHub; see `hsg-implement-admob-ads` / `add-admob-*` + `reference/ads-gma.md`), Firebase, startup, Figma assets, in-app update |
| `26-data-persistence`  | Retrofit / Room / SharedPreferences (`:core-network` / `:core-database` when added)                                                                                           |
| `27-in-app-review`     | Play In-App Review placement (`InAppReviewManager`)                                                                                                                           |
| `28-compose-ui`        | Compose feature modules / Screen-Content / NavGraph (`uiFramework` compose) — [reference/compose-ui.md](rules/reference/compose-ui.md)                                        |
| `29`–`33`              | Screen rules: Entrance, Language, Onboarding, Dashboard, Premium (paywall sticky footer + 3s close)                                                                           |

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

**Option A:** Dedicated template repo + sync script that copies a tagged `.agents/` (and `.claude/` / `.cursor/`) into each app. VERSION / CHANGELOG / sync scripts come in a later pass — do not invent local divergent rules; update the template instead.

## How teammates use this

1. Copy `.agents/` into the project root (this folder includes [`GEMINI.md`](GEMINI.md)).
2. Open the project in Android Studio (Gemini) or Gemini CLI.
3. Type `@` and pick a skill (e.g. `@hsg-create-screen`, `@hsg-figma-to-xml`, `@hsg-figma-to-compose`, `@hsg-localize`).
4. Skills point at `.agents/rules/`. Read `00-global`, `14-security-secrets`, and `16-logging` on every task; path-scoped rules apply when those files are touched.
5. Before PRs: `@hsg-review-complete` or individual `hsg-review-*` skills.
