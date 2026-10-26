---
name: sonicc-admob-appOpen-lifecycle
description: Sonic Cursor Skill: Wire AdMob App Open LIFECYCLE placement (MainActivity unblock/preload, AppOpenLifecycle or AppOpenLoadingActivity). Use for resume/foreground app open, or /sonicc-admob-appOpen-lifecycle. For splash startup use sonicc-admob-appOpen-Startup. If ads were never wired, use sonicc-setup-admob-ads first.
---

# Add AdMob App Open — Lifecycle

Follow `.cursor/rules/21-ads-billing.mdc` and [reference/ads-gma.md](../../rules/reference/ads-gma.md) (App Open section).

Source module: [orbitalsonic/Admob-Ads](https://github.com/orbitalsonic/Admob-Ads) (`gmaAds`) — engine is placed from GitHub; this skill only adds a placement.

Obey `.cursor/project-settings.json` (`uiFramework`, `applicationId`).

If `:gmaAds` is missing → redirect to **`sonicc-setup-admob-ads`**.

Do **not** edit `AppOpenAds`, controller, validator, or `AppOpenLifecycle` engine unless changing engine behavior. Catalog / flag only for placement config.

Sibling: `sonicc-admob-appOpen-Startup` for splash `ENTRANCE`.

---

## Steps

1. Ask which mode (`AppOpenAdConfig.LOAD_LIFECYCLE_WITH_LOADING_SCREEN`):

| Flag      | Behavior                                                                                                                 |
|-----------|--------------------------------------------------------------------------------------------------------------------------|
| **true**  | `AppOpenLoadingActivity` loads then shows `LIFECYCLE`. Do **not** preload in `MainActivity` or reload after dismiss.          |
| **false** | Load `LIFECYCLE` in `MainActivity` (home shell); `AppOpenLifecycle` shows on process foreground and reloads after dismiss. |

2. Ensure `AppOpenAdKey.LIFECYCLE` + catalog row + `resValue` unit IDs in `gmaAds/build.gradle.kts` + RC exist.
3. Confirm `AppOpenLifecycle` is registered in `gmaAdsModule` (shipped with module — do not reinvent).
4. In `MainActivity.onCreated()` (home shell) when ads are active:

```kotlin
unblockAppOpen()
// only when LOAD_LIFECYCLE_WITH_LOADING_SCREEN == false:
loadAppOpenAd(AppOpenAdKey.LIFECYCLE)
```

5. Keep App Open blocked through the whole `StartupActivity` funnel (`blockAppOpen` on `StartupFragment`) so lifecycle does not steal the startup ad.
6. Usually extend `LIFECYCLE` rather than adding a third app-open key for resume ads.
7. Compose: call `unblockAppOpen` / load from the host Activity that owns the main shell.

## Remove

Stop `MainActivity` preload / loading-activity usage; if removing `LIFECYCLE` entirely, also stop relying on `AppOpenLifecycle` for show — follow `ads-gma.md` remove steps.
