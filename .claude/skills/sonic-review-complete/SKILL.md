---
name: sonic-review-complete
description: OrbitalSonic Claude Skill: Run full project/PR review — architecture, performance, security, and cleanup/app-size — into one Pass/Fail report. Use when asking for complete review, review-*, or pre-merge quality gate.
---

# Complete Review

Run these skills in order on the same change set / project:

1. **`sonic-review-architecture`**
2. **`sonic-review-performance`**
3. **`sonic-review-security`**
4. **`sonic-review-cleanup`**

Optionally note `release/pre-release` items if the user asked about shipping readiness (do not replace a full pre-release pass).

Obey `.claude/project-settings.json` when present.

For shrink/R8 detail (`optimization { enable = true }` on `:app` release, no minify on libraries, unused resources / PNG→WebP): report under **Cleanup** — do not double-number the same Critical under Security.

## Aggregated report

Number every actionable finding in **one** continuous list across all sections. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Overall verdict
Pass / Pass with notes / Fail

## Architecture
(summary)

## Performance
(summary)

## Security
(summary)

## Cleanup
(summary)

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

**Fail** if any skill reports a **Critical** finding (module boundary break, Main-thread ANR risk, leaked secret, unsafe exported component, missing `:app` release `optimization { enable = true }`, library wrongly enabling minify/shrink, clear size-critical asset issues, **cold-start missing Koin definition** for App/MainActivity/Startup/Splash inject graph).

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
