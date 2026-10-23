---
name: sonicg-create-screen
description: OrbitalSonic Gemini Skill: Full screen scaffold in one invoke — UI (sonicg-figma-to-xml or sonicg-figma-to-compose) + sonicg-create-mvi + sonicg-create-clean-architecture when domain/data is needed. Use when the user wants a complete feature screen without attaching the three skills separately. Do not use for ads / :gmaAds, dialogs-only, or bottom-sheets-only (use sonicg-create-dialog / sonicg-create-bottom-sheet).
---

# Create Screen (orchestrator)

One skill for a **complete feature screen**: layout/UI → optional domain/data → presentation MVI.

**Does not replace** the leaf skills. It **loads and runs** them in order. Prefer this when the user would otherwise attach `sonicg-figma-to-xml` / `sonicg-figma-to-compose` + `sonicg-create-mvi` + `sonicg-create-clean-architecture`.

Follow `.agents/rules/` — especially `00-global`, `01-feature-checklist`, and whatever each leaf skill lists.

Obey `.agents/project-settings.json` when present (`uiFramework`, `writeTestsWithFeatures`, `orientation`, `themeModes`).

## When to use

| Use **`sonicg-create-screen`**                           | Use a leaf skill instead                                         |
|---------------------------------------------------|------------------------------------------------------------------|
| New feature screen end-to-end (Figma or freeform) | Layout only → `sonicg-figma-to-xml` / `sonicg-figma-to-compose`                |
| User asked for “full screen” / “wire everything”  | MVI only (layout exists) → `sonicg-create-mvi`                          |
| Domain + UI + MVI in one chat                     | Domain/data only → `sonicg-create-clean-architecture`                   |
|                                                   | Dialog / sheet UI only → `sonicg-create-dialog` / `sonicg-create-bottom-sheet` |

**Never** use for ads / `:gmaAds` / AdMob (`21-ads-billing`) unless the user **explicitly** asks to convert ads to MVI.

## Preconditions

Confirm before coding:

1. **Feature name** (camelCase folder, e.g. `userProfile`)
2. **Figma URL** (optional) or freeform layout brief
3. **New domain/data?** — yes if new UseCase / repository / DataSource / API / Room is required; no if injecting existing UseCases only
4. Read **`uiFramework`** from `project-settings.json` (default `xml`)

## Orchestration order (mandatory)

Execute **in this order**. For each step: **read that skill’s `SKILL.md` and follow it fully** — do not invent a parallel checklist.

### 1 — UI

| `uiFramework`   | Run skill                                                              |
|-----------------|------------------------------------------------------------------------|
| `xml` (default) | **`sonicg-figma-to-xml`** (`.agents/skills/sonicg-figma-to-xml/SKILL.md`)         |
| `compose`       | **`sonicg-figma-to-compose`** (`.agents/skills/sonicg-figma-to-compose/SKILL.md`) |

- Decide the host first: startup funnel screen (`StartupActivity`, `Startup*` naming, `fragment_startup_*.xml`) vs app screen (`MainActivity`, plain naming) — `sonicg-create-mvi` “Which host?”. Startup Language / Premium and in-app Language / Premium are **separate** screens (`30`, `33`)
- Bottom navigation → `menu_bottom_nav_main` + tabs in `nav_graph_main` (MainActivity-hosted, default) or nested `nav_graph_dashboard` (optional variant) as that skill requires
- Dialog-only / sheet-only requests → stop and redirect to `sonicg-create-dialog` / `sonicg-create-bottom-sheet` (do not continue MVI here unless the user also asked for a host screen)

### 2 — Domain + data (conditional)

If the screen needs **new** UseCases, repository interfaces/impls, DataSources, Retrofit, Room, or prefs wiring:

→ Run **`sonicg-create-clean-architecture`** (`.agents/skills/sonicg-create-clean-architecture/SKILL.md`)

Skip this step when only existing domain APIs are reused.

### 3 — Presentation MVI

→ Run **`sonicg-create-mvi`** (`.agents/skills/sonicg-create-mvi/SKILL.md`)

Wire Fragment / `*Screen`, Intent / State / Effect / ViewModel, DI, nav destination, and tests per that skill + `writeTestsWithFeatures`.

## After all steps

- Walk **`01-feature-checklist`**
- Summarize what was created (layouts, domain/data files if any, MVI package, nav)
- Point to `@sonicg-review-complete` before PR when appropriate

## Do not

- Duplicate leaf-skill bodies inside this file — always **open and follow** the linked `SKILL.md`
- Skip `sonicg-create-clean-architecture` when new domain/data is clearly required, then invent repos inside `sonicg-create-mvi`
- Create presentation → `:data` dependencies
- Run only UI and stop when the user asked for a full screen (unless they cancel mid-flow)
