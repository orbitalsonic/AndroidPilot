---
description: Premium / paywall sticky footer and delayed close button
paths:
  - "**/premium/**"
  - "**/PremiumFragment*"
  - "**/PremiumScreen*"
  - "**/paywall/**"
---

# Premium / paywall screen

Full billing playbook: [reference/premium-billing.md](reference/premium-billing.md). Skills: `sonicc-implement-in-app-billing`, `sonicc-add-subscription-packages`, `sonicc-add-inapp-packages`.

## Layout

- **Sticky footer:** purchase CTA + legal / footer text constrained to the **bottom of the root**; do **not** put them inside the scroll content
- Benefits, plan cards, hero content → `NestedScrollView` (or list) above the footer (`layout_constraintBottom_toTopOf` CTA)
- Close (X): `ButtonStyle.IconButton`; visibility from State

## Close button — 3 second delay (mandatory)

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

- Immediate close button on paywall open
- Scrolling the legal footer / primary CTA away with benefits content
- Raw `BillingClient` in the Fragment when `:data` BillingManager exists
