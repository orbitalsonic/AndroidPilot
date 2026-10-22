---
name: hsg-create-screen
description: Hypersoft Gemini Skill: Full screen scaffold in one invoke — UI (hsg-figma-to-xml or hsg-figma-to-compose) + hsg-create-mvi + hsg-create-clean-architecture when domain/data is needed. Use when the user wants a complete feature screen without attaching the three skills separately. Do not use for ads / :gmaAds, dialogs-only, or bottom-sheets-only (use hsg-create-dialog / hsg-create-bottom-sheet).
---

# Create Screen (orchestrator)

One skill for a **complete feature screen**: layout/UI → optional domain/data → presentation MVI.

**Does not replace** the leaf skills. It **loads and runs** them in order. Prefer this when the user would otherwise attach `hsg-figma-to-xml` / `hsg-figma-to-compose` + `hsg-create-mvi` + `hsg-create-clean-architecture`.

Follow `.agents/rules/` — especially `00-global`, `01-feature-checklist`, and whatever each leaf skill lists.

Obey `.agents/project-settings.json` when present (`uiFramework`, `writeTestsWithFeatures`, `orientation`, `themeModes`).

## When to use

| Use **`hsg-create-screen`** | Use a leaf skill instead |
|-------------------------|--------------------------|
| New feature screen end-to-end (Figma or freeform) | Layout only → `hsg-figma-to-xml` / `hsg-figma-to-compose` |
| User asked for “full screen” / “wire everything” | MVI only (layout exists) → `hsg-create-mvi` |
| Domain + UI + MVI in one chat | Domain/data only → `hsg-create-clean-architecture` |
| | Dialog / sheet UI only → `hsg-create-dialog` / `hsg-create-bottom-sheet` |

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

| `uiFramework` | Run skill |
|---------------|-----------|
| `xml` (default) | **`hsg-figma-to-xml`** (`.agents/skills/hsg-figma-to-xml/SKILL.md`) |
| `compose` | **`hsg-figma-to-compose`** (`.agents/skills/hsg-figma-to-compose/SKILL.md`) |

- Dashboard + BottomNavigation → produce menu + `nav_graph_dashboard` as that skill requires
- Dialog-only / sheet-only requests → stop and redirect to `hsg-create-dialog` / `hsg-create-bottom-sheet` (do not continue MVI here unless the user also asked for a host screen)

### 2 — Domain + data (conditional)

If the screen needs **new** UseCases, repository interfaces/impls, DataSources, Retrofit, Room, or prefs wiring:

→ Run **`hsg-create-clean-architecture`** (`.agents/skills/hsg-create-clean-architecture/SKILL.md`)

Skip this step when only existing domain APIs are reused.

### 3 — Presentation MVI

→ Run **`hsg-create-mvi`** (`.agents/skills/hsg-create-mvi/SKILL.md`)

Wire Fragment / `*Screen`, Intent / State / Effect / ViewModel, DI, nav destination, and tests per that skill + `writeTestsWithFeatures`.

## After all steps

- Walk **`01-feature-checklist`**
- Summarize what was created (layouts, domain/data files if any, MVI package, nav)
- Point to `@hsg-review-complete` before PR when appropriate

## Do not

- Duplicate leaf-skill bodies inside this file — always **open and follow** the linked `SKILL.md`
- Skip `hsg-create-clean-architecture` when new domain/data is clearly required, then invent repos inside `hsg-create-mvi`
- Create presentation → `:data` dependencies
- Run only UI and stop when the user asked for a full screen (unless they cancel mid-flow)
