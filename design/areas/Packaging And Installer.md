# Packaging And Installer
#type/area #area/packaging #area/platform #status/draft

Links: [[../DDS]], [[Platform]]

## Purpose

Packaging And Installer tracks the eventual production packaging path for
Commander Anthology on Windows.

The target direction is to ship the completed desktop app as an MSI or
equivalent Windows installer rather than requiring users to run Gradle or manage
developer tooling.

## Direction

The installer should make the app feel self-contained and ordinary to install.

Initial packaging concerns:

- bundle or install a suitable Java runtime
- avoid relying on a preconfigured `JAVA_HOME`
- avoid relying on whichever `java.exe` happens to appear first on PATH
- install the desktop app and launcher cleanly
- create Start Menu/Desktop shortcuts when appropriate
- preserve user data under `%APPDATA%\Commander Anthology`
- keep generated caches and user data separate from installed application files
- support uninstall without deleting user-owned decks, collection data, or
  exported bundles by default

## Dependency Install

The launcher hardening is a development-era safety net. The finished installer
should either:

- bundle a known-good Java runtime with the application, or
- install/check a required runtime as part of setup with clear user messaging.

The preferred final choice should be made after the desktop app is closer to
release shape, because packaging may be affected by chosen UI/runtime tooling,
SQLite/native library needs, code signing, update strategy, and installer size.

## Open Questions

- Should the MSI bundle a JRE/JDK runtime, or require/install one?
- Should packaging use Gradle application distribution, `jpackage`, WiX, or
  another MSI toolchain?
- How should app updates work?
- Will installers be signed?
- Which generated caches, if any, should be preseeded with the installer?
- What should uninstall preserve versus remove?
