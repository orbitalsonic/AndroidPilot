# Retrofit API (reference pattern)

Follow `.agents/rules/08-gradle.mdc`, `13-libraries-stack.mdc`, `06-coroutines-flow.mdc`, `18-errors-result.mdc`, `22-platform-firebase.mdc`, `26-data-persistence.mdc`. **Add Retrofit/OkHttp/Moshi only with approval** — catalog first.

Prefer existing `InternetManager` (`:core-platform`) for connectivity checks before calls.

## Layers

| Layer / module | Contents |
|----------------|----------|
| `:core-network` | OkHttp client, Retrofit builder, interceptors, base URL config, `ApiService` interfaces — **create when adding Retrofit/API** |
| `:data` | DTOs (if not in network), `RemoteDataSource`, `*RepositoryImpl` |
| `:domain` | Domain models + repository interface — no Retrofit types |
| `:core-platform` | Connectivity only (`InternetManager`) — not the full Retrofit stack |

- Do **not** create an empty `:core-network` on every `hsg-setup-new-project` — add when networking API is approved
- Prefer existing `InternetManager` (`:core-platform`) for connectivity checks before calls

## Setup

1. `libs.versions.toml`: retrofit, okhttp, converter (moshi/gson)
2. Create `:core-network`; `include` alphabetically; wire `lazyModule`
3. `:data` depends on `:core-network`
4. Base URL from build config / `local.properties` — not hardcoded secrets

```kotlin
interface UserApi {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto
}
```

## Error handling

- Map HTTP/IO errors at data boundary to typed `DataError` / `Outcome` (`18-errors-result`)
- Never leak status codes or raw JSON bodies into UI State
- Log: `Constants.TAG` — `UserRepositoryImpl: getUser: Failed: ${e.message}`

## Dispatchers

- All network on `IO` in **Repository** (`withContext`); DataSource stays thin
- Repository returns domain models or typed failures

## DI (`lazyModule` only)

```kotlin
val dataModule = lazyModule {

    //// DataSources
    single { provideRetrofit(get()) }
    single { get<Retrofit>().create(UserApi::class.java) }

    //// Repositories
    single<UserRepository> { UserRepositoryImpl(get(), get(), get()) }
}
```

- `UserRepository` interface + UseCases in `:domain` (`useCaseModule`)
- Never put interfaces/UseCases in `:data`

## Forbidden

- Retrofit interfaces in domain/presentation
- Blocking calls on Main
- Logging auth tokens or full PII
- New networking stack if project already has one — extend existing client
