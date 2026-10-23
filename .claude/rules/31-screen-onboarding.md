---
description: Onboarding (StartupIntroFragment) conventions — ViewPager2 dots slider, media, ads, hand-off to MainActivity
paths:
  - "**/onBoarding/**"
  - "**/onboarding/**"
  - "**/StartupIntro*"
  - "**/OnBoardingFragment*"
  - "**/OnBoardingScreen*"
---

# Onboarding screen (`StartupIntroFragment`)

Lives in `StartupActivity` (`nav_graph_startup`), package `presentation/startup/onboarding/`: `StartupIntroFragment` + `fragment_startup_intro.xml`, `StartupIntroPagerAdapter` (`FragmentStateAdapter`), page fragments / placeholder layout, `StartupIntroViewModel` (page index in State). Compose: `StartupIntroScreen` in `:feature-startup`.

## Always

- Use a **dots / page-indicator slider library** (existing catalog / approved dependency — do not invent a one-off indicator)
- Content typically includes: title, body, **Next / Get started** button (text switches on the last page), ads (if wired), and **media** (image, video, **or** Lottie)
- Helper order: `setupViewPager()` then `screenStarted()`

## Flow

- Entered from `StartupLanguageFragment` (or from `StartupPremiumFragment` close when `isFirstTime`)
- Completing onboarding sets `isOnBoardingCompleted` and clears first-time (`isFirstTime = false`) via repository/UseCase, then `(activity as? StartupActivity)?.moveToMain()` (interstitial first when wired)
- No nav action to Home — `MainActivity` is a separate Activity
- Back press blocked by `StartupActivity`

## Ads

- Load/show via `:gmaAds` extensions only — not MVI Intents for ad inventory (`21-ads-billing`)
- Reuse the startup-language native when it is still loaded (`replace` into the onboarding key) before loading a new one

## Forbidden

- Onboarding without a page indicator when multi-page
- Raw AdMob SDK calls in the Fragment
- Hosting onboarding inside `MainActivity`
