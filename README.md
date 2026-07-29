# 🚀 Android Engineering Rules

> Production-ready AI coding rules for Android development.
>
> A collection of reusable Cursor Rules (`.cursor/rules`) that help AI assistants generate clean, maintainable, and production-ready Android code.

---

## 🎯 Purpose

These rules transform AI coding assistants into experienced Android engineers by enforcing:

- Modern Android development practices
- Clean Architecture
- MVI architecture
- Kotlin best practices
- XML + ViewBinding
- Material Design
- Performance optimization
- Lifecycle safety
- Error handling
- Production-ready implementations

Instead of repeatedly explaining your project standards, simply include these rules in your project.

---

# 📦 Tech Stack

These rules are designed for projects using:

- Kotlin
- XML Layouts
- ViewBinding
- MVI Architecture
- Clean Architecture
- Koin
- Kotlin Coroutines
- StateFlow
- Jetpack Navigation
- Material Components

Supported Android versions:

- Minimum SDK: 24
- Target SDK: 37

---

# 📁 Project Structure

```
.cursor/
└── rules/
    ├── 01-tech-stack.mdc
    ├── 02-architecture.mdc
    ├── 03-ui.mdc
    ├── 04-kotlin.mdc
    ├── 05-performance.mdc
    ├── 06-error-handling.mdc
    ├── 07-testing.mdc
    ├── 08-project-structure.mdc
    ├── 09-android-best-practices.mdc
    └── 10-feature-development.mdc
```

---

# 📖 Rule Overview

## 01 - Tech Stack

Defines the project's technology stack including:

- Kotlin
- XML
- ViewBinding
- Koin
- MVI
- Clean Architecture
- Android SDK versions

---

## 02 - Architecture

Enforces:

- MVI
- Clean Architecture
- SOLID Principles
- Separation of Concerns
- Repository Pattern
- Use Cases
- Immutable UI State

---

## 03 - UI

Defines UI standards:

- XML only
- Material Design
- ViewBinding
- Dark Mode
- System Theme
- Accessibility
- Responsive layouts

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

Defines a consistent package structure:

- presentation
- domain
- data
- di
- utils

---

## 09 - Android Best Practices

Ensures:

- Lifecycle safety
- Permission handling
- API compatibility
- Background execution
- Android platform recommendations

---

## 10 - Feature Development

A senior-engineer workflow that guides AI to:

- Analyse the project before coding
- Reuse existing components
- Consider Android lifecycle
- Handle permissions
- Implement edge cases
- Produce complete, production-ready features

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

Clone or copy the `.cursor/rules` directory into your Android project.

```
Project
│
├── .cursor
│   └── rules
│       ├── 01-tech-stack.mdc
│       ├── ...
│       └── 10-feature-development.mdc
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
