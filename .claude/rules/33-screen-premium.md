---
description: Premium / paywall — startup StartupPremiumFragment vs in-app PremiumFragment, sticky footer, delayed close
paths:
  - "**/premium/**"
  - "**/PremiumFragment*"
  - "**/PremiumScreen*"
  - "**/StartupPremium*"
  - "**/paywall/**"
---

# Premium / paywall screens (two)

Full billing playbook: [reference/premium-billing.md](reference/premium-billing.md). Skills: `sonicc-implement-in-app-billing`, `sonicc-add-subscription-packages`, `sonicc-add-inapp-packages`.

| | **Startup** paywall | **In-app** paywall |
|---|---|---|
| Host | `StartupActivity` · `nav_graph_startup` | `MainActivity` · `nav_graph_main` |
| Class / layout | `StartupPremiumFragment` · `fragment_startup_premium.xml` | `PremiumFragment` · `fragment_premium.xml` |
| Package | `presentation/startup/…` | `presentation/premium/…` |
| Entered from | `StartupFragment` / `StartupWelcomeBackFragment` (`isFirstTime = false`) or `StartupLanguageFragment` (`isFirstTime = true`) — gated by `showPremiumFirstTime` / `showPremiumSecondTime` RC + `!isAppPurchased` + offers loaded | Settings / crown buttons via `navigateTo` (or `navigateRootTo` from a nested tab) |
| Nav arg | `isFirstTime: Boolean` (property `navArgs`) | none |
| Close (X) | After 3 s → `isFirstTime ? StartupIntroFragment : moveToMain()` (interstitial first when wired) | After 3 s → `popFrom(R.id.premiumFragment)` (interstitial first when wired) |
| After purchase | `isAppPurchased = true`, reset ad counters → `moveToMain()` | `isAppPurchased = true` → restart `MainActivity` (`FLAG_ACTIVITY_NEW_TASK or CLEAR_TASK`) so ads/UI refresh |
| Back press | Blocked (`StartupActivity`) | Same as X (pop) |
| Analytics | `startup_premium_screen` | `premium_screen` |

Shared pieces live once: offer loading / purchase launch through shared UseCases (and an optional common ViewModel base), `BillingProductIds`, plan mapper, benefit list items. Fragments + layouts are duplicated because toolbar, close behavior and host differ.

## Layout

- **Sticky footer:** purchase CTA + legal / footer text constrained to the **bottom of the root**; do **not** put them inside the scroll content
- Benefits, plan cards, hero content → `NestedScrollView` (or list) above the footer (`layout_constraintBottom_toTopOf` CTA)
- Close (X): `ButtonStyle.IconButton`; visibility from State
- Loading / error / content groups (`cpiLoadingPremium`, `groupErrorPremium`, `groupContentPremium`) while offers load

## Close button — 3 second delay (mandatory, both screens)

- State starts with `showCloseButton = false`
- UI starts work when resumed:

```kotlin
private fun screenStarted() = launchWhenResumed {
    viewModel.handleIntent(PremiumIntent.ScreenStarted)
}
```

- ViewModel:

```kotlin
private suspend fun onScreenStarted() {
    if (hasStarted) return
    hasStarted = true
    showCloseButtonWithDelay()
    // analytics + observe offers…
}

private fun showCloseButtonWithDelay() = viewModelScope.launch {
    delay(3000.milliseconds)
    _state.update { it.copy(showCloseButton = true) }
}
```

- Do **not** show X on inflate; do **not** start the delay from `onViewCreated` alone without `launchWhenResumed`

## Forbidden

- One `PremiumFragment` serving both hosts via flags
- Immediate close button on paywall open
- Scrolling the legal footer / primary CTA away with benefits content
- Raw `BillingClient` in the Fragment when `:data` BillingManager exists
