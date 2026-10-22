---
name: hsc-add-firebase-events
description: Hypersoft Claude Skill: Add Firebase Analytics screen_view and ui_click for one or more existing screens. Use when extending EventsProvider, adding events to selected Fragments or Compose Screens, or /hsc-add-firebase-events — not for first-time full-app wiring (use hsc-implement-firebase-events).
---

# Add Firebase Events (selected screens)

Follow `.claude/rules/22-platform-firebase.md`, `12-naming-conventions.md`, `16-logging.md`.  
Shared event rules: [events.md](../events.md).

Obey `.claude/project-settings.json` when present.

For first-time full-app events → `hsc-implement-firebase-events`.

## Entry

| App state | Action |
|-----------|--------|
| No `postScreenView`, or old per-screen / per-button events are still posted | Stop → **`hsc-implement-firebase-events`** (it removes the old posts, then wires this catalog for the whole app) |
| `postScreenView` and this catalog already exist | Continue. Add into **existing** files. Do **not** migrate modules without **explicit user approval**. |

---

## Step 0 — Ask before coding

### 0.1 Which screens (mandatory)

List user-visible Fragments / Compose Screens / dialogs / sheets that lack `screen_view`.

**AskQuestion** (`allow_multiple`): the user picks one or more screens. Wait. Do not instrument screens they did not pick.

### 0.2 Names

Use the `screen_name` from [events.md](../events.md). If the screen is not listed, ask for the snake_case name before adding a constant.

Splash, only when a picked screen is the splash: `splash_ft_screen` / `splash_st_screen`.

---

## Step 1 — Constants

Add `SCREAMING_SNAKE` constants to the **existing** `EventsProvider`. The string value is lowercase snake_case (`HOME_SCREEN = "home_screen"`, `LANGUAGE_CONTINUE_BUTTON = "language_continue_button"`).

Do not create a second events file. Do not add a new event name; use `screen_view`, `ui_click`, and the semantic events in [events.md](../events.md).

---

## Step 2 — Call sites

Same as [events.md](../events.md): xml Fragment `onStart` or compose when `*Screen` is shown; click handler for taps.

```kotlin
PlatformFirebase.postScreenView(
    screenName = EventsProvider.SETTINGS_SCREEN,
    screenClass = SettingsFragment::class.java.simpleName,
)
PlatformFirebase.postUiClick(
    screenName = EventsProvider.SETTINGS_SCREEN,
    elementName = EventsProvider.SETTINGS_RATE_US_BUTTON,
    elementType = EventsProvider.TYPE_BUTTON,
)
```

Follow this app’s existing posts if they already fire from somewhere else. Do not move them.

---

## Step 3 — Verify

- [ ] Only the screens the user picked
- [ ] `screen_view` for the screen; `ui_click` for each real tap
- [ ] No raw event strings in UI
- [ ] No new Analytics library unless the poster was missing (then stop and send the user to `hsc-implement-firebase-events`)

## Do not

- Full-app sweep (that is `hsc-implement-firebase-events`)
- Remote Config (`hsc-add-firebase-remote-config`)
- Replace `FirebaseUtils` with `PlatformFirebase` without approval
- One event name per button
- Add `screen_view` beside an old event name for the same screen (that cutover is `hsc-implement-firebase-events`)
- Rename a catalog name after it has shipped (`home_screen` stays `home_screen`)
- Log PII
