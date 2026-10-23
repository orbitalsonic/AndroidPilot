---
name: sonicg-add-admob-rewarded
description: OrbitalSonic Gemini Skill: Add an AdMob rewarded placement to an existing :gmaAds app (AdKey, AdConfig, gmaAds resValue units, RC, load/show with granted). Use when adding rewarded ads, or @sonicg-add-admob-rewarded. If ads were never wired, use sonicg-implement-admob-ads first.
---

# Add AdMob Rewarded

Follow `.agents/rules/21-ads-billing.md` and [reference/ads-gma.md](../rules/reference/ads-gma.md) (Rewarded section).

Source module: [orbitalsonic/Admob-Ads](https://github.com/orbitalsonic/Admob-Ads) (`gmaAds`) — engine is placed from GitHub; this skill only adds a placement.

Obey `.agents/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing or screens were never wired → redirect to **`sonicg-implement-admob-ads`**.

Do **not** edit `RewardedAds`, `RewardedAdController`, or `RewardedAdValidator` unless changing engine behavior.

---

## Steps

1. Ask: placement name, load screen, show trigger (button), fallback flags.
2. Add `RewardedAdKey` enum value.
3. Add matching row in `RewardedAdConfig.placements`.
4. Add `resValue("string", "admob_…_id", "…")` in **both** `debug` and `release` of `gmaAds/build.gradle.kts` (same name; Google sample in debug / production in release).
5. Add RC in `:data`. `isEnabled = { it.rcFlag != 0 }`.
6. Calls:

```kotlin
loadRewardedAd(RewardedAdKey.YOURS)
showRewardedAd(RewardedAdKey.YOURS) { granted ->
    if (granted) { /* grant reward */
    }
}
```

Always check `granted` — failed/skipped show reports `false`.

7. Compose: same from host Activity/Fragment. No raw AdMob in composables. No `AdsManager` injection.

## Remove

Delete load/show → config + key → `resValue` lines (debug + release) → RC → search leftovers.
