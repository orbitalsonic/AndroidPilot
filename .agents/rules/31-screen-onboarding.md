---
description: Onboarding screen conventions (dots slider, media, ads)
paths:
  - "**/onBoarding/**"
  - "**/onboarding/**"
  - "**/OnBoardingFragment*"
  - "**/OnBoardingScreen*"
---

# Onboarding screen

## Always

- Use a **dots / page-indicator slider library** (existing catalog / approved dependency — do not invent a one-off indicator)
- Content typically includes: title, body, action buttons, ads (if wired), and **media** (image, video, **or** Lottie)
- Helper order: `setupViewPager()` then `screenStarted()`

## Flow

- Completing onboarding sets `isOnBoardingCompleted` and clears first-time (`isFirstTime = false`) via repository/UseCase
- Nav actions usually `popUpTo` Entrance with `inclusive=true` when leaving the funnel
- MainActivity: block back during first-time onboarding

## Ads

- Load/show via `:gmaAds` extensions only — not MVI Intents for ad inventory (`21-ads-billing`)

## Forbidden

- Onboarding without a page indicator when multi-page
- Raw AdMob SDK calls in the Fragment
