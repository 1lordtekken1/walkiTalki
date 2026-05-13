# MVP roadmap and remaining gates

Date: 2026-05-13.

## Short answer

The roadmap still tracks **8 MVP gates**, but gate 1 now has a checked-in debug app shell and gate 4 now has a JVM-tested Android permission/environment adapter. The remaining risky work is **6 major implementation/validation gates** plus Compose/visual hardening before a credible MVP.

The repository is past the framework-free core bootstrap: protocol, state machine, permissions, diagnostics, jitter buffer, fake/stream transports, heartbeat, bounded discovery, browser preview, debug Android shell, redacted smoke harness, and Android permission/environment adapter are in place. The MVP is not yet close enough to call "almost done" because the remaining work includes production UI integration, real Bluetooth, physical two-device stability, and real audio capture/playback.

## MVP definition

For this project, MVP means:

- two nearby Android phones can discover/connect without internet;
- one user can hold Push-To-Talk and the other can hear audio;
- permission, pairing, disconnect, and busy states are understandable;
- privacy-safe diagnostics can be copied/exported for support;
- the transport can survive a short two-device smoke test without false success from emulators or fakes.

## Executable progress source of truth

The current gate-by-gate status is mirrored in [MVP Progress Tracker](mvp-progress.md) and backed by `MvpProgressTracker`/`MvpProgressReportRenderer` in `:core`. Stakeholder attack/readiness is now backed by executable `StakeholderRedTeamReview` and tracked in [Stakeholder Red-Team Review](stakeholder-red-team-review.md), which gives each stakeholder a score, attack, evidence gap, and route to 100. Update those executable sources and run `./gradlew :core:test :core:renderMvpProgressReport :core:renderStakeholderRedTeamReview --no-daemon` whenever a gate status changes so docs do not drift from code.

## Remaining steps

### 1. Android Gradle module and manifest shell — debug shell implemented, production hardening remains

A minimal Android app module and manifest shell now compile against the local Android SDK and depend on `:core`. This gate remains open for production hardening, lifecycle wiring, and UI adapter integration, but it is no longer a zero-code blocker.

**Acceptance gate:** Android module builds and can depend on `:core`.

**Rollback trigger:** Android module requires core to import Android framework classes.

**Diagnostics signal:** app/version/build metadata appears in diagnostics without device identifiers.

### 2. Compose UI over `TalkScreenPresenter` — UI intent seam implemented, Compose renderer remains

`AppTalkViewModel` now routes primary action and PTT press/release intents through the app/controller/presenter path, and `AppTalkUiRenderer` can render `TalkScreenState` into `AppTalkRenderModel` for permission-blocked, ready, talking, receiving, busy, disconnected, and over-budget states without Android views mutating controller state directly. The production Compose runtime surface still needs to bind this render model and route user intents through the seam. Compose must not call Bluetooth or audio APIs directly.

**Acceptance gate:** UI states match core presenter/preview states for permission-blocked, ready, talking, receiving, busy, disconnected, and over-budget flows in both JVM render-model tests and Android UI/screenshot checks.

**Rollback trigger:** Compose duplicates state rules already owned by `TalkScreenPresenter`.

**Diagnostics signal:** UI emits only coarse signals such as `ui_ready:ptt_enabled` and `ui_reconnect:DISCONNECTED`.

### 3. Browser/screenshot automation for preview states — deterministic baseline manifest implemented, pixel runner remains

`PreviewAutomationAudit` now checks the rendered talk-screen preview for required MVP states, stable automation hooks, click-budget evidence, and privacy-safe diagnostics without needing a browser binary. `PreviewVisualBaseline` also records a deterministic SHA-256 manifest for the generated HTML so CI can detect preview drift without storing screenshots. Browser/Android pixel baseline capture remains open until a runner is available.

**Acceptance gate:** generated preview is checked in CI/test automation for required states, stable hooks, click-budget markers, privacy constraints, deterministic baseline drift, and later browser/Android pixel baselines.

**Rollback trigger:** visual tests become flaky or require real Bluetooth/audio.

**Diagnostics signal:** preview diagnostics hooks remain metadata-only.

### 4. Android Bluetooth permission and environment adapter — JVM adapter implemented, instrumented tests remain

