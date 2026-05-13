# Development progress

Date: 2026-05-13.

## Current stage

The project is in **pre-MVP Android integration**, with framework-free core/preview, a debug Android shell, and a JVM-tested permission/environment adapter. Gate-by-gate status now lives in [MVP Progress Tracker](mvp-progress.md).

Completed core blocks:

- protocol frame encoding/decoding and safe protocol errors;
- privacy-safe diagnostics redaction;
- Android-version-aware permission policy;
- PTT domain state machine;
- user behavior simulations and click budgets;
- operation modes and connection health monitor;
- audio frame and jitter buffer baseline;
- fake transport `PING/PONG` data slice using protocol frames and connection health;
- protocol stream reader for partial and concatenated frame reads;
- expanded `VoiceTransport` seam implemented by the fake transport for heartbeat, paired-peer discovery, and audio-frame metadata;
- stream-reader edge coverage for EOF during payload, invalid magic, oversized payload headers, and IO failure mapping;
- fake transport reconnect behavior that requires explicit user intent;
- `BluetoothEnvironment`, `AudioInput`, and `AudioOutput` seams with fakes;
- framework-free talk-screen UI state presenter with click-budget, rollback-trigger, and diagnostics-signal coverage;
- checked-in Gradle wrapper plus Java-25-compatible Groovy DSL build scripts that run the dependency-free JVM harness with `javac --release 21`;
- browser-runnable `:preview` module over `TalkScreenPresenter`, with executable HTML state, diagnostics-hook, privacy, click-budget checks, a CI-friendly preview automation audit report, and a deterministic visual-baseline manifest;
- deterministic `Clock`, `DiagnosticsSink`, and `TransportHeartbeatController` seams for heartbeat timing and redacted transport diagnostics before real Bluetooth work;
- bounded `PeerDiscoveryController` with scan-window, Bluetooth readiness, permission-block, and privacy-safe diagnostics coverage;
- framework-free `StreamVoiceTransport` plus `ClassicBluetoothStreamAdapter` that read/write protocol frames over platform socket streams before a real Android socket boundary is connected;
- framework-free `PttAudioController` coordinating PTT capture/send, remote playback enqueue, busy-state no-capture, and redacted audio diagnostics before `AudioRecord`/`AudioTrack`;
- framework-free `WalkieTalkieSessionController` composing permissions, Bluetooth readiness, bounded discovery, PTT state, transport health, UI state, click budget, and redacted diagnostics before Android ViewModel/Compose handlers;
- executable architecture guard in the core JVM harness that prevents Android/Bluetooth/audio framework references from leaking into `:core` or `:preview`;
- minimal Android app shell demo flow for scan/scanning/ready/transmitting with a privacy-safe MVP support report that records only coarse diagnostics signals;
- deterministic `AppDebugHarness` and `renderMvpDebugSmokeReport` task for a local redacted MVP smoke-test report before physical Bluetooth/audio testing;
- Android permission/environment adapter that maps manifest permission names into `PermissionPolicy`, reports Bluetooth readiness, and emits coarse redacted diagnostics behind a JVM-testable seam;
- `AppTalkViewModel` UI intent seam and `AppTalkUiRenderer`/`AppTalkRenderModel` render contract that route primary action/PTT intents and presenter-owned roadmap states into accessibility metadata without Android views duplicating presenter rules;
- executable `ReleaseChecklist` that keeps MVP release status NO_GO until physical-device, support-export, privacy, and operations evidence exist;
- executable `PingPongStabilityPlan` that defines the sustained exchange, disconnect, weak-signal, pairing retry, and diagnostics matrix before realtime audio work;
- executable `StakeholderRedTeamReview` that attacks the app from 10 stakeholder viewpoints, keeps MVP verdict NO_GO, and gives each stakeholder evidence gaps plus route-to-100 criteria.

## What remains before Android/Bluetooth production work

The detailed MVP count is tracked in [MVP Roadmap](mvp-roadmap.md), [MVP Progress Tracker](mvp-progress.md), executable `ReleaseChecklist`, executable `PingPongStabilityPlan`, and executable `StakeholderRedTeamReview`: **8 gates are tracked, 6 open major gates remain**, and the project is still **NO_GO** for MVP until physical Bluetooth/audio and release gates pass.

Near-term technical sequence:

1. Bind `AppTalkRenderModel`/`TalkScreenState` to a production Compose runtime surface and add Android UI/screenshot tests for the intent seam when an Android SDK runner is available.
2. Add browser/Android pixel baseline capture over preview/screenshot states when a runner is available; until then, keep `:preview:renderTalkScreenPreviewAudit` and `:preview:renderTalkScreenVisualBaseline` passing.
3. Connect `ClassicBluetoothStreamAdapter` to a real Bluetooth Classic platform socket boundary and reuse `TransportHeartbeatController` plus `PeerDiscoveryController` for `PING/PONG` timing and bounded discovery.
4. Run `:core:renderPingPongStabilityPlan`, then execute the resulting two-device `PING/PONG` stability matrix on physical devices before any audio transport.
5. Add platform capture/playback adapters behind `AudioInput`/`AudioOutput` only after the data slice is stable, reusing `PttAudioController`.
6. Keep `:core:renderReleaseChecklist` NO_GO until physical smoke, support export, privacy review, beta operations, and device matrix evidence exist.
7. Run `:core:renderStakeholderRedTeamReview` after each gate change so every stakeholder can see current score, attack, evidence gap, and route to 100.

## Current no-go items

- No real Bluetooth implementation yet.
- No real Android microphone/playback implementation yet.
- No Opus integration yet.
- No group/mesh mode yet.
- No background mode yet.
