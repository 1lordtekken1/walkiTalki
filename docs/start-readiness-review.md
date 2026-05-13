# walkiTalki start readiness review

Date: 2026-05-09.
Verdict: **GO for Milestone 0 only**. Do not start Bluetooth/audio production features yet.

## 1. What was rechecked

This is the second attack pass after the TDD-first rules and repository research were added. The goal is to decide whether the project is ready to start implementation, and if yes, what exactly can start without violating the new rules.

Checked inputs:

- repository-wide TDD and architecture rules in `AGENTS.md`;
- TDD development rules and gates in `docs/development-rules.md`;
- first critical plan review in `docs/critical-plan-review.md`;
- existing repository analysis in `docs/existing-repositories-analysis.md`;
- current Android official documentation for Bluetooth permissions, RFCOMM connections, Nearby Connections, and audio latency.

Relevant current Android facts verified during this pass:

- Android Bluetooth RFCOMM still requires a server/client socket model; if devices are not paired, Android can block the connection flow while it shows a pairing request.
- Current Bluetooth discovery documentation says RFCOMM connections require paired devices; this confirms that first-run UX must handle pairing explicitly.
- Android 12+ Bluetooth permissions remain split into nearby-device permissions, while legacy Bluetooth permissions need older-SDK handling.
- Nearby Connections remains a valid pivot candidate but the public codelab is deprecated, so codelab code must not be copied as a current implementation template.
- Android audio latency remains device/build dependent and must be measured; official guidance still recommends matching device sample rate/buffer size and preparing for warmup latency.

Sources:

- Android Bluetooth permissions: https://developer.android.com/guide/topics/connectivity/bluetooth/permissions
- Android Bluetooth connections/RFCOMM: https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices
- Android Bluetooth discovery: https://developer.android.com/develop/connectivity/bluetooth/find-bluetooth-devices
- Android audio latency: https://developer.android.com/ndk/guides/audio/audio-latency
- Nearby Connections codelab: https://developer.android.com/codelabs/nearby-connections

## 2. Final attack board

| Attack area | Failure scenario | Current protection | Pass? | Required action before feature work |
| --- | --- | --- | --- | --- |
| TDD discipline | Developers jump straight to Bluetooth/audio implementation and create untestable code | `AGENTS.md` and development rules require tests first and framework seams | Pass | Start with Gradle/JVM test bootstrap |
| Product scope | MVP promise becomes “real walkie-talkie for groups/emergencies” | Non-goals exclude groups, mesh, background, emergency positioning | Pass | Keep README/product copy narrow |
| Transport choice | Bluetooth Classic is assumed correct before measurement | Plan labels RFCOMM as hypothesis and defines pivot triggers | Pass | Implement data-only slice before audio |
| Pairing UX | User gets stuck on Android pairing dialog or hidden system flow | Second pass confirms pairing must be explicit in UX/state machine | Conditional pass | Add pairing/onboarding states before discovery UI polish |
| BLE temptation | BLE mesh examples push project toward BLE voice | Rules forbid BLE as primary realtime audio without benchmark | Pass | Keep BLE to future discovery/control only |
| Nearby shortcut | Deprecated codelab code is copied into production | Research rules forbid copying deprecated codelab code | Pass | Use current API docs only if pivoting |
| Audio latency | Prototype works on one phone but feels unusable elsewhere | Milestone gates require physical-device measurement | Pass | Delay audio until protocol/data slice passes |
| Codec complexity | Opus is added before proving transport stability | Plan makes PCM diagnostic first and Opus later benchmark | Pass | No codec work before Milestone 4 |
| Permissions | Android version-specific permission behavior breaks onboarding | Permission policy tests are required before UI/platform code | Pass | Write pure permission policy tests first |
| Privacy | Logs leak peer names, identifiers, or audio payloads | Diagnostics redaction tests are required before Bluetooth | Pass | Implement diagnostics model before transport |
| Licensing | Existing code is copied from GPL/AGPL/unknown sources | Research rules require license review before code reuse | Pass | Borrow patterns only, no code copy |
| QA realism | Emulator tests are treated as sufficient | Gates require physical-device smoke/matrix tests | Pass | Prepare manual matrix checklist when hardware is available |
| Battery | Scanning/reconnect loops drain devices | Rules require bounded discovery and reconnect consent | Pass | Add timers/backoff tests before platform scans |
| Background mode | Background mic/service work expands privacy and policy surface | Background mode is explicitly out of MVP | Pass | Keep first app foreground-only |

## 3. Devil's advocate re-run by stakeholder

### 3.1 Product

**Attack:** The team may interpret “can start” as permission to build the whole product.

**Answer:** Start is approved only for Milestone 0: project skeleton, CI/test bootstrap, ADRs, and first failing tests. Feature implementation starts only after gates pass.

**Decision:** Pass with strict milestone boundary.

### 3.2 Engineering

