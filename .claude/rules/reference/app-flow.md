---
description: Full detail for 29–32 screen rules — StartupActivity funnel vs MainActivity, routing, prefs, completion writes
paths:
  - "**/startup/**"
  - "**/Startup*"
  - "**/nav_graph_*.xml"
  - "**/MainActivity.kt"
  - "**/StartupActivity.kt"
---

# App flow — two Activities, first-time vs returning user

Companion to screen rules `29`–`33` and SharedPreferences patterns (`reference/shared-preferences.md`). Reference app: QiblaCompass.

## Activities

```
StartupActivity (launcher, Theme.App.Starting, nav_graph_startup)
  startupFragment ──► startupLanguageFragment ──► [startupPremiumFragment(isFirstTime=true)] ──► startupIntroFragment ──► MainActivity
        │
        └──(returning)──► [startupWelcomeBackFragment] ──► [startupPremiumFragment(isFirstTime=false)] ──► MainActivity

MainActivity (nav_graph_main: Home tabs + LanguageFragment / PremiumFragment / SettingsFragment / ExitFragment …)
```

`StartupActivity.moveToMain()` = `startActivity(Intent(this, MainActivity::class.java))` + `finish()`. `MainActivity` never navigates back to a startup screen.

## Prefs (typical names — match demo / product)

| Key | Role |
|-----|------|
| `isFirstTime` | `true` until onboarding (`StartupIntroFragment`) completes |
| `isLanguageCompleted` | Startup language finished |
| `isOnBoardingCompleted` | Onboarding finished |
| `isAppPurchased` | Entitlement (subs + lifetime) — skips ads, Startup Welcome Back, Startup Premium |
| `showPremiumFirstTime` / `showPremiumSecondTime` | Remote Config ints cached in prefs (`0` = off) |

## Routing (`StartupFragment`)

```
if (isAppPurchased && !isFirstTime) → MainActivity              // skip Startup Welcome Back / Startup Premium; no ads
else if (!isFirstTime) →
    if (welcome-back native loaded) → StartupWelcomeBack                     // returning, not purchased
    else if (showPremiumSecondTime != 0 && offersLoaded) → StartupPremium(isFirstTime = false)
    else → MainActivity
else if (!isLanguageCompleted) → StartupLanguage                // first-time, purchased or not
else → StartupIntro                                             // language done, onboarding pending
```

Then:

```
StartupLanguage Next   → (showPremiumFirstTime != 0 && !isAppPurchased && offersLoaded) ? StartupPremium(true) : StartupIntro
StartupPremium close   → isFirstTime ? StartupIntro : MainActivity
StartupPremium bought  → MainActivity
StartupWelcomeBack Home       → (showPremiumSecondTime != 0 && !isAppPurchased && offersLoaded) ? StartupPremium(false) : MainActivity
StartupIntro finished  → MainActivity
```

Marketing may reorder steps — ask before inventing a new funnel.

## Completion writes

- Startup Language Next → `isLanguageCompleted = true` (+ persist selected locale, apply locale)
- Onboarding done → `isOnBoardingCompleted = true` and `isFirstTime = false`
- Purchase success (either paywall) → `isAppPurchased = true` (+ reset interstitial counters)

## In-app twins (`MainActivity`)

| Startup screen | In-app screen | Difference |
|----------------|---------------|------------|
| `StartupLanguageFragment` | `LanguageFragment` | In-app has back + **Apply** + **search** + nothing-found state; startup has **Next** only (`30`) |
| `StartupPremiumFragment` | `PremiumFragment` | In-app closes with `popFrom`; purchase restarts `MainActivity` (`33`) |

## Screen start pattern

Prefer:

```kotlin
private fun screenStarted() = launchWhenResumed {
    viewModel.handleIntent(XxxIntent.ScreenStarted)
}
```

when work must not run before resume (paywall close delay, RC, etc.).
