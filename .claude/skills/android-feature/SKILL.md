---
name: android-feature
description: Workflow for implementing a new Android feature, screen, fragment, activity, dialog, adapter, or enhancement in this XML/MVVM template. Use whenever the user asks to add, build, implement, or extend functionality — before writing any code. Covers planning, the MVVM/MVI skeleton, the Android configuration checklist, and the completion checklist.
---

# Feature Development

Think through the complete solution before writing code. Do not jump into implementation.

## Step 1 — Understand what already exists

- Read the closest existing feature package and copy its shape.
- Reuse existing components (load the `template-helpers` skill for the catalog).
- Avoid duplicate functionality and unnecessary refactoring. Keep changes minimal and focused.
- Never redesign the project unless explicitly requested.

## Step 2 — Plan

Identify before coding:

- screens and layouts affected
- state the screen holds, and events it emits
- data sources: preferences, assets, network, Firebase
- dependencies, permissions, manifest entries
- lifecycle implications and API-level differences
- edge cases: empty, loading, error, offline, denied permission, rotation, process death

Choose the simplest production-ready solution.

## Step 3 — Follow the architecture

MVVM baseline. MVI only for state-heavy features. Never mix both in one feature.

```
presentation/<feature>/
    Fragment<Feature>.kt      // extends BaseFragment<Fragment<Feature>Binding>
    <Feature>ViewModel.kt     // StateFlow state, no Context
    adapters/ interfaces/ models/ repository/
```

```kotlin
class FragmentSample : BaseFragment<FragmentSampleBinding>(FragmentSampleBinding::inflate) {

    private val viewModel: SampleViewModel by viewModels()

    override fun onViewCreated() {
        initViews()
        observeState()
        setupClicks()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }
}
```

Activities extend `BaseActivity<T>(Binding::inflate)`, implement `onCreated()`, and override
`getInsetsMode()` when the default is wrong.

For MVI, add `state/<Feature>UiState.kt` (one immutable data class), `intent/<Feature>Intent.kt`
and `effect/<Feature>Effect.kt` (sealed interfaces). All user actions enter through a single
`onIntent(intent)`. One-shot effects (navigation, toast, dialog) go through `SharedFlow` or `Channel`.

## Step 4 — Reuse before creating

Before writing a repository, utility, extension, mapper, helper, dialog, adapter, fragment, or
activity, search the project for an existing implementation. Prefer extending it over duplicating.

## Step 5 — Android configuration checklist

Decide whether the feature needs:

- runtime permissions / notification permission / exact alarm permission
- Foreground Service, WorkManager, AlarmManager
- BroadcastReceiver, BootReceiver
- Deep Links / App Links / PendingIntent
- FileProvider or ContentProvider
- navigation graph destination
- manifest entry
- R8 keep rules (`app/src/main/keepRules/*.keep`)
- a new catalog dependency (`gradle/libs.versions.toml`)

Do not ignore required configuration.

## Step 6 — Non-negotiables while coding

- **Compatibility**: API 24–37, guarded with `Build.VERSION.SDK_INT`.
- **Lifecycle**: survives recreation, configuration change, process death, and back stack restore.
- **Threading**: coroutines only, heavy work on `Dispatchers.IO`, main thread for UI only, cancel correctly.
- **Errors**: handle invalid input, missing permissions, network/storage failure, timeout,
  cancellation, nulls, unexpected exceptions. Return meaningful UI states. Never leave a TODO.
- **Performance**: no unnecessary allocations, duplicate work, nested layouts, or UI blocking.
  Prefer lazy init, `DiffUtil`, efficient collections, cached results.
- **Preserve architecture**: no Compose, no modularisation, no architecture swap on an existing
  screen, no unnecessary libraries, no package renames.

## Step 7 — Completion checklist

Check whether the change also requires updating:

- `AndroidManifest.xml`
- the navigation graph
- Koin modules (if wired)
- `SharedPrefManager`
- `strings.xml` (and note that translations are pending)
- `colors.xml` **and** `values-night/colors.xml`
- dimens / themes / styles
- permissions, notification channels, receivers, services, workers
- `app/src/main/keepRules/*.keep`
- `gradle/libs.versions.toml`

Update every file the change touches — if several files are affected, do not leave one behind.

## Step 8 — Finish

Run the `android-preflight` skill before reporting the work as done.
