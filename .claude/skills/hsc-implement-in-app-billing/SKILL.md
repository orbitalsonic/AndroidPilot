---
name: hsc-implement-in-app-billing
description: Hypersoft Claude Skill: Add Google Play in-app billing to a new app — subscriptions and/or one-time products — using hypersoft inappbilling v4, BillingDataSource, paywalls, and isAppPurchased. Use when setting up billing, IAP, premium, or subscriptions for the first time, or /hsc-implement-in-app-billing.
---

# Implement In-App Billing

Follow `.claude/rules/21-ads-billing.md`, `00-global.mdc`, `04-mvi-presentation.mdc`, `07-dependency-injection.mdc`, `16-logging.mdc`, `13-libraries-stack.mdc`.

Full detail: [`.claude/rules/reference/premium-billing.md`](../../rules/reference/premium-billing.md)

Obey `.claude/project-settings.json` when present (`orientation`, `writeTestsWithFeatures`).

**Requires human approval** before adding `inappbilling` if not already in the catalog.

Cross-skills: `hsc-create-clean-architecture` (domain/data), `hsc-create-mvi` + `hsc-figma-to-xml` **or** `hsc-figma-to-compose` (paywall UI, per `uiFramework`).

---

## Step 0 — Mandatory user confirmation (do NOT write code until done)

Use **`AskQuestion`** for every block below. If billing already exists (`BillingDataSource`, `BillingRepository`, paywall screens) on **v3** → stop and redirect to **`hsc-update-in-app-billing`**. On **v4** → redirect to `hsc-add-subscription-packages` or `hsc-add-inapp-packages`.

### Subscriptions block

**Q1 — How many subscriptions?**

| Option                    | Tiers                                                      |
|---------------------------|------------------------------------------------------------|
| Not yet                   | Skip subs this pass                                        |
| Weekly only               | 1 tier                                                     |
| Weekly + monthly          | 2 tiers                                                    |
| Weekly + monthly + yearly | 3 tiers (recommended full paywall)                         |
| Other                     | User lists tiers in free text (e.g. monthly + yearly only) |

**Q2 — Product & plan IDs** (skip if Q1 = Not yet)

| Option       | Action                                                       |
|--------------|--------------------------------------------------------------|
| Use defaults | Fill from default table below; show summary for confirm      |
| Custom       | User enters product ID + plan ID **for every selected tier** |

**Default subscription IDs** (suggest every time):

| Tier    | Product ID              | Plan ID              |
|---------|-------------------------|----------------------|
| Weekly  | `basic_product_weekly`  | `basic-plan-weekly`  |
| Monthly | `basic_product_monthly` | `basic-plan-monthly` |
| Yearly  | `basic_product_yearly`  | `basic-plan-yearly`  |

If Q1 = **Other** → always collect custom IDs per tier.

### In-app block

**Q1 — How many one-time in-app products?**

| Option         | Meaning                        |
|----------------|--------------------------------|
| Not yet        | Skip in-app this pass          |
| 1 package only | Single product                 |
| Other          | User specifies count and types |

**Q2 — Product ID** (skip if Q1 = Not yet)

| Option            | Action                                                         |
|-------------------|----------------------------------------------------------------|
| Suggested default | `{applicationId}` for lifetime unlock, or `basic_inapp_unlock` |
| Custom            | User enters product ID(s)                                      |

Also ask per product: **non-consumable** (lifetime unlock → `isAppPurchased`) or **consumable** (credits → separate flag).

### ID validation (before proceeding)

Validate every user-entered ID. **Warn and re-ask** on failure.

**Product ID:**

- Starts with number or lowercase letter
- Only `[a-z0-9_.]`, max 40 chars
- Example invalid: `Basic_Product_Weekly` → suggest `basic_product_weekly`

**Plan ID:**

- Prefer hyphens: `basic-plan-weekly`
- Allowed: `[a-z0-9._-]`, max 63 chars
- Warn if underscores used where hyphens are conventional

### After answers

1. Print summary: subs tiers + IDs, in-app products + types + IDs
2. Ask user to confirm before coding
3. If **both** subs and in-app = Not yet → stop; ask user to re-run when ready

---

## Step 1 — Gradle

1. JitPack in `settings.gradle.kts` (`dependencyResolutionManagement.repositories`)
2. Catalog under `# Billing`:

```toml
inappbilling = "4.0.0"
hypersoft-inappbilling = { group = "com.github.hypersoftdev", name = "inappbilling", version.ref = "inappbilling" }
```

3. `implementation(libs.hypersoft.inappbilling)` on `:data` and `:presentation`
4. `const val TAG_BILLING = "TAG_BILLING"` in `:core-common` `Constants`

