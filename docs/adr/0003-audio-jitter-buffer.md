# ADR 0003: Framework-free audio frame and jitter buffer baseline

Date: 2026-05-09.
Status: Accepted.

## Context

The development rules require audio-buffer tests before any `AudioRecord` or `AudioTrack` integration. Realtime walkie-talkie audio will face out-of-order frames, duplicates, missing frames, bounded memory, and playback underruns. These behaviors must be deterministic before platform audio APIs are introduced.

## Decision

Add a pure JVM audio baseline:

- `AudioFrame` stores sequence number, sender timestamp, and immutable PCM payload bytes;
- `JitterBuffer` reorders frames by sequence number;
- duplicate and already-played frames are dropped;
- capacity overflow drops the oldest buffered frame;
- empty playback polls increment underrun counters;
- `JitterBufferSnapshot` exposes capacity, buffered frames, played frames, dropped frames, underruns, and last played sequence number.

The buffer is intentionally simple and does not yet implement adaptive playout delay, time-based expiration, codec-specific behavior, or concealment. Those decisions should be based on measured Bluetooth/data-slice behavior.

## Consequences

Positive:

- audio ordering/drop behavior is executable and testable without Android framework APIs;
- diagnostics counters are available before real playback;
- later audio code can wrap `AudioRecord` and `AudioTrack` behind interfaces and feed this buffer.

Negative:

- no real latency guarantee yet;
- no packet-loss concealment yet;
- no Opus or codec-frame semantics yet;
- device-specific audio behavior still requires instrumented and physical-device tests later.

## Follow-up decisions

- Define `AudioInput` and `AudioOutput` interfaces before Android audio integration.
- Add time-window based expiration after transport timestamps and clock seams are finalized.
- Benchmark PCM frame sizes before Opus work.
