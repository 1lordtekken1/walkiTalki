# MVP roadmap and remaining gates

Date: 2026-05-11.

## Short answer

There are **8 major implementation/validation steps** remaining before a credible MVP.

The repository is past the framework-free core bootstrap: protocol, state machine, permissions, diagnostics, jitter buffer, fake/stream transports, heartbeat, bounded discovery, and browser preview are in place. The MVP is not yet close enough to call "almost done" because the remaining work includes Android UI, real Bluetooth, physical two-device stability, and real audio capture/playback.

## MVP definition

For this project, MVP means:

- two nearby Android phones can discover/connect without internet;
- one user can hold Push-To-Talk and the other can hear audio;
- permission, pairing, disconnect, and busy states are understandable;
- privacy-safe diagnostics can be copied/exported for support;
- the transport can survive a short two-device smoke test without false success from emulators or fakes.

## Remaining steps

### 1. Android Gradle module and manifest shell

Add an Android app module once an Android SDK is available in the environment. The module must compile without adding Bluetooth/audio business logic directly to Activities or Compose.

**Acceptance gate:** Android module builds and can depend on `:core`.

**Rollback trigger:** Android module requires core to import Android framework classes.

**Diagnostics signal:** app/version/build metadata appears in diagnostics without device identifiers.

### 2. Compose UI over `TalkScreenPresenter`

Create a thin Compose surface that renders `TalkScreenState` from `WalkieTalkieSessionController` snapshots and routes user intents through ViewModel/application seams. Compose must not call Bluetooth or audio APIs directly.

**Acceptance gate:** UI states match core presenter/preview states for permission-blocked, ready, talking, receiving, busy, disconnected, and over-budget flows.

**Rollback trigger:** Compose duplicates state rules already owned by `TalkScreenPresenter`.

**Diagnostics signal:** UI emits only coarse signals such as `ui_ready:ptt_enabled` and `ui_reconnect:DISCONNECTED`.

### 3. Browser/screenshot automation for preview states

Add automated visual/state checks over `preview/build/reports/talk-screen-preview.html` or Android previews when a browser/Android test runner is available.

**Acceptance gate:** generated preview is checked in CI/test automation for required states and click-budget markers.

**Rollback trigger:** visual tests become flaky or require real Bluetooth/audio.

**Diagnostics signal:** preview diagnostics hooks remain metadata-only.

### 4. Android Bluetooth permission and environment adapter

Implement Android adapters for permission status and Bluetooth readiness behind existing core seams.

**Acceptance gate:** Android 11 legacy discovery and Android 12+ Nearby Devices flows are represented without changing `PermissionPolicy` semantics.

**Rollback trigger:** permission checks are scattered through UI/transport code instead of a dedicated adapter/policy path.

**Diagnostics signal:** permission state is exportable without raw device identifiers.

### 5. Bluetooth Classic wrapper around `StreamVoiceTransport`

Wrap `BluetoothSocket` input/output streams behind the existing transport seam, reusing `StreamVoiceTransport`, `TransportHeartbeatController`, and `PeerDiscoveryController`.

**Acceptance gate:** two physical devices can exchange `PING/PONG` using the real wrapper.

**Rollback trigger:** real socket code leaks into domain/UI layers, or connection health cannot be represented by `TransportSessionResult`.

**Diagnostics signal:** connection timeline reports frame type, sequence, byte counts, health, and coarse failures only.

### 6. Two-device `PING/PONG` stability gate

Run a physical-device matrix before adding realtime audio transport. This is the first point where emulator-only confidence is insufficient.

**Acceptance gate:** sustained two-device `PING/PONG` run with no false stable state after disconnect and no unbounded reconnect loop.

**Rollback trigger:** frequent false disconnects, pairing dead ends, or unacceptable battery drain during scan windows.

**Diagnostics signal:** heartbeat/discovery reports are copyable and redacted.

### 7. Android audio adapters behind `AudioInput` and `AudioOutput`

Implement `AudioRecord` and `AudioTrack` adapters only after the data-slice transport is stable. Keep capture/playback behind the existing audio seams, `PttAudioController`, and jitter buffer.

**Acceptance gate:** local fake/audio adapter tests prove start/stop, no capture before permission, bounded buffers, underrun counters, and no raw payload logging.

**Rollback trigger:** UI/domain code calls `AudioRecord` or `AudioTrack` directly.

**Diagnostics signal:** audio diagnostics expose counts, sequence numbers, underruns, and payload byte lengths only.

### 8. End-to-end PTT MVP smoke test and release checklist

Combine permissions, UI, Bluetooth data slice, heartbeat, discovery, and audio into a constrained MVP flow.

**Acceptance gate:** two users can complete scan/connect/hold-to-talk/release/disconnect within the click budget on physical devices.

**Rollback trigger:** happy path requires more than the allowed action budget, diagnostics are insufficient for support, or audio latency/reliability is below the documented MVP threshold.

**Diagnostics signal:** a support export contains app/build, permission state, transport states, counters, and redacted error reasons.

## Current blockers

- No Android SDK is available in the current environment, so Android module/Compose/Bluetooth wrapper work cannot be verified here yet.
- No browser binary is available in the current environment, so screenshot automation cannot be captured here yet.
- No physical devices are attached, so Bluetooth and realtime audio gates remain future/manual-device gates.

## Quality bar before calling it MVP

Do not call the app MVP until steps 1 through 8 pass. The current codebase is best described as a **strong pre-MVP core and preview foundation**, not an Android MVP.
