# ADR 0004: Protocol stream reader and transport seam

Date: 2026-05-09.
Status: Accepted.

## Context

Bluetooth RFCOMM and other transports expose byte streams where a single read does not guarantee a full application frame. The project needs stream-read behavior and a transport seam before any real `BluetoothSocket` code is introduced.

## Decision

Add two framework-free core pieces:

- `ProtocolStreamReader`, which reads exact protocol frame bytes from an `InputStream` and supports frames split across multiple reads or concatenated in one stream;
- `VoiceTransport`, a small interface for sending `PING`, receiving `PONG`, discovering a paired peer through the Bluetooth environment seam, sending/receiving audio frames as protocol metadata, disconnecting, and taking a session snapshot.

`FakeVoiceTransport` now implements `VoiceTransport`, so future Android transports must match the same contract while preserving protocol counters, connection health, discovery state, audio-frame counters, and timeline behavior.

## Consequences

Positive:

- partial stream behavior is tested before Bluetooth integration;
- fake and real transports can share acceptance tests;
- Android Bluetooth code can stay behind a seam instead of leaking into domain/UI layers.

Negative:

- `ProtocolStreamReader` now maps invalid magic, oversized payloads, EOF, and `IOException` failures into protocol exceptions, but still does no resynchronization after a corrupt frame;
- the `VoiceTransport` interface now models discovery and audio-frame metadata, but no Android `BluetoothSocket`, `AudioRecord`, or `AudioTrack` code exists yet.

## Follow-up decisions

- Add an Android transport implementation behind this seam only after Android module setup and permission UI are ready.
- Keep audio payloads out of diagnostics and fake transport timelines; expose only frame type, sequence number, and byte counts.
- Run physical two-device `PING/PONG` stability tests before treating this seam as ready for realtime audio.
