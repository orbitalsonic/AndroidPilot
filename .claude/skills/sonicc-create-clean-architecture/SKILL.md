---
name: sonicc-create-clean-architecture
description: OrbitalSonic Claude Skill: Add domain, data, and core pieces for a feature (UseCases, repository interfaces/impls, DataSources, DI). No UI (no Fragment/XML/Compose Screen). Use when a screen needs new business/data capability, or after sonicc-create-mvi when domain is missing. For full UI+MVI+domain in one invoke use sonicc-create-screen. Follows Retrofit/Room/SharedPreferences rules references.
---

# Create Clean Architecture Layers

Follow `.claude/rules/` — especially `02-project-structure`, `03-android-architecture`, `06-coroutines-flow`, `07-dependency-injection`, `18-errors-result`, `26-data-persistence`, and:

- `.claude/rules/reference/retrofit.md`
- `.claude/rules/reference/room.md`
- `.claude/rules/reference/shared-preferences.md`

Obey `.claude/project-settings.json` when present (`writeTestsWithFeatures`).

**Does not** create UI or Intent-State-Effect — use `sonicc-figma-to-xml` or `sonicc-figma-to-compose` + `sonicc-create-mvi` for those. Presentation / `:feature-*` must **not** depend on `:data`.

## Preconditions

- Confirm feature / area name (e.g. `user`, `premium`, `remoteConfig`)
- Confirm which pieces are needed: remote API / Room cache / SharedPreferences / pure domain UseCase over existing repo
- Optional: screen or Figma refs for context only (do not implement UI here)
- **Add Retrofit / Room / new libs only with human approval** — catalog first
- When adding **Room**: create **`:core-database`** (`entity/`, DAOs, `AppDatabase`, Koin module per `07`); `include` alphabetically; `:data` depends on it (`26`, `reference/room.md`)
- When adding **Retrofit/API**: create **`:core-network`**; connectivity stays in `:core-platform` (`reference/retrofit.md`)
- Do **not** rebuild `:gmaAds` as a new domain/data MVI feature unless the user **explicitly** asks. Ads keep their existing module structure (`21-ads-billing`)

## Create only what’s missing

```
domain/
  repository/<area>/<Name>Repository.kt     # interface ONLY
  usecase/<area>/<Name>UseCase.kt           # UseCase ONLY
  entity/…                                  # pure Kotlin domain models
  di/UseCaseModule.kt                       # module/lazyModule { factory { … } } per 07

data/
  <area>/dataSource/…                       # DataSource / manager / Api / Dao wrapper
  <area>/repository/<Name>RepositoryImpl.kt # impl ONLY
  <area>/dto/…                              # network DTOs (data only) — Room entities live in :core-database
  di/DataModule.kt                          # module/lazyModule { //// DataSources … //// Repositories … } per 07

core-* (only if required)
  :core-database — Room AppDatabase / entity/ / DAOs when adding Room
  :core-network — OkHttp/Retrofit when adding API clients
  :core-platform — InternetManager / connectivity (not full Retrofit stack)
  :core-common — shared Constants
```

- **Never** create UseCase or repository interface under `:data`
- Presentation / `:feature-*` must **not** depend on `:data`
- DataSources: **no** dispatcher parameter; Repository wraps with `withContext(ioDispatcher)`

## Pattern selection

| Need                            | Follow                                                     |
|---------------------------------|------------------------------------------------------------|
| REST / HTTP                     | `reference/retrofit.md` + `26-data-persistence`            |
| Local DB / cache                | `reference/room.md` + `26-data-persistence`                |
| Flags / settings / RC cache     | `reference/shared-preferences.md` + `22-platform-firebase` |
| Existing repo, new UseCase only | Domain UseCase + `useCaseModule` factory                   |

## DI (`module` if cold-start; else `lazyModule`)

```kotlin
// Used at Entrance / MainActivity → module { } on eager list
// Feature-only → lazyModule { } on lazy list
val useCaseModule = module {

    //// Feature
    factory { GetFeatureUseCase(get()) }
}

val dataModule = module {

    //// DataSources
    single { FeatureDataSource() }

    //// Repositories
    single<FeatureRepository> { FeatureRepositoryImpl(get(), get()) }
}
```

Register **all** new modules/vals in app composition root (`KoinModules` — eager or lazy list per `07`). Use `//// Section` headers. If anything is used from **Entrance / Splash / MainActivity** cold start, the full ctor chain must be on the **eager** list (`23`, `sonicc-review-architecture`).

## Errors & logging

- Map failures at data boundary to typed `DataError` / `Outcome` (`18-errors-result`)
- Log in repository: `Constants.TAG*` — `ClassName: functionName: State: details`
- No tokens / PII in logs

## Tests

- If `writeTestsWithFeatures` is `true`: UseCase unit test with `Fake*Repository` (`sonicc-test-unit`)
- If `false`: skip unless user asks

## Verify

- [ ] Interfaces + UseCases only in `:domain`
- [ ] Impls + DataSources only in `:data`
- [ ] `dataModule`: `//// DataSources` then `//// Repositories`
- [ ] No presentation / `:feature-*` → data dependency introduced
- [ ] Dispatchers only in repository impls
- [ ] New libraries approved + in version catalog
- [ ] Tell user to wire presentation via `sonicc-create-mvi` if UI not done yet
