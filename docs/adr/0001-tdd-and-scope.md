# ADR 0001: TDD-first Milestone 0 scope

Date: 2026-05-09.
Status: Accepted.

## Context

The project is starting from a Bluetooth-first Android walkie-talkie hypothesis, but the second readiness review approved only Milestone 0. The riskiest implementation areas are Bluetooth compatibility, pairing UX, realtime audio latency, privacy-safe diagnostics, Android permissions, and future transport pivots.

Starting directly with `BluetoothSocket`, `AudioRecord`, `AudioTrack`, Compose screens, or Opus would create hard-to-test code before the protocol, diagnostics, and domain seams are stable.

## Decision

Milestone 0 starts with a dependency-light JVM `core` module and executable tests. The initial code is intentionally framework-free:

- protocol frame encoding/decoding;
- diagnostics redaction;
- no Android framework calls;
- no Bluetooth implementation;
- no microphone or playback implementation;
- no copied code from researched repositories.

The first test harness uses Gradle's built-in Java plugin and a dependency-free test runner because external Gradle plugin resolution is unreliable in the current environment. Android/Compose modules may be added after the core behavior and build baseline are stable.

## Consequences

Positive:

- `gradle test` can run without downloading third-party test frameworks;
- protocol and diagnostics behavior is testable before Android integration;
- the project keeps the TDD contract from the first implementation commit.

Negative:

- this is not yet an installable Android application;
- JUnit/Kotlin/Android plugins still need to be introduced when dependency resolution and Android SDK setup are available;
- Compose design is documented now, but not implemented as UI components in this milestone.

## Follow-up decisions

- ADR 0002 should define protocol framing limits and version negotiation in more detail.
- ADR 0003 should define diagnostics event taxonomy and redaction policy.
- ADR 0004 should decide Android Gradle plugin, Kotlin, Compose, minSdk, and targetSdk once the Android skeleton is added.