**Attack:** Documentation is extensive, but no executable tests exist yet. The project could still become documentation-heavy and code-poor.

**Answer:** The next commit must create the Gradle skeleton and a passing/failing test baseline. No further planning-only work should block Milestone 0 unless it resolves an implementation blocker.

**Decision:** Pass for Milestone 0; fail for Bluetooth/audio feature start.

### 3.3 QA

**Attack:** Physical-device matrix is not available in the repository.

**Answer:** Device matrix is not required to bootstrap pure JVM tests, protocol tests, state machine tests, and diagnostics tests. It becomes mandatory before accepting Bluetooth/audio slices.

**Decision:** Pass for Milestone 0 and Milestone 1; conditional for Milestone 2+.

### 3.4 Security and privacy

**Attack:** A voice app can accidentally collect sensitive data before privacy rules are enforced.

**Answer:** Diagnostics redaction and no-recording-by-default are now preconditions. The next implementation order puts diagnostics and permission policy tests before Bluetooth/audio behavior.

**Decision:** Pass if the first implementation PR includes redaction tests before any logging of peer/audio state.

### 3.5 UX

**Attack:** Bluetooth pairing is a system-driven flow and can break the simple walkie-talkie promise.

**Answer:** Pairing must be represented as a first-class onboarding/state-machine condition, not hidden behind a generic “connecting” spinner.

**Decision:** Conditional pass; the first state-machine tests must include pairing required, pairing rejected, and pairing timeout outcomes.

### 3.6 Legal/compliance

**Attack:** Researching open-source apps can lead to accidental license contamination.

**Answer:** Existing repository rules explicitly prohibit copying code without license review, and the research document marks GPL/AGPL/proprietary risks.

**Decision:** Pass.

### 3.7 Support/operations

**Attack:** Bluetooth issues will be impossible to debug if diagnostics are delayed.

**Answer:** Diagnostics model and redaction tests are placed before Bluetooth implementation.

**Decision:** Pass if diagnostics are part of Milestone 1, not postponed.

## 4. Go / no-go gates

### GO now

The project can start:

1. Android Gradle skeleton.
2. Unit test infrastructure.
3. CI/lint baseline.
4. ADR directory and first ADRs.
5. Pure JVM tests for protocol, permission policy, diagnostics redaction, and PTT/domain state machine.

### NO-GO now

The project must not yet start:

1. Real `BluetoothSocket` implementation.
2. Real `AudioRecord` / `AudioTrack` implementation.
3. Opus integration.
4. Nearby Connections integration.
5. Background services.
6. Group/mesh mode.
7. Any code copied from researched repositories.

## 5. Required first implementation sequence

The next development work should be executed in this exact order:

1. Create Android Gradle project skeleton.
2. Add a deliberately tiny test that proves `./gradlew test` works.
3. Add CI command documentation or workflow placeholder.
4. Add `docs/adr/0001-tdd-and-scope.md` recording this GO decision.
5. Add failing protocol tests for frame encoding/decoding.
6. Implement the minimum protocol code to pass those tests.
7. Add failing diagnostics redaction tests.
8. Implement the minimum diagnostics model.
9. Add permission policy tests before any Android permission UI.
10. Add domain state-machine tests before Compose screens.

## 6. First issues/tasks to create

### Task 1: Bootstrap build and test harness

Acceptance criteria:

- `./gradlew test` exists and passes;
- at least one JVM test exists;
- package namespace is established;
- no Bluetooth/audio production code is added.

### Task 2: Add protocol failing tests

Acceptance criteria:

- tests cover valid frame, partial read, concatenated frames, invalid magic, invalid version, max payload, unknown type;
- tests fail before implementation;
- no Android framework dependency in protocol package.

### Task 3: Add diagnostics redaction tests

Acceptance criteria:

- audio payload is never exported;
- peer identifiers are redacted or hashed;
- raw device names are not exported by default;
- counters and state transitions remain visible.

### Task 4: Add permissions policy tests

Acceptance criteria:

- Android 12+ nearby-device permission behavior is modeled;
- Android <= 11 legacy/location behavior is modeled;
- microphone permission is scoped to transmit;
- partial grants return specific next actions.

### Task 5: Add PTT state-machine tests

Acceptance criteria:

- idle/scanning/connecting/connected states are covered;
- local transmit and remote receive are covered;
- simultaneous PTT is deterministic;
- pairing rejected and pairing timeout are explicit states/errors;
- manual reconnect is user-initiated.

## 7. Final decision

The plan passes the second attack only under a narrow interpretation: **start Milestone 0 now, with TDD infrastructure and pure tests first**.

The plan does **not** pass as permission to start Bluetooth/audio production code immediately. Real Bluetooth and audio work become allowed only after the build/test harness, ADRs, protocol tests, diagnostics redaction tests, permission policy tests, and state-machine tests are in place.

Recommended next commit: **bootstrap Android project and first JVM test**.
