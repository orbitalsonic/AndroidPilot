# OrbitalSonic AI Workflow — Agent guide (attach this first)

**Read this file before changing anything under `.cursor/`, `.claude/`, or `.agents/`.**

This repo is the **company OrbitalSonic template** ([AndroidPilot](https://github.com/orbitalsonic/AndroidPilot)) for Clean Architecture Android apps (XML + View Binding **or** Jetpack Compose via `uiFramework`, MVI, Koin `module` / `lazyModule`). Apps copy `.cursor/` / `.claude/` / `.agents/` into their project. Keep this repo the **single source of truth**. Cursor skills use **`/sonic-…`** (description **Sonic Cursor Skill:**); Claude Code twins use **`/sonicc-…`**; Gemini twins use **`@sonicg-…`**.

Full map of existing rules/skills: [`.cursor/README.md`](.cursor/README.md) · [`.claude/README.md`](.claude/README.md)

---

## What lives where

| Thing                   | Path                                                  | Use for                                                            |
|-------------------------|-------------------------------------------------------|--------------------------------------------------------------------|
| **Rules**               | `.cursor/rules/*.mdc`                                 | Standing law (architecture, naming, invariants)                    |
| **Long detail**         | `.cursor/rules/reference/*.md`                        | Full examples / tables (linked from short `.mdc` stubs)            |
| **Skills**              | `.cursor/skills/**/SKILL.md`                          | Multi-step playbooks (`/skill-name` or agent auto-pick)            |
| **Project settings**    | `.cursor/project-settings.json`                       | Per-app knobs (tests, orientation, theme, **uiFramework**, app id) |
| **Bootstrap templates** | `.cursor/skills/project/sonic-setup-new-project/templates/` | Parent*/Base* Kotlin (`InsetsMode`), **startup/** (`StartupActivity` + `MainActivity` + two nav graphs), anim XML, **compose/** when compose |

**Rules** = “always do it this way.”  
**Skills** = “when I ask, follow these steps.”

Do **not** grow a large `.cursor/commands/` tree — prefer skills with `/` invoke.

---

## Hard rules when editing this template

1. **Do not delete rule meaning.** Prefer move / merge / link. If text must leave a `.mdc`, put the **full body** in `rules/reference/` and leave a short stub + link.
2. **Do not invent a second stack** (Hilt, Data Binding, RxJava; Compose when `uiFramework` is `xml`; XML Fragment screens when `uiFramework` is `compose`) unless the user explicitly asks. UI stack is **`uiFramework`**: `xml` | `compose`.
3. **Obey existing patterns** in `.cursor/rules/` — especially `00-global.mdc`.
4. **Unique skill `name:`** — never two skills with the same `name`.
5. **Update** [`.cursor/README.md`](.cursor/README.md) skill map / rules index when you add or rename something.
6. Skills that create features/UI/tests must **read and obey** `.cursor/project-settings.json` when present.
7. **New product skills** must be based on reference apps (see below) and **shown to the user for acceptance** before any skill files are written.
8. **Always update related skills/rules together** — never change one file in a topic cluster and leave siblings stale (see below). Mirror the same edit under `.claude/` (`sonicc-`) and `.agents/` (`sonicg-`) when a twin skill/rule exists.

---

## Keep related skills / rules in sync

When you change a **skill**, **rule**, or **reference** doc, find every peer that teaches the same invariant or workflow and **update those in the same change**. Partial updates cause agents to follow contradictory playbooks.

### How to decide peers

1. Same topic area (`gradle/`, `firebase/`, `review/`, `test/`, …).
2. Skills that **call or defer to** each other (`sonic-gradle-update` ↔ `sonic-gradle-organize`; `sonic-setup-old-project` ↔ Gradle skills).
3. Matching **rule + `rules/reference/`** pair (`08-gradle.mdc` ↔ `reference/gradle.md`).
4. Orchestrators that list the skill (`sonic-review-complete`, `sonic-test-complete`, skill map in `.cursor/README.md`).
5. `.claude/` (`sonicc-`) and `.agents/` (`sonicg-`) twins of anything under `.cursor/`.

### Example clusters (not exhaustive)

| If you change…                                       | Also update…                                                                                                                                                                                                                             |
|------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`gradle/sonic-gradle-update`**                           | `gradle/sonic-gradle-organize`, Groovy → Kotlin DSL + **AGP 9.3+ / built-in Kotlin / R8 `optimization` + `keepRules`** in `project/sonic-setup-old-project` (+ `migration.md`), `08-gradle.mdc`, `rules/reference/gradle.md`, `.claude` / `.agents` Gradle twins |
| **`gradle/sonic-gradle-organize`**                         | `sonic-gradle-update`, `sonic-setup-new-project` / `sonic-setup-old-project` Gradle steps, `08` + `reference/gradle.md`                                                                                                                                    |
| **Groovy → Kotlin DSL guidance**                     | Lives in / must stay aligned with **`sonic-gradle-update`** (convert before bump) **and** `sonic-setup-old-project` Step 2 / `migration.md`; do not teach Groovy conversion in only one place                                                        |
| **`sonic-setup-design-system`**                            | Skill `reference.md`, color/theme notes in `09` / `reference/resources-xml.md` if invariants change, `.claude` / `.agents` twin                                                                                                                      |
| **`sonic-create-mvi` / MVI law**                           | `04-mvi-presentation` + `reference/mvi-presentation.md`, `01-feature-checklist`, `28-compose-ui` + `reference/compose-ui.md` when compose, `sonic-create-screen` (orchestrator), `sonic-review-architecture` if gates change                      |
| **`sonic-create-screen` (orchestrator)**                   | Leaf skills it runs (`sonic-figma-to-xml` / `sonic-figma-to-compose`, `sonic-create-mvi`, `sonic-create-clean-architecture`), `01-feature-checklist`, skill maps in `.cursor/README.md` + `.claude/README.md` + `.agents/README.md`, `.claude` / `.agents` twin                                  |
| **`sonic-create-clean-architecture`**                      | `26` + `reference/` (retrofit/room/prefs), `sonic-create-mvi`, `sonic-create-screen`, `01-feature-checklist`, `.claude` / `.agents` twin                                                                                                                          |
| **UI / Figma (`sonic-figma-to-xml` / `sonic-figma-to-compose`)** | Sibling UI skills (`sonic-create-dialog`, `sonic-create-bottom-sheet`, `sonic-create-custom-view`), `sonic-create-screen`, `09` / `28`, `sonic-setup-new-project` / `sonic-setup-old-project`, `.claude` / `.agents` twins                                                               |
| **Any `test-*` skill**                               | Sibling `test-*` banners/consent rules, `sonic-test-complete`, `11-testing.mdc`                                                                                                                                                                |
| **Any `review-*` skill**                             | Sibling `review-*` including `sonic-review-cleanup`, `sonic-review-complete`, and `sonicc-` / `sonicg-` twins                                                                                                                                            |
| **Firebase / billing / platform skill**              | Sibling skills in that area + matching numbered rule if one exists                                                                                                                                                                       |
| **Any `admob/*` / ads skill**                        | Sibling `admob/sonic-*` / `sonicc-*` skills, `21-ads-billing` + `reference/ads-gma.md`, `sonic-setup-new-project` / `sonic-setup-old-project` ads steps, `.claude` / `.agents` twins                                                                                                |
| **Two-Activity shape (`StartupActivity` / `MainActivity`), `Startup*` twins vs in-app twins** | `23`, `29`–`33`, `17`, `10`, `02`, `19` + `reference/base-ui.md` (`InsetsMode`), `reference/app-flow.md`, `sonic-setup-new-project` (Steps 3–5 + `templates/startup/`, `templates/compose/`), `sonic-setup-old-project` + `migration.md`, `sonic-create-mvi` “Which host?”, `sonic-figma-to-xml` naming table, ads / billing / in-app-update skills, `.claude` / `.agents` twins |
| **Screen / app-flow / premium UI**                   | `29`–`33` screen rules, `reference/app-flow.md`, `17` / `23` / `09`, billing skills + `reference/premium-billing.md`, `sonic-setup-new-project`, `.claude` / `.agents` twins                                                                              |
| **Gemini (`.agents/` / `sonicg-…`) and localize**       | `.agents/` is the full Gemini twin (`sonicg-` folders, `@sonicg-…`, **OrbitalSonic Gemini Skill:**), not localization-only. Keep `sonicg-localize` aligned with AndroidAI `.agents/skills/localize`. Discover **`LanguageDataSource.kt`** (filename — not `@LanguageDataSource` annotation). |
| **Manifest / modules / resource placement**          | `02`, `09` + `resources-xml.md`, `10`, `23`, `sonic-setup-new-project` / `sonic-setup-old-project`, `.claude` / `.agents` twins                                                                                                                                 |

### Checklist before finishing a template edit

- [ ] Updated every peer in the topic cluster (table above or same pattern).
- [ ] Updated `.cursor/README.md` if name/path/description changed.
- [ ] Updated `.claude/` (`sonicc-`) and `.agents/` (`sonicg-`) twins when present.
- [ ] No contradictory “Do not” / step order left between siblings.

---

## Always-on vs not

| File                       | Always-on? | Role                                                             |
|----------------------------|------------|------------------------------------------------------------------|
| `00-global.mdc`            | Yes        | Stack + Always/Never                                             |
| `14-security-secrets.mdc`  | Yes        | Secrets / exported / PII                                         |
| `16-logging.mdc`           | Yes        | `Constants.TAG*` format                                          |
| `01-feature-checklist.mdc` | **No**     | Checklist when scaffolding a feature                             |
| Other `02`–`33`            | **No**     | Glob or description when relevant (`28-compose-ui` when compose; `29`–`33` screen rules) |

**Prefer fewer always-on rules.** New “must always” items → add to `00-global` only if truly every chat needs them; otherwise use globs or a skill.

---

## How to add or update a **rule**

### Small invariant (fits in one screen)

1. Edit the matching `.mdc` under `.cursor/rules/` (or add `NN-topic.mdc`).
2. Frontmatter:
   ```yaml
   ---
   description: Short trigger text for Agent
   globs: ["**/path/**/*.kt"]   # or omit if alwaysApply
   alwaysApply: false
   ---
   ```
3. Keep body short: invariants + BAD/GOOD. Point to `reference/` for long tables.

### Large / detailed rule (examples, long tables)

1. Put **full text** in `.cursor/rules/reference/<topic>.md`.
2. Keep `.mdc` as a **short stub**: must-follow bullets + link to that reference file.
3. Same pattern as `09-resources-xml.mdc` → `reference/resources-xml.md`.

### Naming

- Numbered rules: `00`–`33` style (`26-data-persistence.mdc`, `28-compose-ui.mdc`, `33-screen-premium.mdc`).
- Next free number if adding a new top-level topic.
- Reference files: kebab-case (`mvi-presentation.md`).

### Globs

- Prefer **narrow** paths (`**/presentation/**/*.kt`, `**/feature*/**/*.kt`, `**/data/**/*.kt`) over `**/*.kt`.
- Keep a strong `description` so Agent can still load the rule when relevant.

---

## How to add or update a **skill**

### Folder layout

```
.cursor/skills/<area>/<skill-folder>/SKILL.md
```

Areas already used: `project`, `feature`, `ui`, `i18n`, `review`, `test`, `gradle`, `platform`, `premium`, `billing`, `firebase`, `build`, `release`, `admob`.

### SKILL.md shape

```markdown
---
name: my-skill-name
description: What it does + when to use it (Agent reads this to auto-pick).
---

# Title

Follow `.cursor/rules/…` (and `reference/…` when detail lives there).

Obey `.cursor/project-settings.json` when present.

## Steps

1. …
```

### Pick the right area

| You want to…                                                       | Prefer                                                                                                                                                   |
|--------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| Full feature screen (UI + MVI + optional domain/data) in one invoke | `feature/sonic-create-screen` (orchestrates UI + `sonic-create-mvi` + `sonic-create-clean-architecture`)                                                                   |
| Scaffold presentation only (Intent/State/Effect/VM + Fragment or `*Screen`) | `feature/sonic-create-mvi`                                                                                                                              |
| Add domain/data/repo                                               | `feature/sonic-create-clean-architecture`                                                                                                                      |
| XML / Figma layout only                                            | `ui/sonic-figma-to-xml` (or dialog / bottom-sheet) — when `uiFramework` is `xml`                                                                               |
| Compose / Figma screen only                                        | `ui/sonic-figma-to-compose` (or dialog / bottom-sheet) — when `uiFramework` is `compose`                                                                       |
| Review PR / architecture / perf / security                         | `review/review-*` (+ wire into `sonic-review-complete` if full gate)                                                                                           |
| Unit / integration / E2E tests                                     | `test/test-*` (+ `sonic-test-complete` if full suite)                                                                                                          |
| Gradle catalog / organize                                          | `gradle/`                                                                                                                                                |
| Ship checklist                                                     | `release/pre-release`                                                                                                                                    |
| In-app billing / subscriptions / premium                           | `premium/sonic-implement-in-app-billing`, `premium/sonic-add-subscription-packages`, `premium/sonic-add-inapp-packages`                                                    |
| Firebase RC / Analytics events                                     | `firebase/sonic-implement-firebase-remote-config`, `firebase/sonic-add-firebase-remote-config`, `firebase/sonic-implement-firebase-events`, `firebase/sonic-add-firebase-events` |
| New multi-step product feature (ads, IAP, …)                       | New skill under a clear area (`admob/`, `billing/`, …); add **rules** for invariants                                                                     |
| String localization                                                | Cursor `i18n/sonic-localize` (`/sonic-localize`), Claude `sonicc-localize` (`/sonicc-localize`), Gemini `.agents/skills/sonicg-localize` (`@sonicg-localize`). Do not rename the English word localize. |

### Skill vs rule (quick)

- Repeated **constraint** → **rule** (and `reference/` if long).
- Repeated **workflow** (“do steps 1–7”) → **skill**.
- Do **not** copy full XML/Material rules into every UI skill — **link** `09` + `reference/resources-xml.md`. Compose UI skills **link** `28` + `reference/compose-ui.md`.

### After adding a skill

1. Add a line to the skill map in [`.cursor/README.md`](.cursor/README.md).
2. If it is a review/test “complete” orchestrator, update that skill’s checklist to call the new one.

---

## New product / platform skills — learn from reference apps first

When the user asks to **create a new skill** for a real product feature (examples: `sonic-implement-in-app-update`, `sonic-implement-in-app-review`, billing, ads helpers, etc.), **do not invent the flow from scratch**.

### Reference apps (search in this order)

These are production apps. Use them as the working template source:

| Priority      | Path                                                                       |
|---------------|----------------------------------------------------------------------------|
| **1 (first)** | `E:\SohaibAhmed\UnderWorking\Qibla-Finder-Qibla-Compass-HSAIAppsLab`       |
| **2**         | `E:\SohaibAhmed\UnderWorking\MusicplayerPlayMP3Music-DigitalGenerationHub` |
| **3**         | `E:\SohaibAhmed\UnderWorking\Photo-Collage-Maker-HSAIAppsLab`              |

1. Search **ref 1** for the feature (classes, managers, Fragments, DI, Gradle).
2. If missing or unclear → search **ref 2**, then **ref 3**.
3. Prefer the **highest-priority app that has a complete, working implementation**.

### Mandatory acceptance before writing the skill

**Stop and ask the user before creating any skill files.** Show a short proposal and wait for explicit acceptance.

Include at least:

1. **Which reference app** you used (path + priority).
2. **What you found** — key files (manager, host screen, DI, Gradle deps).
3. **Proposed skill** — `name`, folder (`platform/…`, `feature/…`, etc.), and high-level steps.
4. **Where to implement in apps** — e.g. host screen (`DashboardFragment`), module (`:core-ui` manager + `:presentation` host). Confirm this with the user.
5. Ask clearly: **“Accept this skill plan? (yes / change …)”**

Only after the user accepts → write `SKILL.md` (put the agreed host screen / modules **inside the skill**), any templates under that skill folder, rules/reference if needed, and update `.cursor/README.md`.

### Workflow checklist (new skill from refs)

1. Read this README + `.cursor/README.md` + `00-global`.
2. Search ref apps **1 → 2 → 3** for a working implementation.
3. Draft proposal (template summary + host screen + modules).
4. **Ask user for acceptance** — do not write skill files yet.
5. On accept: create skill (+ templates if useful), optional rule stub, update skill map.
6. On reject/change: revise proposal and ask again.

---

## Typical work you will be asked for

### New feature conventions

1. Read `00-global`, `01-feature-checklist`, `04` / `reference/mvi-presentation`, `03`, `07` / DI reference.
2. Prefer extending existing skills (`sonic-create-screen`, `sonic-create-mvi`, `sonic-create-clean-architecture`) over parallel skills. Full screens → `sonic-create-screen`; layer-only work → leaf skills.
3. Add a **rule** only for new invariants; add a **skill** only for a new multi-step flow.

### Updates (deps, Gradle, platform)

1. Prefer updating `gradle/sonic-gradle-update`, `gradle/sonic-gradle-organize`, or `08` / `reference/gradle.md` — **and always the whole Gradle cluster together** (including Groovy → Kotlin DSL in `sonic-gradle-update` + `sonic-setup-old-project`).
2. **`sonic-gradle-update`** must: convert remaining Groovy `*.gradle` → Kotlin DSL when present; bump to **AGP 9.3+** and remove `kotlin-android` / `kotlin-kapt`; migrate R8 to `optimization { enable = true }` + `src/main/keepRules/*.keep`; bump catalog **and** hardcoded `"g:a:v"` lines; migrate them into `libs.versions.toml`; place under organize section headers.
3. Library allow/deny list → `13-libraries-stack.mdc`.
4. No new libraries without explicit human approval (`00-global`) — migrating an existing hardcode into the catalog is required, not a new library.

### Testing

1. Extend `test/sonic-test-unit`, `sonic-test-integration`, `sonic-test-e2e`, or `sonic-test-complete`.
2. Conventions → `11-testing.mdc` (pyramid: unit = fakes/Flow; integration = Room/MockWebServer; e2e = device UI).
3. Each `test-*` skill starts with a one-line `We are going to …` banner; device-needed skills must mention emulator/physical device (or already attached).
4. Authoring skills **write missing tests, execute, and report**. On failures: **ask user consent** before fixing production code, then retest. Do not silently weaken assertions.
5. Respect `project-settings.json` → `writeTestsWithFeatures`.
6. After rename/add of test skills, update the skill map in [`.cursor/README.md`](.cursor/README.md).

### Review

1. Extend `review/sonic-review-architecture`, `sonic-review-performance`, `sonic-review-security`, or `sonic-review-complete`.
2. Keep Pass/Fail report style consistent with existing review skills.

---

## Do / Don’t

**Do**

- Read `.cursor/README.md` + matching existing rule/skill before writing.
- For **new product skills**, search reference apps (Qibla → Music Player → Photo Collage) and **get user acceptance** before writing files.
- Put agreed host screen / modules **inside the skill** after acceptance.
- Link to `reference/` instead of duplicating long text.
- Keep skill `description` specific (when to use + when not to).
- Use simple wording in new docs.
- When editing one skill/rule, **update every related peer** (Gradle cluster, test siblings, review siblings, `.claude` / `.agents` twins, etc.).

**Don’t**

- Invent platform/feature flows when a reference app already has a working one.
- Write a new skill without showing the plan and waiting for acceptance.
- Delete reference bodies or rule lines “to clean up” without moving them.
- Add always-on rules for niche topics.
- Create duplicate skill names or a second skill tree (`screens/` vs `ui/`, etc.).
- Put Retrofit/Room/prefs as new skills — use `26-data-persistence` + `reference/`.
- Convert ads (`:gmaAds`) to MVI unless the user **explicitly** asks; place from GitHub and use extensions (`21-ads-billing`, `reference/ads-gma.md`, `admob/*`).
- Change Speak-Translate or other apps unless the user asks to sync.
- Update only `sonic-gradle-update` (or only `sonic-setup-old-project`) when Groovy → Kotlin DSL / catalog / organize behavior changes — keep the whole Gradle cluster aligned.

---

## Suggested prompt when attaching this file

> Read `MUST_READ_BEFORE_SKILL_CHANGES.md` and `.cursor/README.md` first. Then: \<your task\>. Follow the template rules: no content loss, prefer move/link, update the skill map if you add skills, **always update related skills/rules in the same topic cluster** (e.g. Gradle: sonic-gradle-update + Groovy→KTS + sonic-gradle-organize + 08/reference). For new product skills, search the reference apps (priority order), show me the plan (template + host screen), wait for my acceptance, then create the skill.

---

## Quick paths

| Need                | Open                                              |
|---------------------|---------------------------------------------------|
| Template overview   | `.cursor/README.md`                               |
| Always-on law       | `.cursor/rules/00-global.mdc`                     |
| Feature checklist   | `.cursor/rules/01-feature-checklist.mdc`          |
| XML detail          | `.cursor/rules/reference/resources-xml.md`        |
| MVI detail          | `.cursor/rules/reference/mvi-presentation.md`     |
| DI detail           | `.cursor/rules/reference/dependency-injection.md` |
| Compose detail      | `.cursor/rules/reference/compose-ui.md`           |
| App settings schema | `.cursor/project-settings.json` (`uiFramework`)   |
