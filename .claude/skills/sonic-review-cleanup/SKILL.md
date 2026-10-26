---
name: sonic-review-cleanup
description: OrbitalSonic Claude Skill: Review unused code/drawables, APK/AAB size (PNG→WebP, vectors), and release R8/optimization on :app only. Use for cleanup, dead resources, or app-size suggestions. Prefer sonic-review for a full multi-check pass.
---

# Cleanup / App Size Review

Follow `.claude/rules/14-security-secrets.md`, `09-resources-xml.md`, `reference/resources-xml.md`, `sonic-gradle-organize` / `sonic-gradle-update` R8 notes.

Obey `.claude/project-settings.json` when present.

**Report only.** Do not delete assets, convert PNG→WebP, or change Gradle in the review turn — list findings, then ask which numbers to fix per [fix-selection.md](../fix-selection.md).

When run inside **`sonic-review`**: report detailed shrink/module findings under **Cleanup** only (do not duplicate the same Critical as Security).

## Module suitability (mandatory)

| Module | What belongs | What does **not** |
|--------|----------------|-------------------|
| **`:app` `release`** | `optimization { enable = true }` — code **and** resource shrinking together | Legacy `isMinifyEnabled` / `isShrinkResources` / `proguardFiles` / `proguard-rules.pro` |
| **`:app` `debug`** | No R8 (unminified; `applicationIdSuffix` OK) | `optimization.enable` / minify |
| **Library modules** (`:domain`, `:data`, `:core-*`, `:feature-*`, `:presentation`, …) | `src/main/keepRules/*.keep` when reflection/serialization needs keeps for the **app** R8 pass | `buildTypes` with `optimization.enable`, `isMinifyEnabled`, `isShrinkResources`, `proguardFiles` |
| **Any module** | Keep rules only under `src/main/keepRules/*.keep` | `proguard-rules.pro` / `consumer-rules.pro` |

Do **not** enable minify/shrink/optimization on every module — only `:app` release.

### Release shrink config

- [ ] `:app` `release` has `optimization { enable = true }`
- [ ] `:app` `debug` has **no** R8 / `optimization.enable`
- [ ] No library module enables minify / shrink / `optimization.enable`
- [ ] Keep rules live in `src/main/keepRules/*.keep` only (app + libraries that need them)
- [ ] No `proguardFiles` / `proguard-rules.pro` / `consumer-rules.pro`

Findings:
- **Critical** — `:app` release lacks `optimization { enable = true }` (and has no acceptable equivalent)
- **Warning** — legacy `isMinifyEnabled = true` + `isShrinkResources = true` on `:app` release → migrate to `optimization { enable = true }` (do not keep both old and new)
- **Critical** — a **library** enables minify/shrink/optimization → remove; rely on app R8 + keep rules

## Unused code

- [ ] Unused classes / files / dead feature packages (search references)
- [ ] Unused Koin registrations / factories with no consumers
- [ ] Obsolete imports and clearly dead private APIs
- Mark uncertain hits (reflection, string class names, multi-module) as **Warning**, not Critical

## Unused resources

- [ ] Drawables / layouts / anim / raw / menus with no `@` / `R.` / Compose `painterResource` / `R.drawable` references
- [ ] Strings / colors / dimens unused across modules (shared `:core-ui` strings — be careful of future screens)
- [ ] If `getIdentifier` / dynamic resource names exist → **do not** suggest delete as Critical; note as Warning / investigate

## App size

- [ ] Large PNG/JPEG → suggest WebP (lossy/lossless as appropriate)
- [ ] Prefer vector (`ic_svg_*`) for icons over raster/WebP
- [ ] Skip / do not nag: launcher icons if product policy forbids change, `.9.png`, already-vector assets
- [ ] Duplicate density folders bloating the same asset; oversized fonts / `raw`
- [ ] Note: R8 resource shrinking only strips **unreferenced** resources — unused-resource cleanup still matters

Size findings:
- **Critical** — very large raster in release path with clear WebP/vector replacement
- **Suggestion** — smaller PNG→WebP under a reasonable threshold; optional compressions

## Dependencies

- [ ] Catalog deps / project modules with no usage → **Suggestion** unless clearly dead (**Warning**)

## Report format

Number every actionable finding. Follow [fix-selection.md](../fix-selection.md) after the report.

```markdown
## Fix list
| # | Issue | Location | Severity | Fix |
|---|-------|----------|----------|-----|
| 1 | :app release missing optimization.enable | `app/build.gradle.kts` | Critical | Add optimization { enable = true } |
| 2 | Unused drawable | `core-ui/.../ic_old.png` | Warning | Delete if confirmed unused |
| 3 | Large PNG → WebP | `core-ui/.../hero.png` (~800KB) | Suggestion | Convert to WebP |
```

After the report: **do not fix yet** — ask which numbers to fix per `fix-selection.md` (e.g. user replies `fix 1, 2, 4, 7`).