`AndroidPermissionEnvironmentAdapter` now maps Android manifest permission names into the core `PermissionPolicy`, reports `BluetoothEnvironmentState`, and emits redacted diagnostics behind a testable `PermissionChecker` seam. Physical/instrumented Android grant behavior still needs API-version validation.

**Acceptance gate:** Android 11 legacy discovery and Android 12+ Nearby Devices flows are represented without changing `PermissionPolicy` semantics.

**Rollback trigger:** permission checks are scattered through UI/transport code instead of a dedicated adapter/policy path.

**Diagnostics signal:** permission state is exportable without raw device identifiers.

### 5. Bluetooth Classic wrapper around `StreamVoiceTransport` — stream adapter seam implemented, physical wrapper remains

`ClassicBluetoothStreamAdapter` now wraps platform socket input/output streams behind the existing `VoiceTransport` seam and redacts raw peer identifiers into `peerHash` diagnostics. A real Android socket boundary and two-device evidence remain open, while the seam still reuses `StreamVoiceTransport`, `TransportHeartbeatController`, and `PeerDiscoveryController`.

**Acceptance gate:** two physical devices can exchange `PING/PONG` using the real wrapper without raw peer identifiers in diagnostics.

**Rollback trigger:** real socket code leaks into domain/UI layers, or connection health cannot be represented by `TransportSessionResult`.

**Diagnostics signal:** connection timeline reports frame type, sequence, byte counts, health, and coarse failures only.

### 6. Two-device `PING/PONG` stability gate — executable plan added, physical matrix remains

Run a physical-device matrix before adding realtime audio transport. `PingPongStabilityPlan` now makes the sustained exchange, disconnect, weak-signal, pairing retry, and diagnostics evidence requirements executable in `:core`, but the gate remains blocked until that plan runs on physical devices. This is the first point where emulator-only confidence is insufficient.

**Acceptance gate:** executable `PingPongStabilityPlan` is satisfied by a sustained two-device `PING/PONG` run with no false stable state after disconnect and no unbounded reconnect loop.

**Rollback trigger:** frequent false disconnects, pairing dead ends, or unacceptable battery drain during scan windows.

**Diagnostics signal:** heartbeat/discovery reports include frame counters, missed heartbeat windows, disconnect/weak-signal health transitions, and redacted support metadata only.

### 7. Android audio adapters behind `AudioInput` and `AudioOutput`

Implement `AudioRecord` and `AudioTrack` adapters only after the data-slice transport is stable. Keep capture/playback behind the existing audio seams, `PttAudioController`, and jitter buffer.

**Acceptance gate:** local fake/audio adapter tests prove start/stop, no capture before permission, bounded buffers, underrun counters, and no raw payload logging.

**Rollback trigger:** UI/domain code calls `AudioRecord` or `AudioTrack` directly.

**Diagnostics signal:** audio diagnostics expose counts, sequence numbers, underruns, and payload byte lengths only.

### 8. End-to-end PTT MVP smoke test and release checklist — executable checklist added, physical smoke remains

Combine permissions, UI, Bluetooth data slice, heartbeat, discovery, audio, support export, and release operations into a constrained MVP flow. `ReleaseChecklist` now makes the NO_GO release criteria executable in `:core`, but every physical-device and operations item remains blocked until real evidence exists.

**Acceptance gate:** two users can complete scan/connect/hold-to-talk/release/disconnect within the click budget on physical devices, and every executable release checklist item is `GO`.

**Rollback trigger:** happy path requires more than the allowed action budget, diagnostics are insufficient for support, or audio latency/reliability is below the documented MVP threshold.

**Diagnostics signal:** a support export contains app/build, permission state, transport states, counters, release checklist status, and redacted error reasons.

## Current blockers

- Production Compose/ViewModel UI is not implemented yet.
- Android permission/environment behavior has JVM coverage, but still needs instrumented API-version grant/deny validation.
- No browser binary is available in the current environment, so screenshot automation is generated locally but not browser-validated here.
- No physical devices are attached, so Bluetooth Classic, realtime audio, weak-signal, disconnect, and end-to-end PTT gates remain future/manual-device gates.

## Quality bar before calling it MVP

Do not call the app MVP until all 8 gates pass on physical devices and the executable stakeholder red-team review no longer reports `NO_GO`. Every stakeholder must have a route-to-100 item backed by evidence, not assertion. The current codebase is best described as a **pre-MVP core + debug Android shell with a tested permission/environment adapter**, not an Android MVP.
