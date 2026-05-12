# ADR 0002: Pure permission policy and PTT state machine before Android UI

Date: 2026-05-09.
Status: Accepted.

## Context

The readiness review requires permission policy tests and domain state-machine tests before Android permission UI, Compose screens, or Bluetooth/audio platform code. The key risks are Android-version-specific permissions, confusing partial grants, hidden microphone use, pairing failures, simultaneous Push-To-Talk, and automatic reconnect without user consent.

## Decision

Add framework-free `core` implementations for:

- `PermissionPolicy`: computes required permissions and the next user action for scan, connect, receive, and transmit use cases;
- `PttStateMachine`: models scan, pairing, connecting, connected, transmitting, receiving, busy, disconnected, and manual reconnect states.

The policy intentionally distinguishes:

- Android 12+ nearby-device permission flow;
- Android 11 and lower legacy Bluetooth + location discovery flow;
- receive-only behavior that does not require microphone permission;
- transmit behavior that requires microphone permission.

The state machine intentionally makes pairing and reconnect explicit:

- pairing required, rejected, and timed out are separate states;
- simultaneous Push-To-Talk resolves to deterministic `BUSY`;
- automatic reconnect ticks do not leave `DISCONNECTED` in the MVP;
- user-requested reconnect is the only reconnect path.

## Consequences

Positive:

- Android UI can be driven by pure decisions instead of scattering SDK checks through screens;
- pairing and reconnect UX can be tested before Bluetooth implementation;
- privacy expectations are preserved because microphone permission is scoped to transmit.

Negative:

- platform-specific permission edge cases still require instrumented tests later;
- the state machine is intentionally conservative and may need expansion when real transport events appear;
- Android copy and visual treatment still need Compose implementation after the domain states stabilize.

## Follow-up decisions

- ADR 0003 should define the protocol framing details and stream reader behavior.
- ADR 0004 should define diagnostics event taxonomy beyond the current redaction baseline.
- Android implementation must map real permission APIs and Bluetooth pairing callbacks into these pure policy/state-machine events.
