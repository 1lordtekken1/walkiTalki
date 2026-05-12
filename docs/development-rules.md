# walkiTalki development rules: TDD-first baseline

Date: 2026-05-09.

## 1. Non-negotiable engineering mode

walkiTalki must be developed with a strict TDD-first workflow. The first implementation milestone is not “make Bluetooth audio work”; it is “make the project safe to change.” That means tests, fakes, diagnostics, and acceptance gates come before production complexity.

The default loop for every feature is:

1. **Red:** write a failing test or executable check that captures the behavior.
2. **Green:** add the smallest implementation that makes the test pass.
3. **Refactor:** improve names, structure, duplication, and seams without changing behavior.
4. **Record:** update the PR with the test evidence and any architecture decision affected.

Documentation-only and build-bootstrap tasks may skip Red, but must explicitly say why.

## 2. Test pyramid for this app

| Layer | Purpose | Default tool | Examples |
| --- | --- | --- | --- |
| Pure JVM unit tests | Fast behavior checks | JUnit/Kotlin test stack | frame encoder/decoder, jitter buffer, sequence handling, PTT state machine |
| Coroutine tests | Deterministic async behavior | `runTest`, injected dispatchers/scopes | reconnect timeout, heartbeat scheduler, scan timeout, audio frame pump |
| Robolectric or AndroidX tests | Android-adjacent behavior without devices where reliable | AndroidX test/Robolectric if added | permission UI policy, ViewModel lifecycle |
| Instrumented device tests | Real platform behavior | AndroidJUnitRunner | Bluetooth permissions, real sockets, AudioRecord/AudioTrack smoke checks |
| Manual physical matrix | Hardware truth | test checklist + diagnostics export | Pixel/Samsung/Xiaomi/OnePlus connection and latency checks |

## 3. First tests to write before app code

### 3.1 Protocol tests

Create tests before implementing the protocol package:

- encodes and decodes a valid `PING` frame;
- decodes two concatenated frames from one stream;
- decodes one frame split across multiple reads;
- rejects invalid magic/version;
- rejects payload length above maximum;
- preserves sequence number and monotonic sender timestamp;
- treats unknown frame type as recoverable protocol error, not crash.

### 3.2 State machine tests

Before implementing UI or Bluetooth classes, define a pure domain state machine and test:

- idle -> scanning -> peer found -> connecting -> connected;
- connected -> transmitting while local PTT is held;
- connected -> receiving while remote PTT is active;
- simultaneous PTT resolves to deterministic “busy” or priority rule;
- permission denied blocks scanning and recording with user-action error;
- socket failure transitions to disconnected with diagnostics event;
- manual reconnect is allowed only after explicit user action in MVP.

### 3.3 Audio-buffer tests

Before touching `AudioRecord` or `AudioTrack`, test pure buffer logic:

- accepts in-order frames;
- drops duplicate frames;
- drops frames older than playout window;
- handles missing sequence numbers without blocking forever;
- caps memory growth;
- reports underrun/overrun counters.

### 3.4 Permissions policy tests

Before writing Android permission UI, model a pure permissions policy:

- Android 12+ requires Nearby Devices permissions for scan/advertise/connect;
- Android <= 11 discovery may require location depending on scan mode;
- microphone permission is required only for transmit, not for receive-only diagnostics;
- partial grants produce explicit next action, not a generic failure.

### 3.5 Diagnostics tests

Before first Bluetooth integration, test diagnostics redaction:

- audio payload is never included;
- peer identifiers are hashed or redacted;
- device names are redacted by default;
- exported diagnostics include app version, Android version, device model class, permission state, transport state transitions, counters, and error codes.

## 4. Required implementation seams

Production Android classes must be wrapped behind interfaces so unit tests can use fakes:

- `BluetoothEnvironment` for adapter state, permissions visibility, discovery, and socket creation;
- `VoiceTransport` for peer discovery, connection, frame send/receive, and disconnect;
- `AudioInput` and `AudioOutput` for microphone/playback;
- `Clock` for monotonic time;
- `DispatcherProvider` or injected coroutine scopes for deterministic coroutine tests;
- `DiagnosticsSink` for structured events.

Direct calls from ViewModels or Compose code into `BluetoothAdapter`, `BluetoothSocket`, `AudioRecord`, or `AudioTrack` are forbidden.

## 5. Acceptance gates by milestone

### Milestone 0: project foundation

Required before feature code:

- Gradle project builds;
- `./gradlew test` exists and passes;
- CI runs unit tests and lint;
- ADR directory exists;
- TDD rules and local skills are present.

### Milestone 1: protocol and state machine

Required before real Bluetooth:

- protocol unit tests pass;
- state machine tests pass;
- diagnostics redaction tests pass;
- no Android framework dependency in protocol/domain packages.

### Milestone 2: Bluetooth data slice

Required before audio:

- fake transport tests pass;
- instrumented or manual two-device `PING/PONG` test succeeds;
- failed connect produces structured error;
- discovery/connect timeout is covered by test.

### Milestone 3: foreground PTT PCM slice

Required before codec work:

- audio buffer tests pass;
- one-way PTT smoke test works on two physical devices;
- microphone stops within the accepted post-release drain window;
- diagnostics include frame counters and underrun/overrun counters.

### Milestone 4: codec and transport decision

Required before Opus or transport pivot:

- PCM bottleneck is measured;
- Opus benchmark has a test plan;
- Bluetooth Classic success rate and latency are measured on the physical matrix;
- pivot decision is documented in an ADR.

## 6. Definition of done for every PR

A PR is not done unless it includes:

- tests added or updated first, or a documented reason tests are not applicable;
- exact commands run;
- diagnostics/observability impact;
- privacy impact for any permission, audio, peer, or logging change;
- updated ADR if architecture changed;
- no new direct Android framework calls from UI/domain layers.

## 7. Current TDD backlog

1. Bootstrap Android Gradle project with a passing empty unit test.
2. Add protocol package tests for frame encoding/decoding.
3. Add pure PTT/domain state machine tests.
4. Add diagnostics redaction tests.
5. Add permission policy tests.
6. Only then implement the first production packages.