---

## Step 2 — Play Console (parallel with code)

**Subscriptions** (for each confirmed tier):

1. Monetize → Subscriptions → create product (Option 1: one product per plan)
2. Add base plan with confirmed plan ID
3. Optional: free-trial offer on weekly (or chosen tier) — offer IDs stay dynamic in code

**In-app** (for each confirmed product):

1. Monetize → In-app products → create managed product with confirmed product ID
2. Non-consumable for lifetime; consumable for credits

License testers on internal track. App uploaded to a test track.

---

## Step 3 — Domain (`hsc-create-clean-architecture`, area `billing`)

Create only what user confirmed:

```
domain/.../billing/BillingProductIds.kt     # ONLY confirmed IDs
domain/.../entity/billing/PremiumPlan.kt    # if subs
domain/.../entity/billing/SubscriptionOffer.kt
domain/.../entity/billing/InAppProduct.kt   # if in-app
domain/.../entity/billing/BillingPurchaseResult.kt
domain/.../repository/billing/BillingRepository.kt
domain/.../usecase/billing/ObserveSubscriptionOffersUseCase.kt   # if subs
domain/.../usecase/billing/ObserveInAppProductsUseCase.kt      # if in-app
domain/.../usecase/sharedPref/IsAppPurchasedUseCase.kt
domain/.../usecase/sharedPref/SetAppPurchasedUseCase.kt
```

`BillingProductIds` example (scoped to user choices):

```kotlin
object BillingProductIds {
    // Subscriptions — include only selected tiers
    const val WEEKLY_PRODUCT = "basic_product_weekly"
    const val WEEKLY_PLAN = "basic-plan-weekly"
    // …

    val allSubscriptionProductIds: List<String> = listOf(/* confirmed product IDs only */)
    val nonConsumableProductIds: List<String> = listOf(/* lifetime IDs */)
    val consumableProductIds: List<String> = listOf(/* credit pack IDs */)
}
```

`SubscriptionOffer` must include: `productId`, `planId`, `offerId`, `priceFormatted`, `hasFreeTrial`, `freeTrialDays`.

Register use cases in `useCaseModule` with `lazyModule` / `factory`.

---

## Step 4 — Data

**BillingDataSource** — singleton owner:

```kotlin
billingManager
    .setSubscriptions(BillingProductIds.allSubscriptionProductIds)
    .setNonConsumables(BillingProductIds.nonConsumableProductIds)
    .setConsumables(BillingProductIds.consumableProductIds)
```

- `connect()` once from `Application` via `start(applicationScope)`
- Collect `purchasesState` → `sharedPrefManager.isAppPurchased`
- Never construct `BillingManager` per screen

**BillingRepositoryImpl:**

- Subs: prefer-trial mapper from `productsState` (see reference §4)
- In-app: map `ProductDetail` price by `productId`

Koin (`dataModule`):

```kotlin
single { BillingDataSource(androidContext(), get()) }
single { get<BillingDataSource>().billingManager }
single<BillingRepository> { BillingRepositoryImpl(billingDataSource = get()) }
```

Application:

```kotlin
koin.get<BillingDataSource>().start(applicationScope)
```

---

## Step 5 — Entitlement

- Pref key `is_app_purchased` (boolean, default `false`)
- Sync from `purchasesState` + set on `PurchaseOutcome.Success` / `AlreadyOwned`
- Gate ads managers on `isAppPurchased` — do not convert ads to MVI as part of billing work unless the user **explicitly** asks
- Non-consumable in-app → same flag; consumable → separate balance unless product says otherwise

---

## Step 6 — Presentation (`hsc-create-mvi`)

Typical screens:

| Screen         | When                          | Content                                     |
|----------------|-------------------------------|---------------------------------------------|
| Splash premium | RC-gated first/returning user | Often weekly CTA                            |
| Premium        | Home / Settings               | Selected subs tiers + optional lifetime row |

MVI effects:

```kotlin
data class LaunchSubsPurchase(val productId: String, val planId: String, val offerId: String?) : PremiumEffect()
data class LaunchInAppPurchase(val productId: String, val offerId: String?) : PremiumEffect()
```

Fragment (release):

```kotlin
// Subs
billingManager.purchaseSubs(act, productId, planId, offerId = offerId)
// In-app
billingManager.purchaseInApp(act, productId, offerId = offerId)
```

Map `PurchaseOutcome` → `BillingPurchaseResult` → `PurchaseResultReceived` Intent.

| Build   | Behavior                                     |
|---------|----------------------------------------------|
| DEBUG   | Fake success — no Play sheet                 |
| RELEASE | Real purchase with `offerId` when applicable |

