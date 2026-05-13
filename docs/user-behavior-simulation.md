# User behavior simulation

Date: 2026-05-09.
Status: Active Milestone 1 quality gate.

## Purpose

walkiTalki must not wait for Android UI implementation to validate user behavior. The core module now contains framework-free simulations that combine permission policy, PTT state transitions, user-facing messages, and diagnostics redaction.

These simulations are not a replacement for physical-device tests. They are a fast safety net that prevents the product flow from becoming unclear before Bluetooth, audio, or Compose code exists.

## Simulated journeys

The current executable test runner covers these user journeys:

1. **Happy path:** user grants Nearby devices and Microphone, scans, selects a peer, connects, presses Push-To-Talk, releases, and returns to ready state.
2. **Denied Nearby devices:** user tries to scan without required permissions and receives a specific next action instead of a generic failure.
3. **Pairing rejected:** user reaches pairing, rejects it, and the app does not auto-reconnect without explicit user action.
4. **Simultaneous Push-To-Talk:** local user talks, remote peer starts talking, and the state resolves deterministically to `BUSY` with a clear message.
5. **Disconnect with diagnostics:** connection fails after an audio frame; diagnostics remain visible but redact raw peer id, raw device name, and audio payload bytes.

## Quality gates

A user-flow change is not accepted unless it preserves these properties:

- every blocked action has a specific user-facing recovery message;
- automatic reconnect does not occur in the MVP without user intent;
- simultaneous talk states are deterministic;
- diagnostics keep counters/state information while redacting sensitive peer and audio data;
- receive-only flows do not require microphone permission;
- transmit flows require microphone permission.

## Implementation note

`UserJourney` is intentionally framework-free. Android code must later map platform callbacks into these core methods/events instead of duplicating the behavior in Compose or Android framework classes.

## 100-scenario catalog

The simulation gate now includes `SimulationScenarioCatalog.all()`, an executable catalog of 100 stakeholder scenarios across permissions, pairing, transport, audio, UI/UX, privacy, reliability, and release readiness. The catalog includes cases such as:

- remote user muted/forgotten volume;
- one user walking out of range into weak signal/interference;
- simultaneous Push-To-Talk;
- remote audio arriving before connection;
- pairing rejection/timeouts;
- screenshot/diagnostics privacy checks;
- accessibility and click-budget UI/UX checks.

Every catalog entry carries acceptance criteria, rollback/pivot trigger, diagnostics signal, and a simulated `UserJourneyResult`. `CoreTestRunner.simulationCatalogCoversOneHundredStakeholderScenarios()` enforces the count, unique IDs, required metadata, key scenarios, and redaction of raw peer identifiers, device names, and audio payload bytes.

See [Vertical Slice Readiness Roadmap](vertical-slice-readiness-roadmap.md) for stakeholder readiness scores and the full 100-scenario inventory.

Run `./gradlew :core:renderSimulationScenarioReport` to write the executable catalog to `core/build/reports/simulation-scenarios.md` for QA and stakeholder review.
