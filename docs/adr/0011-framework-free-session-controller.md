# ADR 0011: Framework-free session controller for future UI/ViewModel

Date: 2026-05-11.
Status: Accepted.

## Context

The next MVP steps require an Android module, Compose UI, permission adapters, Bluetooth discovery/connection, and audio adapters. The project already has separate seams for permission policy, Bluetooth environment, discovery, transport, PTT state, audio, diagnostics, and UI presentation.

Before adding an Android ViewModel or Compose event handlers, the app-level user actions need a framework-free coordinator so Compose does not duplicate business rules or call platform APIs directly.

## Decision

Add `WalkieTalkieSessionController`, a pure JVM session coordinator that composes existing core seams:

- `PermissionPolicy` for scan/connect/transmit decisions;
- `BluetoothEnvironment` for readiness state;
- `PeerDiscoveryController` for bounded discovery;
- `VoiceTransport` for transport health snapshots;
- `PttStateMachine` for domain state transitions;
- `TalkScreenPresenter` for UI state;
- `RecordingDiagnosticsSink` for redacted support output;
- `Clock` for deterministic snapshots.

The controller exposes `WalkieTalkieSessionSnapshot`, which contains only renderable state, coarse permission/bluetooth/health state, discovery/transport snapshots, user action count, UI state, and redacted diagnostics.

## Acceptance criteria

- A missing Nearby Devices permission blocks scanning, keeps discovery inactive, renders the permission-blocked screen, and does not leak the raw peer id.
- A granted happy path can scan, connect, press-to-talk, release-to-listen, remain within click budget, and emit redacted discovery diagnostics.
- The controller does not import Android framework classes and does not expose raw peer identifiers in diagnostics.
- Future Compose/ViewModel code can delegate user intents to this controller instead of duplicating permission, discovery, PTT, or UI-state rules.

## Rollback / pivot trigger

Rollback or redesign this controller if it grows Android framework dependencies, duplicates logic already owned by lower-level controllers, leaks peer identifiers/audio payloads, or prevents replacing Bluetooth Classic with another transport later.

## Diagnostics signal

Session diagnostics are the redacted aggregate from the injected `RecordingDiagnosticsSink`. They may include existing coarse events such as `PeerDiscovery(...)`, `TransportHeartbeat(...)`, and `AudioPipeline(...)`, but never raw peer IDs, MAC addresses, device names, or raw audio payloads.

## Consequences

Positive:

- Compose can be a thin renderer and intent forwarder.
- Session-level click-budget and support diagnostics can be tested without Android.
- The MVP flow has a single framework-free place to compose permissions, discovery, transport, and UI state.

Negative:

- This is not yet an Android ViewModel and does not persist across process death.
- Real permission grants, Bluetooth sockets, and audio adapters still require Android/device tests.