Portrait **and** landscape unless `project-settings.json` locks orientation. All strings in `:core-ui`.

**Paywall layout / close (mandatory — `33-screen-premium`):**

- Sticky footer: CTA + legal fixed at bottom; scrollable content above
- Close (X): `showCloseButton = false` until 3s delay after `launchWhenResumed { handleIntent(ScreenStarted) }` → `showCloseButtonWithDelay()`

---

## Step 7 — Navigation & Remote Config (optional)

| RC flag (`0` = off)     | Use                               |
|-------------------------|-----------------------------------|
| `showPremiumFirstTime`  | First-time users after onboarding |
| `showPremiumSecondTime` | Returning users at entrance       |

Both require `!isAppPurchased`. Hide premium entry when purchased.

### Entrance navigation is not owned by billing

Application `connect()` / `start(applicationScope)` stays once at process start.

A billing refresh does not replace the normal leave-splash gate, and it does not navigate from Remote Config when ads are wired.

| Entrance ads | Who decides when to leave |
|--------------|---------------------------|
| Not wired | `hsc-setup-new-project`: Remote Config returns, or **5 seconds** |
| Wired (`hsc-implement-admob-ads`) | Consent (8s) → AdMob init → ad calls (8s), then navigate. Remote Config result is ignored |

`showPremiumFirstTime` / `showPremiumSecondTime` may still choose the **next** screen after that leave. They must not add another wait on the splash. Both require `!isAppPurchased`.

### Purchased user — cancel ads, keep the first-time funnel

`isAppPurchased` in `SharedPrefManager` is the no-ads flag (written when the billing response syncs `purchasesState`, and on `PurchaseOutcome.Success` / `AlreadyOwned`). Subs and non-consumable lifetime only. A consumable must not take this path.

In `EntranceViewModel`, as soon as that flag is **true** (already true at start, or it flips true when the billing response writes the pref):

1. Cancel the in-flight Entrance work: Remote Config wait, consent, AdMob init, ad load/show, and the 5s / 16s timers.
2. Do not show ads or splash premium.
3. Then navigate with the normal first-time funnel. `isAppPurchased` does **not** skip Language or Onboarding.

| User | `isFirstTime` | Destination |
|------|---------------|-------------|
| Purchased, first time | `true` | Language, then Onboarding, same as an unpaid first-time user |
| Purchased, returning | `false` | **Dashboard**. Skip Welcome Back and any other returning-user screen |
| Not purchased, returning | `false` | Welcome Back |

The Fragment still performs navigation from that effect. Do not call `NavController` inside the ViewModel.

```kotlin
fun onPurchased() {
    entranceJob?.cancel()
    val destination = if (sharedPrefManager.isFirstTime) {
        entranceDestination() // Language or Onboarding
    } else {
        Dashboard
    }
    _effect.emit(EntranceEffect.Navigate(destination))
}
```

---

## Step 8 — Tests

When `writeTestsWithFeatures: true` (`hsc-test-unit`):

- Fake `BillingRepository` emitting `SubscriptionOffer` with non-null trial `offerId`
- Assert `LaunchSubsPurchase` / `LaunchInAppPurchase` carry confirmed product IDs

---

## Step 9 — Verify

- [ ] JitPack + catalog + deps + `TAG_BILLING`
- [ ] Console products match `BillingProductIds`
- [ ] Singleton `BillingManager` + Application connect
- [ ] Billing does not own Entrance navigation (5s Remote Config gate without ads; consent + ads 16s gate when ads are wired)
- [ ] `isAppPurchased == true` cancels Entrance ads. First-time users still open Language / Onboarding. Returning users open Dashboard and skip Welcome Back
- [ ] Prefer-trial mapper; `offerId` in `purchaseSubs`
- [ ] In-app in correct `setNonConsumables` / `setConsumables`
- [ ] `isAppPurchased` synced; ads gated
- [ ] Fake purchase debug only
- [ ] Paywall strings in `:core-ui`; portrait + landscape

---

## Do not

- Write code before Step 0 confirmation
- Hardcode product/plan IDs in Fragments
- Skip `offerId` for trials
- Use raw `BillingClient` or v3 listeners
- Ship fake purchase in release
- Log purchase tokens or PII
- Using a billing refresh as the normal leave-splash gate
- Keeping a purchased user on Entrance, Welcome Back, or an ad once `isAppPurchased` is true
- Skipping Language or Onboarding because `isAppPurchased` is true
- Replacing the ads leave-splash sequence (consent 8s → AdMob init → ad calls 8s) with a Remote Config wait when ads are wired and the user is not purchased
