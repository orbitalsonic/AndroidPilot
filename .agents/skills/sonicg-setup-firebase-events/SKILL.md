---
name: sonicg-setup-firebase-events
description: OrbitalSonic Gemini Skill: First-time Firebase Analytics for the full app, or a full cutover off old per-screen event names (shared screen_view and ui_click, EventsProvider). Use when adding Analytics events app-wide, replacing old events, EventsProvider, postScreenView, or @sonicg-setup-firebase-events — not for a few screens on an app that already uses this catalog (use sonicg-add-firebase-events) and not for Remote Config.
---

# Implement Firebase Events (full app, first time)

Follow `.agents/rules/22-platform-firebase.md`, `12-naming-conventions.md`, `16-logging.md`, `08-gradle.md`, `00-global.md`.  
Shared event rules: [events.md](../events.md).

Obey `.agents/project-settings.json` when present.

**Requires human approval** before adding `firebase-analytics` if it is not in the catalog.

For extra screens later → `sonicg-add-firebase-events`.

## Entry

| App state | Action |
|-----------|--------|
| `postScreenView` + this catalog already on most screens | Stop. Point user to `sonicg-add-firebase-events`. |
| Old events exist (`postFirebaseEvent`, one event name per screen or button, strings like `"HOME_SCREEN"`) | Continue. Remove those first, then post only the catalog in [events.md](../events.md). |
| No events | Continue. |

---

## Step 0 — Ask before coding

List every user-visible Fragment / Compose Screen / dialog / sheet.

If a screen has no name in [events.md](../events.md), **AskQuestion** for the snake_case `screen_name` before adding a constant. Do not invent one.

Splash, when the app has one: `splash_ft_screen` on first launch after install, `splash_st_screen` on every later launch.

---

## Step 0.5 — Remove old events first

Search the whole project before adding the new catalog.

Delete every old analytics post and the constants that fed it:

- `postFirebaseEvent` and any `logEvent` whose name is the screen or button (`HOME_SCREEN`, `LANGUAGE_CONTINUE_BUTTON`, `SPLASH_FT`, …)
- Constants whose string value is that old event name
- A second events object, if one exists only for those old names

Do not post an old name and a new name for the same screen. From this build on, the only events that leave the app are the closed list in [events.md](../events.md).

GA4 keeps events already collected. This step only stops the app from sending the old names.

---

## Step 1 — Catalog

Latest stable, `# Firebase` / `// Firebase`:

```toml
firebaseBom = "…"   # latest stable
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
```

```kotlin
// Firebase
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)   // :core-platform (or :app if single-module)
```

`:app` still needs `google-services` + `google-services.json` before events can reach GA4.

Ensure `Constants.TAG_FIREBASE` exists (`16-logging`).

In the **application** manifest:

```xml
<meta-data
    android:name="google_analytics_automatic_screen_reporting_enabled"
    android:value="false" />
```

---

## Step 2 — EventsProvider + poster

Multi-module greenfield:

1. Copy [templates/firebase/EventsProvider.kt](../sonicg-setup-new-project/templates/firebase/EventsProvider.kt) → `:core-common`. Keep only screen and element constants for screens that exist, plus the closed event list and parameter constants.
2. Copy [templates/firebase/PlatformFirebase.kt](../sonicg-setup-new-project/templates/firebase/PlatformFirebase.kt) → `:core-platform` `firebase/PlatformFirebase.kt` when the object is missing (`recordException` + `postScreenView` + `postUiClick` + `getDeviceToken`). No `Context` field. No ads-revenue helper in this skill.

Single-module: same two types next to existing helpers. Ask before inventing a new module.

Add an element constant for each real control, using [events.md](../events.md) (`language_continue_button`, `home_premium_icon`, …).

---

## Step 3 — Wire every screen

**Screen** — xml Fragment `onStart` (once each time that destination starts, including return from another screen or an ad). Do not log the parent again when a dialog that only paused it is dismissed.

```kotlin
override fun onStart() {
    super.onStart()
    PlatformFirebase.postScreenView(
        screenName = EventsProvider.HOME_SCREEN,
        screenClass = HomeFragment::class.java.simpleName,
    )
}
```

**compose:** post `postScreenView` when `*Screen` is shown, once per entry.

Host Activity sets the entry source before the first screen starts. It does not also log `screen_view` for that fragment.

```kotlin
PlatformFirebase.setEntrySource(EventsProvider.ENTRY_ORGANIC)
```

**Tap** — on the click handler: `postUiClick`. If that tap is a semantic event in [events.md](../events.md) (`language_confirm`, `iap_start`, `exit_confirm`, …), log that event as well.

Do **not** auto-log in `ParentFragment`.

---

## Step 4 — Verify

- [ ] One `EventsProvider` (no parallel constant objects)
- [ ] No raw event string literals in UI
- [ ] Event names are only the closed list in [events.md](../events.md)
- [ ] Screen and element values are lowercase snake_case constants
- [ ] Automatic screen reporting is off
- [ ] Poster has no `Context` on `PlatformFirebase`
- [ ] All discovered screens covered (or listed as skipped with reason)
- [ ] No `postFirebaseEvent` and no old per-screen or per-button event names left in source

## Do not

- Remote Config (`sonicg-setup-firebase-remote-config`)
- New modules / migrate folder layout without approval
- Second `EventsProvider`
- One event name per screen or button
- Leave old event posts in place next to `screen_view` / `ui_click`
- Rename a name from this catalog after it has shipped (`home_screen` stays `home_screen`)
- PII in event names or bundles
