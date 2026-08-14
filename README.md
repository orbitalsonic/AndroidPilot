# 🚀 Android Engineering Rules

> Production-ready AI coding rules for Android development.
>
> A collection of reusable AI rules (Cursor `.mdc` and Claude `CLAUDE.md`) that help AI assistants generate clean, maintainable, and production-ready Android code.

---

## 🎯 Purpose

These rules transform AI coding assistants into experienced Android engineers by enforcing:

- Modern Android development practices
- MVVM (Baseline) + MVI (State-heavy)
- AGP 9 / Gradle 9 Standards
- Kotlin best practices
- XML + ViewBinding
- Material 3
- Performance optimization
- Lifecycle safety
- Error handling
- Production-ready implementations

Instead of repeatedly explaining your project standards, simply include these rules in your project.

---

# 📦 Tech Stack

These rules are designed for projects using:

- Kotlin only
- XML Layouts only
- ViewBinding only
- MVVM / MVI Architecture
- Koin
- Kotlin Coroutines
- StateFlow
- Jetpack Navigation
- AGP 9 / Gradle 9 Standards
- sdp / ssp for dimensions
- Glide for images
- Firebase (Analytics, Crashlytics, Remote Config)
- Splash Screen (androidx.core.splashscreen)

Supported Android versions:

- Minimum SDK: 24
- Target SDK: 37
- Compile SDK: 37
- Java Compatibility: 17

---

# 📁 Project Structure

```
.cursor/
└── rules/
    ├── 01-tech-stack.mdc
    ├── ...
    └── 13-updating-project.mdc
.claude/
└── CLAUDE.md
```

---

# 📖 Rule Overview

## 01 - Tech Stack

Defines the project's technology stack and modern standards including:

- Kotlin only (No Java)
- XML + ViewBinding only (No Compose)
- MVVM + MVI
- Koin DI
- AGP 9 / Gradle 9 standards (No Kotlin plugin, new optimization DSL)
- SDK versions (24-37)
- Dimens (sdp/ssp)

---

## 02 - Architecture

Enforces:

- MVVM (Baseline) / MVI (State-heavy)
- Repository Pattern
- Separation of Concerns
- Immutable UI State
- SharedFlow/Channel for Effects

---

## 03 - UI

Defines UI standards:

- XML only
- ViewBinding only
- Material 3
- Edge-to-edge support
- Accessibility & Dark Mode
- Responsive layouts with sdp/ssp

---

## 04 - Kotlin

Encourages:

- Idiomatic Kotlin
- Coroutines
- StateFlow
- Extension Functions
- Sealed Classes
- Immutable data

---

## 05 - Performance

Optimizes:

- RecyclerView
- Memory usage
- Rendering
- Startup time
- Background work
- Object allocation

---

## 06 - Error Handling

Standardizes:

- Result handling
- Exception handling
- User-friendly errors
- Structured concurrency
- Validation

---

## 07 - Testing

Promotes:

- Testable architecture
- Dependency Injection
- Unit testing
- Maintainable code

---

## 08 - Project Structure

Defines a consistent single-module (`:app`) package structure:

- `common/` (App-wide features)
- `helpers/` (Stateless Kotlin extensions)
- `presentation/` (Activities, Fragments, ViewModels)
- `storage/` (Preferences, Data Providers)

---

## 09 - Android Best Practices

Ensures:

- Lifecycle safety
- AGP 9 / Gradle 9 standards
- R8 optimization (.keep files)
- Permission handling
- API compatibility (24-37)

---

## 10 - Feature Development

A senior-engineer workflow that guides AI to:

- Analyze the project before coding
- Reuse existing building blocks in `helpers/` and `presentation/base/`
- Implement production-ready features with proper error handling
- Handle Android lifecycle and permissions

---

## 11 - Template Conventions

Catalog of reusable building blocks:

- Base classes (Activity/Fragment)
- Helper catalog (Navigation, Theme, Permissions, etc.)
- Common components (Firebase, Network, Observers)
- Adapter & Dialog standards

---

## 12 - New Project

Workflow for starting a new app:

- Identity & Branding updates
- Firebase setup
- Signing configuration
- Template cleanup

---

## 13 - Updating Project

Workflow for maintaining an existing project:

- Establishing baselines
- Dependency & SDK upgrades
- Safe migrations & deprecations
- Resource management

---

## CLAUDE.md

A comprehensive, single-file rule set specifically optimized for **Claude Code**, **Roo Code**, and **Cline**. It consolidates all project standards, tech stack details, and engineering workflows into a format that Claude can easily consume.

It also includes a set of specialized **Claude Skills** (in `.claude/skills/`) for:
- Feature development
- UI & Theming
- Template helpers catalog
- Build configuration & R8
- New project setup
- Project updates & migrations
- Pre-flight verification

---

# 💡 Why These Rules?

Most AI assistants are excellent at generating code, but they don't automatically understand your project's architecture or engineering standards.

These rules ensure generated code is:

- Consistent
- Maintainable
- Scalable
- Production-ready
- Easy to review
- Easy to test

---

# 🚀 Installation

Clone or copy the `.cursor/rules` and `.claude/CLAUDE.md` into your Android project.

```
Project
│
├── .cursor
│   └── rules
│       ├── 01-tech-stack.mdc
│       ├── ...
│       └── 13-updating-project.mdc
└── .claude
    └── CLAUDE.md
```

Cursor will automatically load these rules for your project.

---

# 🤖 Compatible AI Assistants

Although created for Cursor, these rules can be adapted for:

- Cursor
- Claude Code
- GitHub Copilot
- Gemini CLI
- Windsurf
- Roo Code
- Continue.dev
- Cline
- OpenHands
- Any AI coding assistant that supports project-level instructions

---

# 🤝 Contributing

Contributions are welcome!

Feel free to:

- Improve existing rules
- Add new rule files
- Share Android best practices
- Suggest optimizations
- Report issues

Please open an issue or submit a pull request.

---

# ⭐ If You Find This Helpful

If this repository improves your AI-assisted Android development workflow, consider giving it a ⭐ to support the project.

---

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

Copyright OrbitalSonic

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
