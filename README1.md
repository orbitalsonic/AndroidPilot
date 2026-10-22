# Hypersoft AI Workflow

Company template for **Cursor**, **Claude Code**, and **Android Studio Gemini**. Single source of truth for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `lazyModule`).

Apps copy these folders into the project root — do not fork rules/skills inside an app.

| Tool | Copy this folder |
|------|------------------|
| **Cursor** | [`.cursor/`](.cursor/) |
| **Claude Code** | [`.claude/`](.claude/) |
| **Android Studio Gemini** | [`.agents/`](.agents/) |

Keep `.cursor/`, `.claude/`, and `.agents/` in sync. If you change a rule or skill in one, update the twins. Skill names differ only by prefix: Cursor `/hs-…`, Claude Code `/hsc-…`, Gemini `@hsg-…`. Android Studio loads skills from `.agents/skills` (not `.skills` or `.gemini`).

## What’s in this repo

| Path | Role |
|------|------|
| [`.cursor/rules/`](.cursor/rules/) | Company law (architecture, naming, invariants) |
| [`.cursor/skills/`](.cursor/skills/) | Cursor playbooks (`/hs-skill-name`) |
| [`.claude/rules/`](.claude/rules/) | Same law, Claude Code format |
| [`.claude/skills/`](.claude/skills/) | Claude Code playbooks (`/hsc-skill-name`) |
| [`.agents/rules/`](.agents/rules/) | Same law, Gemini format |
| [`.agents/skills/`](.agents/skills/) | Gemini playbooks (`@hsg-skill-name`) |
| [`MUST_READ_BEFORE_SKILL_CHANGES.md`](MUST_READ_BEFORE_SKILL_CHANGES.md) | How to edit this template |

Full skill map and rules index:

- Cursor: [`.cursor/README.md`](.cursor/README.md)
- Claude Code: [`.claude/README.md`](.claude/README.md)
- Gemini: [`.agents/README.md`](.agents/README.md)

## How teammates use this in an app

1. Copy `.cursor/`, `.claude/`, and/or `.agents/` into the Android app root.
2. Open the app in Cursor, run `claude` at the repo root, or open the app in Android Studio (Gemini Agent).
3. **Cursor:** type `/hs` (for example `/hs-setup-new-project`). **Claude Code:** type `/hsc` (for example `/hsc-setup-new-project`). **Android Studio Gemini:** type `@hsg` (for example `@hsg-setup-new-project`). Cursor skills say **Hypersoft Cursor Skill:**; Claude skills say **Hypersoft Claude Skill:**; Gemini skills say **Hypersoft Gemini Skill:**. Do not pick Figma plugin skills (“Created by Figma”) for Android screens.
4. After setup, obey `.cursor/project-settings.json` (or the twin under `.claude/` / `.agents/`) — especially `uiFramework`.

### Gemini localization (`hsg-localize`)

Use this from **Android Studio Gemini Agent** after `.agents/` is in the app root. Invoke **`@hsg-localize`** (type `@hsg`). Cursor and Claude twins are `/hs-localize` and `/hsc-localize`. Description starts with **Hypersoft Gemini Skill:**.

1. Gemini finds `LanguageDataSource.kt` and lists language codes.
2. You confirm languages, then complete vs missing-only localization.
3. It snapshots source strings once, then translates each locale in **parallel** (one worker per language, disjoint `values-*` folders), preserves placeholders/XML, and reports files created/modified.

### Typical feature flow

Cursor: `/hs-…` · Claude Code: `/hsc-…` · Gemini: `@hsg-…` (same steps)

1. `/hs-setup-new-project` or `/hsc-setup-new-project` or `@hsg-setup-new-project` (greenfield) · `/hs-setup-old-project` or `/hsc-setup-old-project` or `@hsg-setup-old-project` (existing app)
2. `/hs-setup-design-system` or `/hsc-setup-design-system` or `@hsg-setup-design-system` — Figma tokens/themes in `:core-ui`
3. `/hs-figma-to-xml` or `/hsc-figma-to-xml` or `@hsg-figma-to-xml` if xml; `/hs-figma-to-compose` or `/hsc-figma-to-compose` or `@hsg-figma-to-compose` if compose
4. `/hs-create-mvi` or `/hsc-create-mvi` or `@hsg-create-mvi` — Intent / State / Effect / ViewModel + Fragment **or** `*Screen`
5. `/hs-create-clean-architecture` or `/hsc-create-clean-architecture` or `@hsg-create-clean-architecture` — only when new domain / data is required
6. `/hs-review-complete` or `/hsc-review-complete` or `@hsg-review-complete` before PR

## How to change this template

Read [`MUST_READ_BEFORE_SKILL_CHANGES.md`](MUST_READ_BEFORE_SKILL_CHANGES.md) **before** editing anything under `.cursor/`, `.claude/`, or `.agents/`.

Hard rules:

- Do not invent local rules inside an app — update **this** repo, then copy into apps.
- Do not delete rule meaning; prefer move / merge / link.
- Update related skills/rules in the same change (Cursor `hs-` ↔ Claude `hsc-` ↔ Gemini `hsg-`).
- New product skills: learn from the reference apps, show a plan, wait for acceptance.

## Repo

https://github.com/hypersoftdev/ai-workflow
