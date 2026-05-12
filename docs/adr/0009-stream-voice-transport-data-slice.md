# ADR 0009: Stream-backed voice transport data slice

Date: 2026-05-11.
Status: Accepted.

## Context

A real Bluetooth Classic implementation will ultimately wrap `BluetoothSocket` input/output streams. The project is not ready to add Android framework production code until stream behavior is covered by deterministic JVM tests and the transport interface has a non-fake byte-stream implementation.

The existing fake transport proves counters and metadata, while `ProtocolStreamReader` proves framing. The missing seam is a transport that writes and reads protocol frames through plain Java streams.

## Decision

Add `StreamVoiceTransport`, a framework-free `VoiceTransport` implementation backed by `InputStream` and `OutputStream`.

`StreamVoiceTransport` must:

- encode outgoing `PING` and `AUDIO` frames with `ProtocolFrameCodec` before writing bytes;
- read incoming frames with `ProtocolStreamReader`;
- update heartbeat health when a `PONG` is read;
- count sent, received, audio, and send-failure metrics consistently with `TransportSessionResult`;
- keep timeline diagnostics to frame type, sequence number, byte count, and coarse write/read failures only;
- avoid Android, Bluetooth, Compose, peer identifiers, device names, and raw audio payload logging.

## Acceptance criteria

- JVM tests can assert exact bytes written to an `OutputStream` decode to a valid protocol frame.
- JVM tests can feed concatenated `PONG` and `AUDIO` frames through an `InputStream` and observe health/counter updates.
- Write failures increment `sendFailures` without counting a frame as sent.
- Timeline output never includes raw audio payload bytes.

## Rollback / pivot trigger

Rollback or redesign this data slice if wrapping real `BluetoothSocket` streams requires Android framework types to leak into `core`, or if two-device `PING/PONG` stability tests show the stream transport counters/health do not reflect real socket behavior.

## Diagnostics signal

The stream transport timeline may include signals such as `write:PING#42`, `read:PONG#7`, `read:AUDIO#8 bytes=3`, and `write_failed:<reason>`. It must not include raw peer identifiers, MAC addresses, device names, or audio bytes.

## Consequences

Positive:

- The next Android Bluetooth slice can wrap socket streams behind an already-tested transport.
- Protocol bytes, counters, and failure paths are covered without physical devices.
- The MVP transport remains replaceable because it still implements `VoiceTransport`.

Negative:

- This does not create a `BluetoothSocket` implementation.
- Real device tests are still required before accepting Bluetooth stability or audio transport.
