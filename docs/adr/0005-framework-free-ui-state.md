# ADR 0005: Framework-free talk screen state

Date: 2026-05-10.
Status: Accepted.

## Context

The next milestone needs an Android module and Compose surface, but the repository rules require UI state tests before Compose rendering tests. Compose must not call Bluetooth, audio, or permission framework APIs directly.

## Decision

Add a framework-free `TalkScreenPresenter` and immutable `TalkScreenState` in `:core` before adding Compose code.

The presenter accepts already-derived domain/seam inputs:

- `PttState` from the domain state machine;
- `PermissionAction` from the permission policy;
- `BluetoothEnvironmentState` from the Bluetooth seam;
- `ConnectionHealth` from the connection monitor;
- current user action count and the click budget.

The presenter returns only display-safe text, booleans for Push-To-Talk and diagnostics visibility, click-budget status, acceptance criteria, rollback trigger text, and a privacy-safe diagnostics signal.

## Acceptance criteria

- Permission, Bluetooth, disconnected, degraded, connected, transmitting, receiving, scanning, connecting, and idle states can be rendered without Android framework classes.
- Push-To-Talk is enabled only when permissions are allowed, Bluetooth is ready, connection health is stable, and the PTT state is connected/transmitting.
- Blocking states expose diagnostics and a stable diagnostics signal without raw peer identifiers, MAC addresses, device names, or audio payloads.
- Click-budget status is computed from injected counters so future UI tests can assert the connect-to-talk path stays inside the allowed action budget.

## Rollback / pivot trigger

Rollback or pivot the UI-state model if Compose needs to inspect `BluetoothEnvironment`, `AudioInput`, `AudioOutput`, Android permission classes, or transport implementations directly, or if the happy-path connect-to-talk flow requires more than the configured click budget.

## Diagnostics signal

`TalkScreenState.diagnosticsSignal()` must use coarse event names such as `ui_blocked:REQUEST_NEARBY_DEVICES`, `ui_ready:ptt_enabled`, and `ui_reconnect:DISCONNECTED`. It must not include raw peer IDs, MAC addresses, device names, or audio bytes.

## Consequences

Positive:

- Compose can be added as a thin renderer over a tested state model.
- Browser/design-preview tests can validate state snapshots before physical Bluetooth/audio work.
- UI diagnostics stay privacy-safe by construction.

Negative:

- This does not yet create an Android module or actual Compose previews.
- Text strings are simple placeholders and may need localization/resource extraction when Android UI is introduced.
