# Firebase Analytics events — shared

Used by `sonicg-setup-firebase-events` and `sonicg-add-firebase-events`. Follow `.agents/rules/22-platform-firebase.md`, `12-naming-conventions.md`, `16-logging.md`.

Every app uses the same event names. Screens and buttons are parameter values, so GA4 paths, funnels, and retention match across apps.

## Placement

| App state | Action |
|-----------|--------|
| Multi-module + no events yet | `EventsProvider` in **`:core-common`**. `PlatformFirebase` poster in **`:core-platform`**. |
| Already has `EventsProvider` / `FirebaseUtils` / other event constants | **Add into that structure.** Do **not** create a second provider. Do **not** move files without **explicit user approval**. |
| Single-module, no events yet | One `EventsProvider` next to existing helpers. Ask before inventing a new module. |
| Old per-screen or per-button event names (`postFirebaseEvent`, `"HOME_SCREEN"`) | **`sonicg-setup-firebase-events` removes them first**, then posts only this catalog. Do not send both. |

## Names

Kotlin constants are `SCREAMING_SNAKE`. The string value is lowercase `snake_case`. Constant name and string value are not the same text.

- Start with a letter. Only `a-z`, `0-9`, `_`. Event names are at most 40 characters.
- Never start a name with `firebase_`, `google_`, or `ga_`.
- Never use reserved GA4 names: `ad_click`, `error`, `session_start`.
- A name from this catalog, once released, is never renamed or reused. A removed screen keeps its name reserved. Old event names outside this catalog are deleted by `sonicg-setup-firebase-events` and are not posted again.
- No raw event strings in UI.

```kotlin
object EventsProvider {
    const val SCREEN_VIEW = "screen_view"
    const val UI_CLICK = "ui_click"
    const val HOME_SCREEN = "home_screen"
    const val HOME_PREMIUM_ICON = "home_premium_icon"
    const val TYPE_ICON = "icon"
}
```

### Closed event list

New screens and buttons are new **names**, not new events. Add an event name only when the user explicitly extends this list.

| Event | When | Parameters |
|-------|------|------------|
| `screen_view` | A screen, dialog, or sheet becomes visible | `screen_name`, `screen_class`, `previous_screen`, `entry_source` |
| `ui_click` | Any tap | `screen_name`, `element_name`, `element_type` |
| `splash_complete` | Splash hands over | `screen_name`, `duration_ms`, `ad_shown`, `next_screen` |
| `language_confirm` | Language continue tapped | `language_code`, `source` |
| `onboarding_complete` | Onboarding finished or skipped | `total_steps`, `skipped` |
| `permission_grant` / `permission_deny` | System permission answer | `permission_type` |
| `ads_click` | User taps an ad | `ad_format`, `ad_placement` |
| `ads_return` | User returns after an ad click | `ad_placement`, `time_away_sec` |
| `ads_show_fail` | A due ad did not show | `ad_placement`, `reason` |
| `premium_view` / `premium_close` | Premium shown / closed without buying | `screen_name`, `premium_id` |
| `iap_start` / `iap_success` / `iap_fail` | Purchase tapped / confirmed / failed | `product_id`, `premium_id` |
| `feature_start` / `feature_complete` / `feature_fail` | Core feature used | `feature_name` |
| `error_shown` | User sees an error | `error_type`, `error_code` |
| `exit_confirm` | User exits via the exit dialog | `session_duration_sec` |

`screen_name` is sent on every event. `iap_*` is the paywall funnel. Leave Play's automatic `in_app_purchase` for revenue. Do not also log a manual `purchase` for the same transaction.

A business tap logs **two** events: `ui_click`, plus the semantic event when one applies (`language_confirm`, `iap_start`, `exit_confirm`, …).

### Screen names

| `screen_name` | When |
|---------------|------|
| `splash_ft_screen` | Splash on the first launch after install |
| `splash_st_screen` | Splash on every later launch |
| `language_screen` | Language picker |
| `onboarding_<n>_screen` | Each onboarding page (`onboarding_1_screen`) |
| `permission_screen` | Pre-permission explainer |
| `home_screen` | Main screen |
| `settings_screen` | Settings |
| `premium_splash_ft_screen` | Premium right after the first-time splash |
| `premium_onboarding_screen` | Premium after the last onboarding page |
| `premium_splash_st_screen` | Premium right after the returning splash |
| `premium_home_screen` | Premium opened from `home_premium_icon` |
| `premium_feature_screen` | Premium from a locked feature |
| `premium_limit_screen` | Premium when the free limit is reached |
| `premium_settings_screen` | Premium opened from settings |
| `premium_success_screen` | After a successful purchase |
| `exit_dialog` | Exit confirmation |
| `rate_dialog` | Rating prompt |
| `no_internet_dialog` | Offline message |
| `privacy_policy_screen`, `terms_screen` | Legal pages |

App-specific screens add their feature area (`remote_control_screen`). `<area>` is the screen name without `_screen` and without the step number.

Splash first-open versus returning is required when the app has a splash: `splash_ft_screen` / `splash_st_screen`.

### Element names

`<area>_<purpose>_<type>`. Purpose may be omitted when the type is enough (`language_item`).

Allowed endings: `_button`, `_icon`, `_tab`, `_card`, `_item`, `_toggle`, `_link`, `_fab`, `_chip`, `_slider`.

`element_type` is that ending without the underscore (`button`, `icon`, …).

Onboarding buttons use one name on every page (`onboarding_next_button`). The page number stays in `screen_name`. Premium controls include the placement (`premium_home_close_icon`).

Ad placement values: `<area>_<format>` (`splash_ft_appopen`, `home_banner`).

`entry_source`: `organic`, `notification`, `deeplink`, `ad`, `widget`.

## Where to fire

Match the app's existing call sites when events already exist.

Greenfield:

- **`screen_view`** — xml: Fragment `onStart`. compose: when `*Screen` enters composition, once per time it is shown. Log again when the user returns from another screen or from an ad. Log a dialog or sheet when it opens. Do not log the parent again when that dialog closes.
- **`ui_click`** — the click / continue / cross handler.
- **Not** `ParentFragment` auto-log, not a root `NavGraph` auto-log, not the ViewModel.
- Host `Activity` sets `entry_source` before the first screen starts. It does not log its own `screen_view` when a fragment is the screen.

`screen_view` uses `FirebaseAnalytics.Event.SCREEN_VIEW` (string `screen_view`) with `Param.SCREEN_NAME` and `Param.SCREEN_CLASS`.

Turn off automatic screen reporting so Activities do not log a second `screen_view`:

```xml
<meta-data
    android:name="google_analytics_automatic_screen_reporting_enabled"
    android:value="false" />
```

## Poster

`PlatformFirebase` in `:core-platform` is an `object` with **no** `Context` field. Copy the poster from `sonicg-setup-new-project` [templates/firebase/PlatformFirebase.kt](sonicg-setup-new-project/templates/firebase/PlatformFirebase.kt) (`recordException`, `postScreenView`, `postUiClick`, `getDeviceToken`).

```kotlin
PlatformFirebase.setEntrySource(EventsProvider.ENTRY_ORGANIC)
PlatformFirebase.postScreenView(
    screenName = EventsProvider.HOME_SCREEN,
    screenClass = HomeFragment::class.java.simpleName,
)
PlatformFirebase.postUiClick(
    screenName = EventsProvider.HOME_SCREEN,
    elementName = EventsProvider.HOME_PREMIUM_ICON,
    elementType = EventsProvider.TYPE_ICON,
)
```

Log format: `ClassName: functionName: State: details`. Never log PII, tokens, or full parameter bundles that could hold them.
