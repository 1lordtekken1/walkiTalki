# ADR 0014: Android permission/environment adapter seam

Date: 2026-05-12

## Status

Accepted

## Context

MVP roadmap item 4 requires Android Bluetooth permission and environment state to be represented behind existing core seams before real `BluetoothSocket`, `AudioRecord`, or `AudioTrack` code is introduced. The core `PermissionPolicy` already models Android 11 legacy discovery and Android 12+ Nearby Devices behavior, but the Android app needed a small adapter that maps Android manifest permission names into that policy without scattering permission checks through UI or transport code.

## Decision

Add `AndroidPermissionEnvironmentAdapter` in the `:app` layer. It:

- maps Android manifest permission strings to core `AndroidPermission` values through `AndroidPermissionManifestNames`;
- accepts a testable `PermissionChecker` seam so JVM tests can cover grant/deny combinations deterministically;
- exposes `decisionFor(UseCase)`, `bluetoothStateFor(UseCase)`, and `diagnosticsSignalFor(UseCase)`;
- keeps diagnostics coarse (`permission:nearby_devices_missing`, `permission:legacy_location_missing`, `permission:microphone_missing`, `bluetooth:off`, `permission:ready`) and avoids raw device names, MAC addresses, peer identifiers, and manifest permission dumps;
- provides `fromContext(Context, boolean)` as the Android framework boundary while keeping UI/domain code independent from direct permission calls.

## Acceptance criteria

- Android 12+ scan requires Nearby Devices permissions and does not require legacy fine location.
- Android 11 legacy scan requires Bluetooth, Bluetooth admin, and fine location.
- Bluetooth-off state takes priority in environment reporting after permission collection.
- Transmit-audio use case reports the microphone gate separately from Nearby Devices.
- JVM unit tests cover the adapter without requiring physical devices.

## Rollback/pivot trigger

Rollback or redesign this adapter if permission checks begin leaking into Activities, Compose, transport, or core domain code, or if instrumented API-version tests prove that framework grant behavior differs from the modeled policy.

## Diagnostics signal

Only coarse permission/environment states may be exported. Do not log raw MAC addresses, raw peer identifiers, unredacted device names, or raw audio payloads.
