---
description: Full detail for 26-data-persistence (Room) — :core-database, entities, DAOs, repository mapping, DI
paths:
  - "**/data/**/*.kt"
  - "**/Room*.kt"
  - "**/*Dao*.kt"
  - "**/*Entity*.kt"
  - "**/database/**"
  - "**/core-database/**"
---

# Room Cache (reference pattern)

Follow `.claude/rules/08-gradle.md`, `13-libraries-stack.md`, `06-coroutines-flow.md`, `03-android-architecture.md`, `26-data-persistence.md`. **Add Room only with human approval** — add to `libs.versions.toml` first.

## Layers

| Layer / module | Contents |
|----------------|----------|
| `:core-database` | `@Entity` (under `entity/`), `@Dao`, `AppDatabase`, `di` Koin module per `07` — **create this module when adding Room** |
| `:data` | `*DataSource` wrapping DAOs, `*RepositoryImpl`, maps Entity ↔ domain |
| `:domain` | Pure Kotlin entities + repository interface |

- Room types stay in `:core-database` / data boundary — not in domain or presentation
- Do **not** create an empty `:core-database` on every `sonic-setup-new-project` — add when Room is approved

## Setup

1. Catalog: `room-runtime`, `room-ktx`, `ksp` room compiler
2. Create `:core-database` module; `include` alphabetically in `settings.gradle.kts`
3. `:data` depends on `:core-database`
4. Database class — version migrations planned from day one

```
core/database/
  AppDatabase.kt
  di/DatabaseModule.kt
  entity/YourEntity.kt
  <feature>/YourDao.kt
  keepRules/rules.keep
```

```kotlin
@Database(entities = [ItemEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
```

## DAO rules

- Suspend functions or `Flow` for queries — no blocking on Main
- Large list reads: map off Main in repository (`flowOn(IO)`)
- Pagination: `LIMIT`/`OFFSET` or Paging 3 if approved

## Repository

- Orchestrate DAO + network DataSource (cache-first / network-first per feature)
- **Inject `ioDispatcher` in Repository** — wrap all DataSource/DAO calls with `withContext(ioDispatcher)`
- DataSource classes: no dispatcher parameter
- DTO → entity mapping in data layer
- Expose `Flow<List<DomainEntity>>` or suspend APIs to domain

## DI (`module` if cold-start; else `lazyModule`)

Room used by Startup / first screen → **`module`**. Feature-only DB → **`lazyModule`**. See `07`.

```kotlin
val dataModule = module {

    //// DataSources
    single { provideDatabase(get()) }
    single { get<AppDatabase>().itemDao() }

    //// Repositories
    single<ItemRepository> { ItemRepositoryImpl(get(), get(), get()) }
}
```

- `ItemRepository` interface + UseCases in `:domain` (`useCaseModule`)
- Never put interfaces/UseCases in `:data`

## R8

- Add keep rules in `src/main/keepRules/*.keep` for entities if release minify breaks Room

## Forbidden

- Room in `:presentation` or `:domain` modules
- `notifyDataSetChanged` for huge cached lists — use ListAdapter + DiffUtil
- Schema changes without migration strategy
