---
name: sonicg-review
description: OrbitalSonic Gemini Skill: Full project/PR review gate — asks yes/no for architecture, cleanup, performance, and security, runs the chosen review-* skills, and merges them into one Pass/Fail report with a numbered fix list. Use when asking for a complete review, review-*, pre-merge quality gate, or @sonicg-review. For a single dimension call sonicg-review-architecture / sonicg-review-cleanup / sonicg-review-performance / sonicg-review-security directly.
---

# Review (orchestrator)

Obey `.agents/project-settings.json` when present.

Leaf skills: **`sonicg-review-architecture`**, **`sonicg-review-cleanup`**, **`sonicg-review-performance`**, **`sonicg-review-security`**. Their checks and severities apply unchanged — this skill only selects, orders, and aggregates.

## Step 0 — Yes / no questions (mandatory AskUserQuestion)

Ask these in **one** `AskUserQuestion` call, each as a yes / no question. Default every answer to **yes**; the user unticks what they do not want.

| # | Question | Runs |
|---|----------|------|
| 1 | Review **architecture** (Clean Architecture, MVI, module boundaries, DI cold-start graph)? | `sonicg-review-architecture` |
| 2 | Review **cleanup / app size** (unused code & drawables, PNG→WebP, `:app` release R8 `optimization`)? | `sonicg-review-cleanup` |
| 3 | Review **performance** (ANR risk, dispatchers, large lists, RecyclerView / Lazy lists)? | `sonicg-review-performance` |
| 4 | Review **security** (secrets, exported components, permissions, log leaks, keep rules)? | `sonicg-review-security` |

Wait for the answers. If every answer is **no**, stop and say nothing was selected. Skip the questions only when the user already named the dimensions in the request (e.g. "review architecture and security only") — then run exactly those.

## Step 1 — Run the selected skills in this order

1. `sonicg-review-architecture`
2. `sonicg-review-performance`
3. `sonicg-review-security`
4. `sonicg-review-cleanup`

Same change set / project for every skill. Optionally note `release/pre-release` items if the user asked about shipping readiness (do not replace a full pre-release pass).

For shrink/R8 detail (`optimization { enable = true }` on `:app` release, no minify on libraries, unused resources / PNG→WebP): report under **Cleanup** — do not double-number the same Critical under Security. When Cleanup was answered **no**, the R8 release check still reports under Security as a single finding.

## Step 2 — Aggregated report

Number every actionable finding in **one** continuous list across the sections that ran. Sections answered **no** appear as `Skipped (not selected)`. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Overall verdict
Pass / Pass with notes / Fail

## Scope
Architecture: run | skipped · Performance: run | skipped · Security: run | skipped · Cleanup: run | skipped

## Architecture
(summary or Skipped)

## Performance
(summary or Skipped)

## Security
(summary or Skipped)

## Cleanup
(summary or Skipped)

## Fix list
1. [Architecture / Critical] …
2. [Architecture / Warning] …
3. [Performance / Critical] …
4. [Security / High] …
5. [Cleanup / Critical] …
6. [Optional] …

## Must-fix before merge
(reference Fix list numbers that are Critical / High)

## Optional follow-ups
(reference remaining Fix list numbers)
```

**Fail** if any selected skill reports a **Critical** finding (module boundary break, Main-thread ANR risk, leaked secret, unsafe exported component, missing `:app` release `optimization { enable = true }`, library wrongly enabling minify/shrink, clear size-critical asset issues, **cold-start missing Koin definition** for App/MainActivity/Startup/Splash inject graph). A verdict covers only the selected dimensions — say so in **Scope**.

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
