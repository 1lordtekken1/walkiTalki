# ADR 0007: Heartbeat controller with clock and diagnostics seams

Date: 2026-05-11.
Status: Accepted.

## Context

The next transport milestone is a real Bluetooth Classic data slice behind `VoiceTransport`. Before any platform socket code is introduced, heartbeat timing and diagnostics must be deterministic and testable without real delays, Android handlers, or raw peer/audio logging.

The repository rules also require `Clock` and `DiagnosticsSink` seams for transport/audio/state-machine work.

## Decision

Add three framework-free core seams:

- `Clock`, with `FakeClock` for deterministic JVM tests;
- `DiagnosticsSink`, with `RecordingDiagnosticsSink` for redacted assertions;
- `TransportHeartbeatController`, which drives `VoiceTransport.sendPing(...)` from an injected clock and records privacy-safe heartbeat/disconnect diagnostics.

The controller sends the first heartbeat immediately, sends later heartbeats only after the configured `OperationModePolicy.heartbeatIntervalMillis()`, and records a `CONNECTED -> DISCONNECTED` diagnostics transition when missed heartbeat windows move transport health to disconnected.

## Acceptance criteria

- Heartbeat tests must advance `FakeClock`; they must not sleep or rely on wall-clock time.
- Heartbeat diagnostics may include sequence number, coarse health, and missed heartbeat window count only.
- Disconnect diagnostics must not include peer IDs, MAC addresses, device names, or audio payloads.
- Future Bluetooth transports can share the same heartbeat controller without exposing Android socket classes to domain, UI, or preview layers.

## Rollback / pivot trigger

Rollback or redesign this controller if a real transport needs Android framework timers in domain code, if diagnostics start carrying sensitive peer/audio data, or if physical two-device `PING/PONG` tests show the selected heartbeat interval causes false disconnects.

## Diagnostics signal

The heartbeat layer records `TransportHeartbeat(sequenceNumber=..., health=..., missedHeartbeatWindows=...)` and `TransportStateChanged(CONNECTED->DISCONNECTED, reason=heartbeat_missed)`. These are coarse signals intended for support/debugging without exposing raw identifiers.

## Consequences

Positive:

- Real transport work can reuse a deterministic heartbeat loop instead of inventing platform-specific timing logic.
- Test code can simulate long time windows instantly.
- Diagnostics remain redacted by construction.

Negative:

- The controller only covers data-slice heartbeat behavior; it does not prove physical Bluetooth stability.
- Heartbeat interval choices still require two-device testing before audio transport begins.
