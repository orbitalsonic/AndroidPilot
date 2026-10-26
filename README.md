# OrbitalSonic AI Workflow

Company template for **Cursor**, **Claude Code**, and **Android Studio Gemini**. Single source of truth for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `module` / `lazyModule`).

Apps copy these folders into the project root — do not fork rules/skills inside an app.

| Tool | Copy this folder |
|------|------------------|
| **Cursor** | [`.cursor/`](.cursor/) |
| **Claude Code** | [`.claude/`](.claude/) |
| **Android Studio Gemini** | [`.agents/`](.agents/) |

Keep `.cursor/`, `.claude/`, and `.agents/` in sync. If you change a rule or skill in one, update the twins. Skill names differ only by prefix: Cursor `/sonicc-…`, Claude Code `/sonic-…`, Gemini `@sonicg-…`. Android Studio loads skills from `.agents/skills` (not `.skills` or `.gemini`).

## What’s in this repo

| Path | Role |
|------|------|
| [`.cursor/rules/`](.cursor/rules/) | Company law (architecture, naming, invariants) |
| [`.cursor/skills/`](.cursor/skills/) | Cursor playbooks (`/sonicc-skill-name`) |
| [`.claude/rules/`](.claude/rules/) | Same law, Claude Code format |
| [`.claude/skills/`](.claude/skills/) | Claude Code playbooks (`/sonic-skill-name`) |
| [`.agents/rules/`](.agents/rules/) | Same law, Gemini format |
| [`.agents/skills/`](.agents/skills/) | Gemini playbooks (`@sonicg-skill-name`) |
| [`MUST_READ_BEFORE_SKILL_CHANGES.md`](MUST_READ_BEFORE_SKILL_CHANGES.md) | How to edit this template |

Full skill map and rules index:

- Cursor: [`.cursor/README.md`](.cursor/README.md)
- Claude Code: [`.claude/README.md`](.claude/README.md)
- Gemini: [`.agents/README.md`](.agents/README.md)

## How teammates use this in an app

1. Copy `.cursor/`, `.claude/`, and/or `.agents/` into the Android app root.
2. Open the app in Cursor, run `claude` at the repo root, or open the app in Android Studio (Gemini Agent).
3. **Cursor:** type `/sonicc` (for example `/sonicc-setup-new-project`). **Claude Code:** type `/sonic` (for example `/sonic-setup-new-project`). **Android Studio Gemini:** type `@sonicg` (for example `@sonicg-setup-new-project`). Cursor skills say **Sonic Cursor Skill:**; Claude skills say **OrbitalSonic Claude Skill:**; Gemini skills say **OrbitalSonic Gemini Skill:**. Do not pick Figma plugin skills (“Created by Figma”) for Android screens.
4. After setup, obey `.cursor/project-settings.json` (or the twin under `.claude/` / `.agents/`) — especially `uiFramework`.

### Gemini localization (`sonicg-localize`)

Use this from **Android Studio Gemini Agent** after `.agents/` is in the app root. Invoke **`@sonicg-localize`** (type `@sonicg`). Cursor and Claude twins are `/sonicc-localize` and `/sonic-localize`. Description starts with **OrbitalSonic Gemini Skill:**.

1. Gemini finds `DpLanguages.kt` (or `LanguageDataSource.kt`), lists the language codes, and maps each one to its `values-*` folder with `rules/reference/languages.md` (104-language catalog, RTL set).
2. You confirm languages, then complete vs missing-only localization.
3. It snapshots source strings once, then translates each locale in **parallel** (one worker per language, disjoint `values-*` folders), preserves placeholders/XML, and reports files created/modified.

### Typical feature flow

Cursor: `/sonicc-…` · Claude Code: `/sonic-…` · Gemini: `@sonicg-…` (same steps)

1. `/sonicc-setup-new-project` or `/sonic-setup-new-project` or `@sonicg-setup-new-project` (greenfield) · `/sonicc-setup-old-project` or `/sonic-setup-old-project` or `@sonicg-setup-old-project` (existing app)
2. `/sonicc-setup-design-system` or `/sonic-setup-design-system` or `@sonicg-setup-design-system` — Figma tokens/themes in `:core-ui`
3. `/sonicc-figma-to-xml` or `/sonic-figma-to-xml` or `@sonicg-figma-to-xml` if xml; `/sonicc-figma-to-compose` or `/sonic-figma-to-compose` or `@sonicg-figma-to-compose` if compose
4. `/sonicc-create-mvi` or `/sonic-create-mvi` or `@sonicg-create-mvi` — Intent / State / Effect / ViewModel + Fragment **or** `*Screen`
5. `/sonicc-create-clean-architecture` or `/sonic-create-clean-architecture` or `@sonicg-create-clean-architecture` — only when new domain / data is required
6. `/sonicc-review-complete` or `/sonic-review-complete` or `@sonicg-review-complete` before PR

**Shortcut:** `/sonicc-create-screen` / `/sonic-create-screen` / `@sonicg-create-screen` runs steps 3–5 in one invoke.

## How to change this template

Read [`MUST_READ_BEFORE_SKILL_CHANGES.md`](MUST_READ_BEFORE_SKILL_CHANGES.md) **before** editing anything under `.cursor/`, `.claude/`, or `.agents/`.

Hard rules:

- Do not invent local rules inside an app — update **this** repo, then copy into apps.
- Do not delete rule meaning; prefer move / merge / link.
- Update related skills/rules in the same change (Cursor `sonicc-` ↔ Claude `sonic-` ↔ Gemini `sonicg-`).
- New product skills: learn from the reference apps, show a plan, wait for acceptance.

## Repo

https://github.com/orbitalsonic/AndroidPilot

## License

Copyright OrbitalSonic. Licensed under the Apache License, Version 2.0 — see [LICENSE](LICENSE).
