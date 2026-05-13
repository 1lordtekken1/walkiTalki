# ADR 0016: Bluetooth Classic stream adapter seam

Date: 2026-05-13

## Status

Accepted

## Context

MVP roadmap gate 5 requires a Bluetooth Classic wrapper around `StreamVoiceTransport`, but this environment currently has no physical devices and no Android SDK path for instrumented validation. The next safe TDD step is to prove that platform socket input/output streams can be wrapped behind `VoiceTransport` without importing Android framework classes into `:core` or logging raw peer identifiers.

## Decision

Add `ClassicBluetoothStreamAdapter` in the core transport layer. It:

- accepts generic `InputStream`/`OutputStream` objects that a future Android Bluetooth boundary can provide;
- delegates protocol reads/writes to `StreamVoiceTransport` and exposes it as `VoiceTransport`;
- adds a coarse diagnostics signal with a short `peerHash` instead of raw MAC address, raw peer ID, or unredacted device name;
- preserves the ability to swap the future platform boundary to Nearby Connections or Wi‑Fi Direct because the core adapter only knows streams and `VoiceTransport`.

## Acceptance criteria

- JVM tests prove `sendPing` writes a valid protocol `PING` through the stream-backed transport.
- JVM tests prove adapter diagnostics and snapshots contain `peerHash=` but not raw MAC addresses or device names.
- Core/preview architecture guard remains clean: no Android framework, `BluetoothSocket`, `AudioRecord`, or `AudioTrack` references in protected roots.

## Rollback/pivot trigger

Rollback or redesign this seam if the real Android socket wrapper needs lifecycle/error semantics that cannot be represented by `VoiceTransport`, `TransportSessionResult`, `ConnectionHealthMonitor`, and redacted diagnostics.

## Diagnostics signal

Diagnostics may include `classic_stream:connected`, connection time, frame counts, and `peerHash`. They must not include raw MAC addresses, raw peer IDs, unredacted device names, or raw audio payloads.
