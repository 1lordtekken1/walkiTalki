# Development progress

Date: 2026-05-09.

## Current stage

The project is in **Milestone 1: framework-free core data slice**.

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
- browser-runnable `:preview` module over `TalkScreenPresenter`, with executable HTML state, diagnostics-hook, privacy, and click-budget checks;
- deterministic `Clock`, `DiagnosticsSink`, and `TransportHeartbeatController` seams for heartbeat timing and redacted transport diagnostics before real Bluetooth work;
- bounded `PeerDiscoveryController` with scan-window, Bluetooth readiness, permission-block, and privacy-safe diagnostics coverage;
- framework-free `StreamVoiceTransport` that reads/writes protocol frames over Java streams before wrapping `BluetoothSocket` streams;
- framework-free `PttAudioController` coordinating PTT capture/send, remote playback enqueue, busy-state no-capture, and redacted audio diagnostics before `AudioRecord`/`AudioTrack`;
- framework-free `WalkieTalkieSessionController` composing permissions, Bluetooth readiness, bounded discovery, PTT state, transport health, UI state, click budget, and redacted diagnostics before Android ViewModel/Compose handlers;
- executable architecture guard in the core JVM harness that prevents Android/Bluetooth/audio framework references from leaking into `:core` or `:preview`.

## What remains before Android/Bluetooth production work

The detailed MVP count is tracked in [MVP Roadmap](mvp-roadmap.md): **8 major implementation/validation steps remain** before a credible MVP.

Near-term technical sequence:

1. Add Android Gradle module and Compose design-preview surface over `WalkieTalkieSessionController`/`TalkScreenPresenter` once an Android SDK is available.
2. Add screenshot/browser automation over `preview/build/reports/talk-screen-preview.html` when a browser driver is available.
3. Add real Bluetooth Classic wrapper around `StreamVoiceTransport` and reuse `TransportHeartbeatController` plus `PeerDiscoveryController` for `PING/PONG` timing and bounded discovery.
4. Run two-device `PING/PONG` stability tests before any audio transport.
5. Add real `AudioRecord`/`AudioTrack` adapters behind `AudioInput`/`AudioOutput` only after the data slice is stable, reusing `PttAudioController`.

## Current no-go items

- No real Bluetooth implementation yet.
- No real Android microphone/playback implementation yet.
- No Opus integration yet.
- No group/mesh mode yet.
- No background mode yet.
