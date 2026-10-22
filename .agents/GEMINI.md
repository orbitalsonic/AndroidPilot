# Hypersoft AI Workflow — Gemini

Production Android app. Kotlin, Clean Architecture, MVVM + MVI (Intent / State / Effect), Koin `lazyModule` only. UI from `.agents/project-settings.json` **`uiFramework`**: `xml` (View Binding + `:presentation`) or `compose` (Jetpack Compose + `:feature-*`). Never Data Binding. No Hilt unless the project already uses it.

## Law

Follow [`.agents/rules/`](rules/) (especially `00-global`, `14-security-secrets`, `16-logging`). Path-scoped rules apply when matching files are touched. Full patterns: [`.agents/rules/reference/`](rules/reference/). Compose packaging: `28-compose-ui` + [reference/compose-ui.md](rules/reference/compose-ui.md).

When [`.agents/project-settings.json`](project-settings.json) exists, obey `uiFramework`, `writeTestsWithFeatures`, `orientation`, `themeModes`, `applicationId`, `appName`, optional `figmaDesignSystemUrl`.

## Typical feature flow

**Shortcut:** `@hsg-create-screen` runs UI + optional domain/data + MVI in one invoke.

1. `@hsg-setup-new-project` (greenfield) or `@hsg-setup-old-project` (existing production app) — persist **`uiFramework`**
2. `@hsg-setup-design-system` — Figma tokens/themes in `:core-ui` (Compose `AppTheme` in `core/ui/theme/` when compose)
3. `@hsg-figma-to-xml` if xml; `@hsg-figma-to-compose` if compose (or `@hsg-create-dialog` / `@hsg-create-bottom-sheet`)
4. `@hsg-create-mvi` — Intent / State / Effect / ViewModel + Fragment **or** `*Screen` in `:feature-*`
5. `@hsg-create-clean-architecture` — only when new domain / data is required
6. `@hsg-review-complete` before PR

## Skills

Invoke with `@` using the skill folder name (`@hsg-create-screen`, `@hsg-create-mvi`, `@hsg-figma-to-xml`, `@hsg-figma-to-compose`, `@hsg-localize`, …). Full map and rules index: [README.md](README.md).
