# OrbitalSonic AI Workflow — Gemini

Production Android app. Kotlin, Clean Architecture, MVVM + MVI (Intent / State / Effect), Koin — `module` for the cold-start graph, `lazyModule` for deferred features / ads (`07-dependency-injection`). UI from `.agents/project-settings.json` **`uiFramework`**: `xml` (View Binding + `:presentation`) or `compose` (Jetpack Compose + `:feature-*`). Never Data Binding. No Hilt unless the project already uses it.

## Law

Follow [`.agents/rules/`](rules/) (especially `00-global`, `14-security-secrets`, `16-logging`). Path-scoped rules apply when matching files are touched. App shape (reference QiblaCompass): **two Activities** — `StartupActivity` (launcher, splash, `nav_graph_startup`: `StartupFragment` → `StartupLanguageFragment` → `StartupPremiumFragment` → `StartupIntroFragment` / `StartupWelcomeBackFragment`) hands off to `MainActivity` (`nav_graph_main`: home tabs + in-app `LanguageFragment` / `PremiumFragment` / settings). Base classes use `InsetsMode` edge-to-edge (`19`, `23`, `29`–`33`). Full patterns: [`.agents/rules/reference/`](rules/reference/). Compose packaging: `28-compose-ui` + [reference/compose-ui.md](rules/reference/compose-ui.md).

When [`.agents/project-settings.json`](project-settings.json) exists, obey `uiFramework`, `writeTestsWithFeatures`, `orientation`, `themeModes`, `applicationId`, `appName`, optional `figmaDesignSystemUrl`.

## Typical feature flow

**Shortcut:** `@sonicg-create-screen` runs UI + optional domain/data + MVI in one invoke.

1. `@sonicg-setup-new-project` (greenfield) or `@sonicg-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `@sonicg-setup-design-system` — Figma tokens/themes in `:core-ui` (Compose `AppTheme` in `core/ui/theme/` when compose)
3. `@sonicg-figma-to-xml` if xml; `@sonicg-figma-to-compose` if compose (or `@sonicg-create-dialog` / `@sonicg-create-bottom-sheet`)
4. `@sonicg-create-mvi` — Intent / State / Effect / ViewModel + Fragment **or** `*Screen` in `:feature-*`
5. `@sonicg-create-clean-architecture` — only when new domain / data is required
6. `@sonicg-review-complete` before PR

## Skills

Invoke with `@` using the skill folder name (`@sonicg-create-screen`, `@sonicg-create-mvi`, `@sonicg-figma-to-xml`, `@sonicg-figma-to-compose`, `@sonicg-localize`, …). Descriptions start with **OrbitalSonic Gemini Skill:** — do not pick Figma plugin skills ("Created by Figma") for Android screens. Full map and rules index: [README.md](README.md).

Cursor twins: `/sonicc-…` under `.cursor/`. Claude Code twins: `/sonic-…` under `.claude/`. Keep all three in sync (`MUST_READ_BEFORE_SKILL_CHANGES.md`).
