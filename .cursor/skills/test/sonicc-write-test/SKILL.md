---
name: sonicc-write-test
description: Sonic Cursor Skill: Full test pass gate — asks yes/no for unit, integration, and E2E, runs the chosen suites (plus the emulator walkthrough with E2E), reports results, and on failures asks consent before fixing production code and retests. Use for a full test pass, run all tests, or /sonicc-write-test. For one layer only call sonicc-write-unit-test / sonicc-write-integration-test / sonicc-write-e2e-test directly. Requires emulator or physical device for E2E / connected tests and the walkthrough.
---

# Test (orchestrator)

Follow `.cursor/rules/11-testing.mdc`. Obey `.cursor/project-settings.json` when present (`writeTestsWithFeatures`, `orientation`, `themeModes`).

Leaf skills: **`sonicc-write-unit-test`**, **`sonicc-write-integration-test`**, **`sonicc-write-e2e-test`**. They own writing missing tests and their own run/fix loops; this skill selects, runs, and aggregates.

## Step 0 — Yes / no questions (mandatory AskUserQuestion)

Ask these in **one** `AskUserQuestion` call, each as a yes / no question. Default every answer to **yes**.

| # | Question | Runs |
|---|----------|------|
| 1 | Run **unit** tests (JVM: UseCases, ViewModels, Flow with fakes)? | `./gradlew test` + gap check via `sonicc-write-unit-test` |
| 2 | Run **integration** tests (Room in-memory, MockWebServer, multi-layer wiring)? | integration classes under `src/test` (and `androidTest` ones when a device is attached) + gap check via `sonicc-write-integration-test` |
| 3 | Run **E2E** tests on a device (Espresso / `androidTest`) and the emulator walkthrough? | `./gradlew connectedDebugAndroidTest` + Step 3 walkthrough + gap check via `sonicc-write-e2e-test` |

Wait for the answers. If every answer is **no**, stop and say nothing was selected. Skip the questions only when the user already named the layers in the request (e.g. "run unit tests only") — then run exactly those.

## Start banner

First user-visible sentence after the answers (adapt to the selection):

> We are going to run the selected test pass (unit / integration / E2E + walkthrough) — an emulator or physical device is required for E2E / connected `androidTest` and the walkthrough (or we proceed with the device already attached). On failures we will ask before fixing production code.

If E2E was selected and no device is attached: say so after the banner, run the JVM suites that were selected, mark E2E and walkthrough as **blocked** — do not claim they passed.

## Step 1 — Run the selected suites

From project root (Gradle; on Windows PowerShell use `.\gradlew.bat`):

```bash
./gradlew test                          # unit + JVM integration (when selected)
./gradlew connectedDebugAndroidTest     # E2E / androidTest (when selected and a device is attached)
```

- Report pass/fail per module
- Integration tests usually live under `src/test` (covered by `test`); device-side integration (`androidTest`) runs with `connectedDebugAndroidTest`
- When unit is **no** but integration is **yes**, run `test` filtered to the integration classes (`--tests "*IntegrationTest"` or the project's naming) instead of the whole suite

## Step 2 — Coverage gaps (selected layers only)

- Unit gaps (UseCase / ViewModel / Flow-with-fakes) → suggest or invoke `sonicc-write-unit-test`
- Integration gaps (Room / MockWebServer multi-layer) → `sonicc-write-integration-test`
- Critical UX / device flow gaps → `sonicc-write-e2e-test`
- Honor `writeTestsWithFeatures` in `.cursor/project-settings.json`

## Step 3 — Emulator walkthrough (E2E selected)

On an emulator (or device) matching minSdk / target as practical:

- [ ] Cold start → Startup (or start destination) loads
- [ ] Primary nav path(s) reachable without crash
- [ ] Orientation: rotate if `orientation` is `both` or `landscape`
- [ ] Theme: day/night if `themeModes` includes both
- [ ] Permission flows (if any) — deny then grant
- [ ] Offline / no-network graceful path if feature uses network
- [ ] Back stack / predictive back does not break primary flows
- [ ] No obvious ANR / frozen UI on list-heavy screens

Document: device/API, build variant, what was skipped.

## Step 4 — Failures: consent → fix → retest

After Gradle and/or walkthrough failures:

1. **Stop** — present failing tests / walkthrough items + suspected cause
2. **Ask consent** before changing production/app code
3. **On consent** — prefer fixing app code toward production-ready behavior; fix tests only if incorrect/brittle (say so when asking). Then **retest** (repeat Step 1 for the affected layer, and Step 3 if the walkthrough was affected) and report again
4. **If user declines** — leave failures listed; do not change production code
5. Do **not** silently weaken assertions to go green

## Output

```markdown
## Scope
unit: run | skipped · integration: run | skipped · e2e + walkthrough: run | skipped | blocked (no device)

## Automated
- unit: Pass/Fail (details) | Skipped
- integration: Pass/Fail / N/A (details) | Skipped
- e2e (androidTest): Pass/Fail / Blocked (reason) | Skipped

## Walkthrough
- ✅/❌ per checklist item (or Skipped)

## Blockers
- …

## Suggested follow-ups
- sonicc-write-unit-test / sonicc-write-integration-test / sonicc-write-e2e-test items to add

## Fix consent
- Asked / Approved / Declined (if failures)
```

## Limits

- Do not invent production secrets or hit real paid APIs
- Do not claim full coverage if suites were skipped or not selected — say so in **Scope**
- Emulator automation beyond Gradle connected tests is best-effort checklist unless the project already has UI automators approved
- Do not fix production code without explicit user consent after failures
