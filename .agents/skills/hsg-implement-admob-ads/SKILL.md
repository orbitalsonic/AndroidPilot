---
name: hsg-implement-admob-ads
description: Hypersoft Gemini Skill: First-time AdMob setup with :gmaAds from hypersoftdev/Admob-Ads — place module if missing, discover mappable screens, ask which screens then which placements, wire confirmed only (RC/prefs only for those). Use when implementing ads for the first time, or @hsg-implement-admob-ads. Not for a single new placement — use add-admob-*.
---

# Implement AdMob Ads

Follow `.agents/rules/21-ads-billing.md` and [reference/ads-gma.md](../../rules/reference/ads-gma.md).

Obey `.agents/project-settings.json` when present (`applicationId`, `uiFramework`).

**Reference:** [hypersoftdev/Admob-Ads](https://github.com/hypersoftdev/Admob-Ads) (learn locally at `E:\SohaibAhmed\Github\Admob-Ads`). Copy `:gmaAds` from GitHub — do not invent a parallel stack. Do **not** convert ads to MVI.

Cross-skills: `hsg-add-admob-banner`, `hsg-add-admob-interstitial`, `hsg-add-admob-native`, `hsg-add-admob-rewarded`, `hsg-add-admob-rewarded-interstitial`, `hsg-add-admob-appOpen-Entrance`, `hsg-add-admob-appOpen-lifecycle`. RC keys for a confirmed placement: `hsg-add-firebase-remote-config`. Billing handoff: `hsg-implement-in-app-billing`, `hsg-add-subscription-packages`, `hsg-add-inapp-packages`.

---

## Step 0 — Preconditions

1. Read `applicationId` and `uiFramework` from project settings.
2. If billing/premium exists, confirm ads stay gated on `isAppPurchased` (validators already do this via prefs).
3. If the app already has a different ads stack and the user did **not** ask to migrate → stop and ask before replacing.

---

## Step 1 — Place `:gmaAds` (if missing)

1. Download the `gmaAds` folder from `https://github.com/hypersoftdev/Admob-Ads` (clone/sparse or download zip — prefer GitHub over inventing files).
2. Copy into the project as module `:gmaAds`.
3. `include(":gmaAds")` in `settings.gradle.kts`.
4. Rename package / `namespace` only → `{applicationId}.gmaAds`.
5. Remap Gradle: ref `:core` / `:data` → this app’s `:core-common`, `:core-platform`, `:data` as appropriate; add catalog deps (`play-services-ads`, UMP) if missing.
6. Remap **host-only** imports inside `:gmaAds` to this app: `Constants.TAG_ADS`, `InternetManager`, `SharedPrefManager`, `launchWhenResumed`, `onBackPressedDispatcher`.
7. Register `gmaAdsModule` (`lazyModule`) in the composition root.
8. AdMob App ID via `resValue("string", "admob_app_id", …)` in `gmaAds/build.gradle.kts` (and/or `:app` manifest as shipped). Keep Google sample unit `resValue`s for debug; release uses production (or samples until swapped). Require `buildFeatures.resValues = true`.
9. During place: **do not** edit controllers, validators, `AdsSdk`, `FullscreenAdGate`, `ConsentManager`, or catalog files.

If `:gmaAds` already exists from setup → skip copy; verify package + DI + App ID.

**Do not** scaffold all catalog Remote Config / SharedPreferences keys here. Prefs and RC are added only for placements confirmed in Steps 4–5 (see Step 6).

If place does not compile because shipped catalog `isEnabled` lambdas reference missing `rc*` prefs: set those **unconfirmed** catalog rows to always-off (`isEnabled = { 0 }` or `{ false }`) so they compile **without** inventing unused SharedPrefManager properties. Do not add a full unused RC surface “just in case.”

---

## Step 2 — Discover mappable screens

Scan the project (`uiFramework` from settings):

- **xml:** Fragments / Activities that match ref roles (Entrance, Language, Onboarding, Menu, Dashboard, Home, Trending, Settings, Feature*, …)
- **compose:** `*Screen` / nav destinations with the same roles

For each found screen, detect **already-wired** formats (calls to `loadBannerAd` / `loadNativeAd` / `loadInterstitialAd` / `loadAppOpenAd` / rewarded*, `ConsentManager`, `blockAppOpen`, etc.).

Build a capability table from the ref strategy. Mark each candidate:

| Screen | In project? | Already wired | Ref-recommended formats (still available) |
|--------|-------------|---------------|-------------------------------------------|
| Entrance | yes | none / partial | App Open, Interstitial (+ consent / `blockAppOpen`) |
| Language | yes | banner only | Native (banner already wired) |
| … | | | |

### Ref capability map (ask only formats still unwired)

| Screen (or Compose equivalent) | Ref-recommended formats |
|--------------------------------|-------------------------|
| Entrance | App Open, Interstitial (+ consent / `blockAppOpen`); optional native preload for next screens |
| Language | Banner, Native |
| Onboarding | Banner, Native, Interstitial (on continue) |
| Menu | Native |
| Dashboard | Banner; Lifecycle App Open (`unblockAppOpen` + load when not loading-screen mode); Inter `BOTTOM_NAVIGATION` / `EXIT` |
| Home | Native, Interstitial, Rewarded, Rewarded Interstitial |
| Trending / Settings | Native |
| Feature One / Two | Banner, Native, Interstitial (`BACK_PRESS`) |

**Do not create** missing product screens. Screens not in the project are **omitted** from the ask list (do not list them as “auto-skipped after wire”).

---

## Step 3 — Round 1: pick screens (stop and wait)

Present only screens that **exist** and still have **at least one** unwired recommended format. Ask the developer to choose:

1. Which screens to implement now (multi-select / numbered list), **or**
2. “all recommended”

Example shape:

```
I scanned the project. Mappable screens with unwired ref formats:

1. EntranceFragment — App Open, Interstitial
2. LanguageFragment — Banner, Native
3. OnboardingFragment — Banner, Native, Interstitial
…

Which screens should we wire now? Reply with numbers (e.g. 1, 2) or "all recommended".
```

**Stop.** Do **not** ask placements or wire until the developer replies.

---

## Step 4 — Round 2: pick placements per finalized screen (stop and wait)

For each selected screen, offer choices based on what’s still unwired and what the ref supports. Prefer short lettered/numbered options.

### Entrance (App Open + Interstitial)

- `A` — Interstitial only
- `B` — App Open only
- `C` — Both (`showAppOpenOrInterstitialAd` priority — ref default / recommended)

Any choice also adds consent + `blockAppOpen`.

### Language / Onboarding-style (Banner top + Native bottom)

- `A` — Banner only
- `B` — Native only
- `C` — Both (recommended when both still unwired)

**Onboarding only:** also ask Interstitial on continue — yes / no (when that format is still unwired).

### Dashboard

Ask as **separate toggles** (only for still-unwired formats):

- Banner — yes / no
- Lifecycle App Open — yes / no
- Interstitial exit / bottom-nav — yes / no (match ref keys present)

### Home

Ask each optional format still unwired:

- Native / Interstitial / Rewarded / Rewarded Interstitial — each yes / no

### Single-format screens (Menu, Trending, Settings)

Confirm implement yes / no (usually Native only).

**Stop again** until the developer answers. If answers are incomplete → ask again; **do not** guess “both” or enable formats they did not pick.

---

## Step 5 — Implement confirmed only

For each confirmed `(screen, format)` pair, follow the matching `add-admob-*` skill (do not invent a parallel path):

1. Ensure catalog key + matching `resValue` unit IDs in `gmaAds/build.gradle.kts` (debug + release) + Remote Config exist **for that placement only**.
2. Wire load / show / lifecycle on that screen only.
3. Cross-screen rules:
   - Entrance: `blockAppOpen` when any Entrance fullscreen was chosen
   - Dashboard: `unblockAppOpen` (+ lifecycle load per mode) **only if** lifecycle App Open was chosen
   - Never inject `AdsManager`; never MVI Intent/State/Effect for ads load/show
4. **xml:** Fragment extensions + View Binding containers (`BannerAdView`, `Native*View`).
5. **compose:** same keys and timing; host extensions (or thin wrappers); no raw AdMob SDK in composables; `AndroidView` for banner/native when needed.

### Entrance startup — ignore Remote Config, then navigate

This replaces the `hsg-setup-new-project` 5-second Remote Config navigation gate once Entrance ads are wired.

In `EntranceViewModel` (`ScreenStarted`):

1. Start `FetchRemoteConfigUseCase` and **ignore its result**. Do not navigate from it. The first two ads stay default-on so this splash does not depend on a fetch that can take ~60s.
2. Run the leave-splash sequence **in order**, then navigate:
   - Consent (`ConsentManager` / UMP) — **8 seconds** max
   - AdMob / `AdsSdk` init
   - Ad calls (load, then show for the confirmed entrance formats) — **8 seconds** max
3. **Navigate after that sequence.** Total cap is **16 seconds** (8 + 8). If a phase finishes early, continue. If it hits its cap, continue anyway.

```kotlin
private fun onScreenStarted() {
    viewModelScope.launch {
        fetchRemoteConfigUseCase() // ignored — do not navigate here
    }
    viewModelScope.launch {
        withTimeoutOrNull(8.seconds) { runConsent() }
        withTimeoutOrNull(8.seconds) {
            initAdMob()
            loadAndShowEntranceAds()
        }
        // navigate
    }
}
```

Load/show stay on `:gmaAds` extensions. Do not add an Intent / State / Effect per ad event. `showAppOpenOrInterstitialAd` (when that format was confirmed) is an ad call inside the 8-second ads window, before navigate.

If `isAppPurchased` is already true, or the billing response sets it true during this sequence, cancel the job (consent, init, ad calls) and do not show an ad. A first-time user still continues to Language / Onboarding. A returning user goes to Dashboard and skips Welcome Back (`hsg-implement-in-app-billing`).

### First two ads default on

SharedPref read default is **`1`** for the first **two** confirmed placements in funnel order:

Entrance (App Open, then Interstitial, then any other entrance format) → Language → Onboarding → Menu → Dashboard → Home → Trending / Settings → Feature screens.

Every later confirmed placement stays default **`0`**. If `RemoteConfigDataSource.DEFAULTS` exists, use the same `1` / `0` split. Catalog `isEnabled` stays `{ it.rcFlag != 0 }`. A console value of `0` still turns a placement off after a successful activate; the default only covers the window before that write. Banner TOP/BOTTOM default-on means `1` (adaptive).

```kotlin
// one of the first two confirmed placements
var rcAppOpen: Int
    get() = sharedPreferences.getInt(appOpen, 1)
    set(value) = sharedPreferences.edit { putInt(appOpen, value) }

// later placement
var rcBannerLanguage: Int
    get() = sharedPreferences.getInt(bannerLanguage, 0)
    set(value) = sharedPreferences.edit { putInt(bannerLanguage, value) }
```

### Remote Config + SharedPreferences (mandatory — confirmed only)

Add **only** keys for placements the developer confirmed in Round 2:

- Key constant + `SharedPrefManager` `rc*` property (read default `1` for the first two confirmed placements, `0` for the rest — see above)
- Default in `RemoteConfigDataSource.DEFAULTS` (same `1` / `0` split when that map exists)
- Copy into prefs in `RemoteConfigRepositoryImpl`
- Same key in Firebase Remote Config console notes / marketing handoff
- Catalog `isEnabled = { it.rcYourFlag != 0 }` (banner TOP/BOTTOM: `0` off, `1` adaptive, `2` collapsible)

**Do not** create SharedPreferences / RC defaults / repository copies for formats or screens that were not selected.

Unconfirmed catalog rows: keep RC-off via `isEnabled = { 0 }` (or leave existing always-off) — **no** unused `rc*` properties.

---

## Step 6 — Verify

- Premium users do not load/show (validator).
- No production IDs in debug.
- `gmaAdsModule` registered; App ID present.
- No MVI Intent/State/Effect for ads load/show.
- SharedPrefManager / RC defaults contain **only** implemented placement keys (plus any pre-existing unrelated keys).
- Entrance starts Remote Config and ignores its result. Navigation follows consent (8s) → AdMob init → ad calls (8s), then leaves. It does not leave on the Remote Config result.
- First two confirmed placements use SharedPref default `1`; later ones use `0`.

---

## Step 7 — End report (mandatory)

Report **Confirmed & wired** and **Not selected** (developer skipped), not “auto-skipped missing screens.”

Example:

```
Confirmed screens: Entrance, Language
Wired:
- EntranceFragment — interstitial only (+ consent, blockAppOpen)
- LanguageFragment — banner + native
Not selected:
- OnboardingFragment — developer skipped
RC / prefs added:
- interEntrance, bannerLanguage, nativeLanguage
```

---

## Step 8 — Marketing handoff template (mandatory)

After wiring (and when a production bundle is ready to share), fill and output this template for the marketing / ops team. Include **only** what this pass implemented (ads and/or billing). Leave AdMob unit ID values blank for marketing to fill. Omit entire sections that were not part of this work.

Output exactly in this shape (filled example structure):

~~~~~
##### {App name from project-settings / product}
*Remote Config*
```
Banner
 - {rcBannerKey}
0: off, 1: Adaptive, 2: Collapsible

Interstitial
 - {rcInterKey}
0: off, 1: on

Native
 - {rcNativeKey}
0: off, 1: on

App Open
 - {rcAppOpenKey}
0: off, 1: on

Rewarded / Rewarded Interstitial
 - {rcRewardedKey}
0: off, 1: on

Premium
 - premiumFirstTime
 - premiumSecondTime
0: off, 1: weekly

InAppBilling (Subscriptions)
    Weekly Package
 		product_id: {product_id}
 		plan_id: {plan_id}

    Monthly Package
 		product_id: {product_id}
 		plan_id: {plan_id}

    Yearly Package
 		product_id: {product_id}
 		plan_id: {plan_id}

InAppBilling (One-time / in-app products)
    {Product display name}
 		product_id: {product_id}
```

*Required Admob Ad Ids*
```
- {placementKey / rc name}: 

- {placementKey / rc name}: 
```
~~~~~

Rules for filling the template:

1. List every **implemented** RC key under its format group (Banner / Interstitial / Native / App Open / Rewarded…).
2. Banner lines always document `0` / `1` / `2` when TOP/BOTTOM adaptive-collapsible applies; other formats use `0` off / `1` on unless the app uses a different scheme — match catalog.
3. **Required Admob Ad Ids:** one blank line per implemented placement (marketing fills production unit IDs → put them in **release** `resValue` lines in `gmaAds/build.gradle.kts`; keep debug on Google samples).
4. **Premium / InAppBilling:** include only if this pass (or the linked billing skill run) added or changed them; use real `product_id` / `plan_id` from the app.
5. Do not list RC keys or ad slots that were not implemented.

Same handoff applies when later passes use `add-admob-*`, `hsg-implement-in-app-billing`, `hsg-add-subscription-packages`, or `hsg-add-inapp-packages` — emit an updated template covering only the new/changed surface.

---

## Forbidden

- Auto-wiring all ref screens without Round 1 + Round 2 confirmation
- Creating missing product screens for ads
- Converting ads to MVI
- Inventing a custom ads module instead of GitHub `:gmaAds`
- Editing `:gmaAds` engine during place
- Creating SharedPreferences / Remote Config keys for placements the developer did not confirm
- Inventing formats not in the ref capability map for that screen role
- Guessing “both” or enabling extra formats when the developer’s answer is incomplete
- Navigating Entrance from the Remote Config result once ads are wired
- Waiting on Remote Config before consent, AdMob init, or ad calls
- Letting consent or ads run past their 8-second caps (16 seconds total) before navigating
- Defaulting every placement to `0`, including the first two confirmed ads
