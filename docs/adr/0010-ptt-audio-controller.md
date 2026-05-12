# ADR 0010: Framework-free Push-To-Talk audio controller

Date: 2026-05-11.
Status: Accepted.

## Context

The MVP still needs Android `AudioRecord` and `AudioTrack` adapters, but production audio APIs must not be added until the PTT/audio coordination rules are deterministic and testable behind existing seams.

The project already has `PttStateMachine`, `AudioInput`, `AudioOutput`, `VoiceTransport`, jitter buffering, and privacy-safe diagnostics. The missing piece is a small coordinator that proves when capture starts/stops, when frames are sent, and how remote frames reach playback.

## Decision

Add `PttAudioController`, a framework-free coordinator that uses only existing core seams:

- `PttStateMachine` for local/remote PTT state;
- `AudioInput` for local capture;
- `AudioOutput` for jitter-buffered playback;
- `VoiceTransport` for audio-frame send/receive accounting;
- `DiagnosticsSink` for redacted audio pipeline diagnostics.

The controller starts `AudioInput` only when local PTT transitions to `TRANSMITTING`, stops capture on local release, sends captured frames only while transmitting, routes remote frames into `AudioOutput`, and refuses local capture while the state machine is `BUSY`.

## Acceptance criteria

- Tests prove local capture starts only from connected/local-pressed state and stops on release.
- Tests prove captured frames after release are not sent.
- Tests prove remote frames are enqueued through `AudioOutput` and diagnostics do not expose raw peer IDs, device names, or payload bytes.
- Tests prove local press during remote receive becomes `BUSY` and does not capture/send local audio.

## Rollback / pivot trigger

Rollback or redesign the controller if real Android audio adapters require UI/domain code to call `AudioRecord` or `AudioTrack` directly, if busy-state behavior causes accidental capture, or if diagnostics start exporting raw audio payloads or raw peer identifiers.

## Diagnostics signal

The audio pipeline records coarse `AudioPipeline(state=..., reason=..., localFramesSent=..., remoteFramesReceived=...)` events plus existing redacted `AudioFrameReceived(...)` events for remote payload metadata. It never logs raw audio bytes.

## Consequences

Positive:

- Android audio adapters can be thin `AudioInput`/`AudioOutput` implementations instead of owning PTT state rules.
- Busy-state and capture start/stop behavior is covered before microphone APIs are introduced.
- Diagnostics remain metadata-only.

Negative:

- This does not prove real microphone latency, echo behavior, or device-specific playback reliability.
- Physical-device audio tests are still required after the Bluetooth data slice is stable.
