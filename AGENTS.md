# walkiTalki agent rules

These instructions apply to the whole repository.

## Development mode: TDD first

1. Start every implementation task by writing or updating tests that describe the desired behavior.
2. Do not add production Android/Kotlin code before a failing test, unless the task is documentation, build setup, or a test harness prerequisite.
3. Keep the Red-Green-Refactor loop explicit in PR descriptions:
   - Red: which test failed first and why.
   - Green: the smallest implementation that made it pass.
   - Refactor: cleanup performed without changing behavior.
4. Prefer deterministic JVM unit tests for protocol, state machine, permissions policy, diagnostics, and audio-buffer logic.
5. Use instrumented tests only for platform behavior that cannot be trusted in JVM tests: Bluetooth permissions, real `BluetoothSocket`, `AudioRecord`, `AudioTrack`, foreground service, and device-specific behavior.
6. Every Bluetooth, audio, protocol, and permission feature must expose test seams through interfaces/fakes before it talks to Android framework classes directly.
7. No feature is accepted without acceptance criteria, a rollback/pivot trigger, and a diagnostics signal.

## Architecture guardrails

- Keep transport, protocol, audio, diagnostics, domain state machine, and UI as separate layers.
- Do not let Compose UI call Bluetooth or audio APIs directly.
- Do not log raw audio payloads, raw peer identifiers, MAC addresses, or unredacted device names.
- Bluetooth Classic RFCOMM is an MVP hypothesis, not a permanent commitment; preserve the ability to test Nearby Connections or Wi-Fi Direct later.
- BLE must not be used as the primary realtime audio transport unless a benchmark proves latency, throughput, and reliability on physical devices.

## Research and borrowing rules

- Existing repositories may inform architecture, tests, and UX, but do not copy code without checking license compatibility and attribution requirements.
- Treat inactive or old Android repositories as pattern sources, not as dependency candidates.
- Convert borrowed ideas into project-specific ADRs and tests before implementation.
