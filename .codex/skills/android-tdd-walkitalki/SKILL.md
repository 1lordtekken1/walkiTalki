---
name: android-tdd-walkitalki
description: Use when implementing or reviewing walkiTalki Android features. Enforces TDD-first development for Bluetooth, audio, protocol, permissions, diagnostics, Compose UI, and coroutine state-machine work.
---

# Android TDD for walkiTalki

## Mandatory workflow

1. Identify the behavior and write the smallest failing test first.
2. Prefer pure JVM tests for protocol, state machine, diagnostics, permissions policy, jitter buffer, and retry logic.
3. Add production code only after the failing test exists.
4. Keep Android framework classes behind interfaces/fakes.
5. Run the narrowest relevant test, then the broader suite available in the repo.
6. Document Red/Green/Refactor evidence in the PR.

## Test selection

- Protocol/framing: JVM unit tests with byte streams, partial reads, invalid frames, max lengths, versioning.
- State machine: JVM tests with fake transport, fake audio, fake clock, test dispatcher.
- Coroutines: `runTest`; inject dispatchers/scopes; avoid real delays.
- Permissions: pure policy tests first, instrumented tests only for platform grant behavior.
- Bluetooth/audio: fake interfaces first; physical-device smoke tests for real `BluetoothSocket`, `AudioRecord`, and `AudioTrack`.
- UI: ViewModel/state tests before Compose rendering tests.

## Architecture seams

Require interfaces for:

- `VoiceTransport`
- `BluetoothEnvironment`
- `AudioInput`
- `AudioOutput`
- `Clock`
- `DiagnosticsSink`
- coroutine dispatchers or scopes

Do not call Android Bluetooth/audio APIs directly from Compose or domain logic.

## Done criteria

A change is incomplete if it lacks:

- tests or explicit test-not-applicable rationale;
- privacy/diagnostics assessment for audio, peer, permission, or logging changes;
- exact commands run;
- an ADR update when architecture changes.
