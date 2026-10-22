# App flow — first-time vs returning user

Companion to screen rules `29`–`32` and SharedPreferences patterns (`reference/shared-preferences.md`).

## Prefs (typical names — match demo / product)

| Key | Role |
|-----|------|
| `isFirstTime` | `true` until onboarding completes |
| `isLanguageCompleted` | Language funnel finished |
| `isOnBoardingCompleted` | Onboarding finished |

## Routing (Entrance)

```
if (isAppPurchased && !isFirstTime) → Dashboard   // skip Welcome Back; no ads
else if (!isFirstTime) → WelcomeBack              // returning, not purchased
else if (!isLanguageCompleted) → Language         // first-time, purchased or not
else → OnBoarding                                 // language done, onboarding pending
```

Marketing may reorder steps — ask before inventing a new funnel.

## Completion writes

- Language done → `isLanguageCompleted = true` (+ persist selected locale)
- Onboarding done → `isOnBoardingCompleted = true` and `isFirstTime = false`

## Screen start pattern

Prefer:

```kotlin
private fun screenStarted() = launchWhenResumed {
    viewModel.handleIntent(XxxIntent.ScreenStarted)
}
```

when work must not run before resume (paywall close delay, RC, etc.).
