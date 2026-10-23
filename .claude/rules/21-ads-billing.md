---
description: AdMob :gmaAds and in-app billing / premium patterns
paths:
  - "**/gmaAds/**"
  - "**/admob*/**"
  - "**/billing/**"
  - "**/premium/**"
  - "**/*.gradle.kts"
---

Apply when the project includes ads and/or IAP. Setup skills **always place** `:gmaAds` from GitHub; screen wiring needs user yes (or `/sonicc-implement-admob-ads`).

## Ads are not MVI

- Ads stay on **`:gmaAds`** (facade + controllers + Fragment/Activity extensions) — **not** Intent / State / Effect
- Do **not** apply `sonicc-create-mvi` to ads; do not inject `AdsManager` in screens
- Screens call extensions (`loadBannerAd`, `showInterstitialAd`, …) with placement keys
- Convert ads to MVI **only** if the user **explicitly** asks

Full playbook: [reference/ads-gma.md](reference/ads-gma.md)

Skills: `sonicc-implement-admob-ads`, `sonicc-add-admob-banner`, `sonicc-add-admob-interstitial`, `sonicc-add-admob-native`, `sonicc-add-admob-rewarded`, `sonicc-add-admob-rewarded-interstitial`, `sonicc-add-admob-appOpen-Entrance`, `sonicc-add-admob-appOpen-lifecycle`

## Placing `:gmaAds`

1. Download from [orbitalsonic/Admob-Ads](https://github.com/orbitalsonic/Admob-Ads) — exact module copy
2. Package / namespace only: `{applicationId}.gmaAds`
3. Remap Gradle + host imports (`Constants.TAG_ADS`, `InternetManager`, `SharedPrefManager`, extensions) to this app’s `:core-common` / `:core-platform` / `:data`
4. During place: **do not** edit engine or catalog (controllers, validators, `AdsSdk`, `FullscreenAdGate`, `ConsentManager`, `*AdConfig`)
5. Later placements may edit catalog + `:data` RC — leave engine alone unless changing engine behavior

## Ads usage invariants

- Gate load/show on **premium** + **Remote Config** ints in `SharedPrefManager` (placements read prefs, not live Firebase)
- `StartupFragment` with ads (`:gmaAds` key `ENTRANCE`): start Remote Config and ignore its result. Leave after consent (8s) → AdMob init → ad calls (8s). Do not leave on the Remote Config result. Without ads, leave when Remote Config returns or after 5s. The first two confirmed placements default on (`1`); later ones default `0`
- Debug = Google sample unit IDs; release = production — both via `resValue` in `gmaAds/build.gradle.kts` (`buildFeatures.resValues = true`); App ID same way — not hardcoded in Kotlin; **no** `ad_ids.xml`
- New placement = `*AdKey` + `*AdConfig` row + `resValue` unit IDs in `gmaAds/build.gradle.kts` (debug + release) + RC in `:data` + screen load/show — **not** controller/validator edits
- Compose: same strategy via host Activity/Fragment extensions — no raw AdMob in composables
- Feature screens remain MVI; ads calls stay outside Intent/State/Effect ownership

## Billing / premium

- Billing manager lives behind a domain `BillingRepository` (impl in `:data`)
- Persist entitlement (e.g. `isAppPurchased`) in SharedPreferences / DataStore via repository
- When `isAppPurchased` becomes true on Startup, cancel splash ads. First-time users still open Startup Language / Startup Intro. Returning users open `MainActivity` and skip Startup Welcome Back / Startup Premium
- Two paywalls: `StartupPremiumFragment` (`presentation/startup/`, `isFirstTime` arg) and in-app `PremiumFragment` (`presentation/premium/`) — `33-screen-premium`
- Product IDs: constants in data/domain — not duplicated in UI
- After purchase success: update entitlement, then let ads/UI react
- Library: [orbitalsonic/SonicInApp](https://github.com/orbitalsonic/SonicInApp) via JitPack (`com.github.orbitalsonic:SonicInApp`)
- Full subs + in-app playbook: [reference/premium-billing.md](reference/premium-billing.md)
- Skills: `sonicc-implement-in-app-billing`, `sonicc-add-subscription-packages`, `sonicc-add-inapp-packages`

## Forbidden

- Loading ads when user is premium (unless product says otherwise)
- Production ad IDs in debug builds
- Calling BillingClient / MobileAds directly from random Fragments when `:gmaAds` / managers exist
- Hardcoding secrets or keystore passwords next to billing setup
- Converting ads to MVI unless the user explicitly asks
- Inventing a parallel ads stack instead of copying `:gmaAds` from GitHub
- Editing engine files when only adding a placement
