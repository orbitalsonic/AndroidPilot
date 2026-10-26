---
name: sonic-add-admob-appOpen-Entrance
description: OrbitalSonic Claude Skill: Wire AdMob App Open ENTRANCE placement (consent, blockAppOpen, load with interstitial, showAppOpenOrInterstitialAd). Use for splash/startup app open, or /sonic-add-admob-appOpen-Entrance. If ads were never wired, use sonic-implement-admob-ads first. For resume/lifecycle app open use sonic-add-admob-appOpen-lifecycle.
---

# Add AdMob App Open — Startup

Follow `.claude/rules/21-ads-billing.md` and [reference/ads-gma.md](../rules/reference/ads-gma.md) (App Open section).

Source module: [orbitalsonic/Admob-Ads](https://github.com/orbitalsonic/Admob-Ads) (`gmaAds`) — engine is placed from GitHub; this skill only adds a placement.

Obey `.claude/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or Startup was never wired for ads → prefer **`sonic-implement-admob-ads`**, or continue here only when module + DI already exist.

Do **not** edit `AppOpenAds`, `AppOpenAdController`, `AppOpenAdValidator`, `AppOpenLifecycle`, or `ConsentManager` unless changing engine behavior.

Sibling: `sonic-add-admob-appOpen-lifecycle` for `LIFECYCLE`.

---

## Steps

1. Confirm `StartupFragment` / `StartupScreen` exists (start of `StartupActivity`). If missing → stop; do not create it only for ads. The `:gmaAds` key stays `ENTRANCE` — only the screen is named Startup.
2. Ensure `AppOpenAdKey.ENTRANCE` + catalog row + `resValue` unit IDs in `gmaAds/build.gradle.kts` + RC (`!= 0` on) exist (shipped with module or add via shared placement steps).
3. On Startup start: `blockAppOpen()` so process `ON_START` does not steal the splash ad.
4. Follow `sonic-implement-admob-ads` Startup startup: start Remote Config and ignore its result. Consent max **8 seconds**, then AdMob init, then load inventory inside the ads **8 seconds**. SharedPref default is `1` when this placement is one of the first two confirmed ads. On ads allowed:

```kotlin
loadAppOpenAd(AppOpenAdKey.ENTRANCE) { /* settled */ }
loadInterstitialAd(InterstitialAdKey.ENTRANCE) { /* settled */ }
// optional: preload natives for Language / next screens
```

5. Still inside the ads 8 seconds, before navigate to Language / Onboarding / Menu (do not navigate from Remote Config):

```kotlin
showAppOpenOrInterstitialAd(AppOpenAdKey.ENTRANCE, InterstitialAdKey.ENTRANCE) {
    // navigate
}
```

App open shows first; interstitial only if app open did not display. This clears the block when appropriate — keep blocked through splash until this show path.

6. Do **not** call `unblockAppOpen()` anywhere in `StartupActivity` — `MainActivity` does that (lifecycle skill).
7. Compose: same ordering from the host Activity/Fragment. No `AdsManager` injection.

## Remove

Drop startup load/show; Startup may `showInterstitialAd` only or navigate with no fullscreen. If removing the key entirely, follow shared remove steps in `ads-gma.md`.
