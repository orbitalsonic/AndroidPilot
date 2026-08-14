---
name: android-build-config
description: Gradle and build configuration rules for this template — AGP 9 / Gradle 9 DSL (built-in Kotlin, compileSdk block, optimization block instead of minify/proguard), the gradle/libs.versions.toml version catalog, R8 keep rules in keepRules/*.keep, gradle.properties defaults, and manifest registration. Use when editing build.gradle.kts, settings.gradle.kts, gradle.properties, the version catalog, keep rules, or when adding any dependency or plugin.
---

# Build Configuration (AGP 9 / Gradle 9)

## SDK and Java levels

- `minSdk = 24`, `targetSdk = 37`, Java source/target compatibility 17.
- `compileSdk` uses the AGP 9 block: `compileSdk { version = release(37) }`.

## Kotlin is built into AGP 9

- The `org.jetbrains.kotlin.android` plugin must **NOT** be applied — applying it fails the build.
- Kotlin compiler plugins that are still needed (parcelize, safeargs) are applied normally.
- Never add `kotlin { jvmToolchain(...) }` or `kotlinOptions { }`. Java levels come from
  `compileOptions`; Kotlin follows them.

## Shrinking uses the `optimization` DSL

Not `isMinifyEnabled` / `isShrinkResources` / `proguardFiles`:

```kotlin
buildTypes {
    release { optimization { enable = true; keepRules { includeDefault = true } } }
    debug   { optimization { enable = false } }
}
```

R8 keep rules live in `app/src/main/keepRules/*.keep` — the extension **must** be `.keep`. There is
no `proguard-rules.pro` any more. Add keep rules for reflection, Gson models, and Firebase.

## gradle.properties

Keeps AGP 9 defaults: built-in Kotlin, new DSL, configuration cache on. Do not re-add
`android.builtInKotlin=false` or `android.newDsl=false`.

## Dependencies

- Declare every dependency and plugin in `gradle/libs.versions.toml`, reference it as `libs.*`.
- Never hardcode a version or a coordinate string inside `build.gradle.kts`.
- Never leave an applied-but-unused plugin or an unused catalog entry behind.
- Prefer a helper that already exists in the template over a new library.
- Never introduce a second DI framework, image loader, or networking stack.

## Registration

- Every new Activity goes in `AndroidManifest.xml`.
- Every new destination goes in the correct `*_nav_graph.xml`.
- Every permission is declared in the manifest with the right `maxSdkVersion` / feature flags.

## Upgrades

- Change versions in `gradle/libs.versions.toml` only; upgrade one group at a time and build between steps.
- AGP upgrades: bump the Gradle wrapper with it (including `distributionSha256Sum`) and check whether
  the DSL moved again.
- A KSP-based library (Room, Glide's generated API, …) needs a KSP version compatible with AGP's
  built-in Kotlin — verify it builds before committing to it.
- A `targetSdk` bump is a behaviour change, not a version number. See the `android-upgrade` skill.

## Verify

`gradlew clean assembleDebug`, plus `gradlew assembleRelease` when R8-sensitive code changed.
If you cannot run the build, say so explicitly rather than claiming it passes.
