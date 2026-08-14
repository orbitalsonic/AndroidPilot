---
name: android-new-project
description: Workflow for turning a fresh copy of this Android template into a new app — renaming the package/namespace/applicationId, branding (icons, splash, palette, fonts), Firebase setup, signing config, stripping unused sample screens, and verifying the foundation. Use when the user says they are starting a new app, rebranding the template, renaming the package, or setting up a copied project.
---

# New Project Workflow

Use this when the template has been copied to start a new app.

**Ask the user for anything you do not know before renaming**: app name, package/namespace,
applicationId, brand colors, Firebase project, support links. Never invent an applicationId.

## Step 1 — Identity

Update in this order and keep everything consistent:

1. `settings.gradle.kts` — `rootProject.name`
2. `app/build.gradle.kts` — `namespace`, `applicationId`, `versionCode = 1`, `versionName = "1.0.0"`
3. Package directories under `app/src/main/java/...` — rename to the new namespace and fix every
   `package` / `import` statement, including `MainApplication` and every `BuildConfig` import
4. `AndroidManifest.xml` — relative `.MainApplication` and `.presentation.*` names stay valid after
   the namespace change; verify every entry still resolves
5. `res/values/strings.xml` — `app_name`
6. `res/values/urls.xml` — about, privacy, terms, help email, support email
7. `README.md`

Do a project-wide search for the old package name and the old app name before declaring this done.

## Step 2 — Branding

- Launcher icons: `mipmap-*` + `mipmap-anydpi-v26` adaptive icon, and `ic_launcher_background`.
- Splash: `drawable/splash_icon.xml`, `values/splash.xml` (`Theme.App.Starting`), and the
  `splashColor` token in `values/colors.xml` + `values-night/colors.xml`.
- Brand palette: replace the `primary*` and `secondary*` ramps in `values/colors.xml`. **Keep the
  semantic token names unchanged** so every layout keeps working.
- Verify contrast of the new palette in BOTH themes.
- Fonts: replace `res/font/*` and update the styles that reference them.

## Step 3 — Firebase

- Replace `app/google-services.json` with the new project's file.
- The `google-services` and `crashlytics` plugins are already applied — do not remove them.
- If the app does not use Firebase, remove the plugins, the dependencies, `google-services.json`, and
  `common/firebase/` together. **Never leave a half-removed integration.**
- Update `RemoteConstants.kt` keys and defaults for the new app.

## Step 4 — Signing

- Create a keystore outside version control, or add a `Keystore/` folder to `.gitignore`.
- Put credentials in `local.properties` or an untracked `keystore.properties`, and read them in
  `build.gradle.kts`. **Never commit passwords.**
- Add a `signingConfig` for release before the first store build.

## Step 5 — Strip what you do not need

- Sample screens live in `presentation/samples/`. Delete the packages you do not need, together with
  their layouts, nav graph destinations, strings, drawables, and `FragmentHome` buttons.
- Delete unused localized `values-<lang>` files only if the app ships fewer languages.
- Remove unused dependencies from `app/build.gradle.kts` **and** `gradle/libs.versions.toml`.
- Keep `presentation/base/`, `helpers/`, `common/`, and `storage/` — they are the template's value.

## Step 6 — Wire the foundation

Confirm these still work after renaming:

- `MainApplication` restores theme (`applyAppTheme`) and locale
- `StartupActivity` → splash / onboarding → `MainActivity` flow
- `startup_nav_graph.xml` and `main_nav_graph.xml`
- `SharedPrefManager` first-run flag
- light and dark theme
- language switching

## Step 7 — First feature

Implement the first real screen using the `android-feature` skill.

## Step 8 — Verify

- `gradlew clean assembleDebug` succeeds
- the app launches; splash → onboarding → main works
- both themes render correctly
- no reference to the template name remains (`generalproject`, "General Project", orbitalsonic links)
- the release build with `optimization { enable = true }` still runs (`gradlew assembleRelease`)
