---
name: hsg-add-admob-native
description: Hypersoft Gemini Skill: Add an AdMob native placement to an existing :gmaAds app (AdKey, AdConfig, gmaAds resValue units, RC, container, load/show/destroy). Use when adding native ads, or @hsg-add-admob-native. If ads were never wired, use hsg-implement-admob-ads first.
---

# Add AdMob Native

Follow `.agents/rules/21-ads-billing.md` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (Native section).

Obey `.agents/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or screens were never wired → redirect to **`hsg-implement-admob-ads`**.

Do **not** edit `NativeAds`, `NativeAdController`, or `NativeAdValidator` unless changing engine behavior.

---

## Steps

1. Ask: placement name, host screen(s), preload-elsewhere vs same-screen, `cache` true/false, fallback flags, which view (`NativeSmallView` / `NativeLargeView` / `NativeLargeSecondView`).
2. Add `NativeAdKey` enum value.
3. Add matching row in `NativeAdConfig.placements`.
4. Add `resValue("string", "admob_…_id", "…")` in **both** `debug` and `release` of `gmaAds/build.gradle.kts` (same name; Google sample in debug / production in release).
5. Add RC in `:data` (`rc*` + defaults + repository copy). `isEnabled = { it.rcFlag != 0 }`.
6. UI container:
    - **xml:** add the native view class to the layout
    - **compose:** `AndroidView` hosting the same view class
7. Calls:
    - Preload: `loadNativeAd(key)` on screen A; `showNativeAd(key, container)` on screen B
    - Same screen: `loadNativeAd(key, container)`
    - When `cache = false` and screen is finished: `destroyNativeAd(key)`
8. No `AdsManager` injection; no raw AdMob in composables.

## Remove

Delete load/show/destroy + container → config + key → `resValue` lines (debug + release) → RC → search leftovers.
