# QA plan: browser, connection stability, and click optimization

Date: 2026-05-09.
Status: Required planning block for Android UI milestone.

## 1. Why this exists

The current codebase is still framework-free core logic, so browser testing cannot run against a real web or Android UI yet. This document turns the requested browser testing, connection testing, connection stability testing, click optimization, and multi-mode behavior into explicit gates for the upcoming UI/platform milestones.

## 2. Browser testing block

When a browser-runnable surface exists, such as a web prototype, generated design preview, Storybook-like component catalog, or Compose screenshot export page, it must include automated checks for:

- onboarding and permission explanation screen;
- discovery screen states: Bluetooth off, permission missing, scanning, peer found, pairing required, pairing rejected, pairing timeout, connection failed;
- Talk screen states: ready, transmitting, receiving, busy, disconnected;
- diagnostics screen with redacted peer information;
- responsive layout at narrow phone width, large phone width, and tablet-ish width;
- keyboard-only navigation for buttons and diagnostics copy action;
- visible focus state and accessible labels for every clickable control.

Browser tests must fail if a primary journey needs unnecessary extra clicks compared to the current user-action budgets.

## 3. Connection and stability testing block

Before real Bluetooth audio is accepted, test in this order:

1. **Pure JVM:** `ConnectionHealthMonitor` tests for stable, degraded, and disconnected health.
2. **Fake transport:** deterministic heartbeat/pong tests with injected time.
3. **Two-device data slice:** real devices exchange `PING/PONG` frames for at least 2 minutes.
4. **Stability soak:** connected idle test for 15 minutes with heartbeat counters and no reconnect loop.
5. **PTT smoke:** foreground one-way PTT for at least 2 minutes with frame counters, drops, and underruns.
6. **Device matrix:** at least Pixel + Samsung + one OEM-skinned Android device before claiming Bluetooth stability.

Metrics to collect:

- time to first connection;
- heartbeat interval and missed heartbeat windows;
- disconnect reason;
- frames sent/received;
- dropped frames;
- jitter buffer underruns;
- user-visible recovery action.

## 4. Click optimization block

Every clickable flow must be represented in either `UserJourney` or UI tests.

Initial budgets:

| Flow | Budget |
| --- | --- |
| First successful nearby connection after permissions | <= 5 user actions after permissions are granted |
| Repeat reconnect after disconnect | <= 2 user actions |
| Copy diagnostics after failure | <= 2 user actions |
| Start talking from connected state | 1 press action |
| Stop talking | 1 release action |

A budget increase requires a product/UX note explaining why the extra action improves safety, privacy, or clarity.

## 5. Modes block

The core now defines these operation modes:

- `PERFORMANCE`: shorter scan/heartbeat timing and larger jitter capacity for active testing and low-latency tuning;
- `BALANCED`: default MVP behavior;
- `POWER_SAVER`: longer heartbeat and smaller buffer profile for lower activity;
- `DIAGNOSTIC`: short heartbeat and verbose diagnostics for connection/stability testing.

MVP constraint: no mode may enable automatic reconnect without explicit user action.

## 6. Next implementation hooks

- Android UI must read operation mode policy instead of hardcoding scan/heartbeat/jitter values.
- Bluetooth data-slice tests must emit connection health events compatible with `ConnectionHealthMonitor`.
- Browser/design-preview tests must assert the click budgets above.


## Preview automation audit

Run `./gradlew :preview:renderTalkScreenPreviewAudit --no-daemon` to produce `preview/build/reports/talk-screen-preview-audit.md`. The audit checks required MVP states, stable automation hooks, click-budget metadata, and privacy-safe diagnostics before a browser/Android visual-baseline runner is available.
