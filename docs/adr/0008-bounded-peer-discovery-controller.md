# ADR 0008: Bounded peer discovery controller

Date: 2026-05-11.
Status: Accepted.

## Context

The next transport milestone is a real Bluetooth Classic data slice behind `VoiceTransport`. Before platform discovery code is introduced, peer discovery needs deterministic scan-window behavior, permission/power blocking behavior, and privacy-safe diagnostics.

Unbounded scans risk battery drain and confusing UX. Discovery diagnostics must not expose raw peer identifiers, MAC addresses, or device names.

## Decision

Add a framework-free `PeerDiscoveryController` that uses existing seams only:

- `VoiceTransport` for paired-peer discovery;
- `BluetoothEnvironment` for readiness/permission/power state;
- `OperationModePolicy.scanWindowMillis()` for bounded scan duration;
- `Clock` for deterministic elapsed-time checks;
- `DiagnosticsSink` for redacted diagnostics.

The controller starts a scan for a paired peer, reports whether it is active, records whether a peer was found, stops when the configured scan window elapses, and blocks immediately when the Bluetooth environment is not ready.

## Acceptance criteria

- Tests advance `FakeClock`; no test sleeps or relies on wall-clock time.
- A permission/power-blocked scan never remains active and emits a coarse blocked reason such as `MISSING_PERMISSION`.
- An active scan stops at `scanWindowMillis()` and emits `scan_window_elapsed`.
- Diagnostics expose only coarse state, reason, elapsed milliseconds, and peer-found boolean; they must not include raw peer IDs, MAC addresses, device names, or audio payloads.

## Rollback / pivot trigger

Rollback or redesign this controller if real Bluetooth discovery requires Android framework classes in domain/UI/preview layers, if scan windows cannot be bounded without losing usability, or if physical-device testing shows unacceptable battery drain or unreliable paired-peer discovery.

## Diagnostics signal

The discovery layer records `PeerDiscovery(state=..., reason=..., elapsedMillis=..., peerDiscovered=...)`. The event intentionally has no peer identifier field.

## Consequences

Positive:

- Real Bluetooth discovery can reuse deterministic scan-window behavior.
- Battery and UX constraints become testable before platform integration.
- Discovery diagnostics stay privacy-safe by construction.

Negative:

- This does not perform Bluetooth inquiry or pairing; it only defines the framework-free controller contract.
- Physical devices still need matrix testing before accepting the Bluetooth data slice.
