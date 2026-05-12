# Vertical slice readiness and stakeholder roadmap
Date: 2026-05-11.
Status: framework-free vertical-slice plan plus executable simulation catalog; not yet an Android MVP.
## Executive summary
The repository currently proves the **core vertical slice** on the JVM: permission policy, PTT state, protocol framing, stream transport seam, fake Bluetooth/audio seams, privacy-safe diagnostics, session coordination, and preview UI states. The actual phone-to-phone Android application is **not 100/100** because there is no Android app module, no real Bluetooth adapter, no real AudioRecord/AudioTrack adapter, no physical two-device validation, and no store/release hardening.
## Module readiness scorecard (0-100)
| Module / slice | Score | Why it is not 100 | Path to 100 |
|---|---:|---|---|
| Protocol framing + stream reader | 78 | Pure JVM codec/stream tests exist, but no real socket soak or fuzz/property suite yet. | Add fuzz/property tests, real Bluetooth stream soak, backwards-compatible version tests. |
| PTT domain state machine | 76 | Core transitions are deterministic, but physical interruption cases are only simulated. | Add Android lifecycle/device interruption adapters and two-device race tests. |
| Permission policy | 72 | Android-version policy is modeled, but no instrumented permission UI/grant verification. | Add Android permission adapter and instrumented tests for API 30/31/33/35 behavior. |
| Diagnostics/privacy redaction | 82 | Redaction is executable, but support export format and privacy review are not finalized. | Version diagnostics schema, add support runbook, run privacy threat review. |
| Audio seams + jitter baseline | 58 | Fake audio and jitter tests exist; no real capture/playback, codec, route, or latency benchmark. | Add AudioRecord/AudioTrack adapters, underrun counters, latency tests, and optional codec benchmark. |
| Transport seams + fake/stream transport | 55 | Frame transport is testable, but no BluetoothSocket wrapper or physical stability matrix. | Wrap BluetoothSocket streams, run two-device ping/pong soak, record disconnect/reconnect gates. |
| Session controller | 57 | Composes core seams, but Android ViewModel/lifecycle integration is missing. | Add app/ViewModel adapter, lifecycle tests, and reconnect/permission UI integration tests. |
| Preview/browser QA surface | 64 | Static HTML covers states and automation hooks; no browser screenshot runner available here. | Add browser/Playwright or Android screenshot tests and visual diff baselines. |
| Android app module | 0 | No Android module exists in this environment. | Create app module, manifest, DI seams, and Compose shell once Android SDK is available. |
| Bluetooth Classic adapter | 5 | Only design seams and stream transport exist. | Implement adapter behind VoiceTransport; validate on physical devices. |
| Release/store readiness | 18 | Docs and gates exist, but no signed build, privacy review, or device matrix. | Add release checklist, crash/privacy docs, signing pipeline, and beta smoke testing. |
## Stakeholder readiness (0-100)
| Stakeholder | Score | What they have today | What closes to 100 |
|---|---:|---|---|
| End users | 22 | Clickable behavior is simulated and previewed; no installable Android app or real audio. | Installable two-phone app with understandable permission/pairing/connect/talk/disconnect flows. |
| Product | 38 | MVP scope, no-go list, click budget, rollback triggers, and 100-scenario catalog. | Validated MVP acceptance on real devices and explicit deferral of group/background/mesh scope. |
| UX/accessibility | 46 | Framework-free presenter and preview states with automation hooks. | Compose UI, large-font/screen-reader/device-orientation checks, and screenshot baselines. |
| Android engineering | 42 | Core seams are clean and framework-free. | Android app module, adapters, lifecycle handling, foreground-service decision, and instrumented tests. |
| Bluetooth/transport engineering | 34 | Protocol, stream reader, heartbeat, fake transport. | BluetoothSocket wrapper plus sustained two-device ping/pong stability gate. |
| Audio engineering | 32 | Jitter buffer, fake input/output, PTT audio controller. | AudioRecord/AudioTrack adapters, route changes, underrun metrics, latency and codec benchmarks. |
| QA | 52 | Executable JVM harness, architecture guard, preview checks, 100 simulation scenarios. | Browser screenshot automation, Android instrumented suite, and physical-device matrix. |
| Privacy/security | 70 | Redaction tests, no raw payload/peer identifiers in diagnostics. | Threat model, support export schema, store/privacy review, and negative tests on Android logs. |
| Support/operations | 44 | Diagnostics signals and user-facing messages are modeled. | Copy/export support bundle, runbook mapping signals to fixes, and beta feedback loop. |
| Release/business | 24 | Roadmap and quality gates exist. | Beta distribution, signed builds, release checklist, analytics-free support policy, and rollback plan. |

## Executable readiness scorecard

The readiness tables above are mirrored in `ReadinessScorecard.modules()` and `ReadinessScorecard.stakeholders()` so the JVM harness can enforce score counts, 0-100 bounds, honest MVP status, and a non-empty path to 100 for every module and stakeholder. `ReadinessReportRenderer.renderMarkdown()` exports the same executable scorecard as Markdown, including the current top blockers, so docs can later be generated from code instead of drifting. Run `./gradlew :core:renderReadinessReport` to write `core/build/reports/readiness-scorecard.md`.


## Executable MVP GO/NO-GO gate

`MvpGateEvaluator.evaluate()` currently returns `NO_GO`: the framework-free core and reports are useful planning gates, but MVP cannot be called ready until Android app, transport adapter, audio adapter, two-device ping/pong, two-device PTT, weak-signal/disconnect matrix, privacy export, and release gates pass. Run `./gradlew :core:renderMvpGateReport` to write `core/build/reports/mvp-gate.md` for CI and release review.

## Roadmap to 100
### 1. Lock framework-free core gates
Keep `./gradlew test` green; expand simulation catalog whenever a new risk is found; no Android framework imports in core/preview.
### 2. Add Android app shell
Create Android module, manifest, Compose shell, ViewModel/application adapter, and DI seams without moving business rules into UI.
### 3. Implement Android permission/environment adapters
Map real permission/Bluetooth state into `PermissionPolicy`/`BluetoothEnvironment`; add API-version instrumented tests.
### 4. Build Bluetooth data vertical slice
Wrap `BluetoothSocket` streams with existing `StreamVoiceTransport`; run real two-device PING/PONG before audio.
### 5. Harden transport stability
Run weak-signal, walk-away, Bluetooth-toggle, stale-pong, and reconnect matrices; tune heartbeat policy and diagnostics.
### 6. Add audio adapters
Implement `AudioRecord`/`AudioTrack` behind `AudioInput`/`AudioOutput`; test route changes, mute/volume UX, underruns, and no raw payload logs.
### 7. Compose UI + UX validation
Render all presenter states, connect UI intents to session controller, add accessibility/screenshot tests and copy review.
### 8. End-to-end PTT smoke test
Two physical devices complete scan/connect/hold-to-talk/release/disconnect within click budget, with support export available.
### 9. Privacy/support/release hardening
Finalize diagnostics schema, support runbook, privacy review, beta checklist, rollback triggers, and no-go scope enforcement.
### 10. 100/100 closure review
All stakeholder gates pass with exact evidence: commands, screenshots, device matrix, diagnostics samples, and signed build artifacts.
## Executable 100-scenario simulation catalog
The source of truth is `SimulationScenarioCatalog.all()`, verified by `CoreTestRunner.simulationCatalogCoversOneHundredStakeholderScenarios()`. Each scenario has stakeholder, category, situation, expected UX, acceptance criteria, rollback trigger, diagnostics signal, and a simulated `UserJourneyResult`.
| # | ID | Stakeholder | Category | Situation | Expected UX |
|---:|---|---|---|---|---|
| 1 | `permissions-nearby-denied-first-run` | user | permissions | First run scan without Nearby devices permission. | Show a specific Nearby devices grant action and keep the user idle. |
| 2 | `permissions-microphone-denied-listen-only` | user | permissions | User can receive but has not granted microphone permission. | Allow listening, block transmit, and explain that microphone is needed only to talk. |
| 3 | `permissions-legacy-location-missing` | support | permissions | Android 11 discovery starts without legacy Location permission. | Request Location for Bluetooth discovery without blaming Bluetooth. |
| 4 | `permissions-partial-bluetooth-connect` | qa | permissions | Nearby scan permission exists but connect permission is missing. | Keep connection blocked and preserve a clear next permission action. |
| 5 | `permissions-revoked-mid-session` | security | permissions | Permission is revoked while the user expects to talk. | Stop transmit path and keep diagnostics free of identifiers. |
| 6 | `permissions-user-denies-twice` | product | permissions | User denies required permission twice. | Repeat the exact blocked reason and avoid dead-end UI copy. |
| 7 | `permissions-background-return` | user | permissions | User returns from settings after granting permission. | Resume at scan/ready state without hidden retries. |
| 8 | `permissions-mic-granted-nearby-denied` | qa | permissions | Microphone exists but Nearby devices is denied. | Do not imply microphone can bypass peer discovery. |
| 9 | `permissions-nearby-granted-mic-denied-ptt` | qa | permissions | Nearby devices is granted but PTT is pressed without microphone. | Block only transmit and keep listen path understandable. |
| 10 | `permissions-enterprise-policy-block` | support | permissions | Enterprise policy blocks a runtime permission. | Expose coarse permission-block diagnostics and actionable copy. |
| 11 | `pairing-peer-not-paired` | user | pairing | Selected peer is not paired yet. | Show pairing required and ask both users to confirm. |
| 12 | `pairing-user-rejects` | user | pairing | User rejects Android pairing confirmation. | Stay rejected until explicit retry; no auto-reconnect loop. |
| 13 | `pairing-peer-rejects` | support | pairing | Remote peer rejects pairing. | Show retry guidance without exposing remote device name. |
| 14 | `pairing-timeout-pocket` | qa | pairing | Pairing prompt times out while phone is pocketed. | Keep a clear timeout/retry state. |
| 15 | `pairing-wrong-peer-selected` | privacy | pairing | User taps the wrong nearby device. | Avoid raw peer names in diagnostics and allow scanning again. |
| 16 | `pairing-two-peers-same-name` | qa | pairing | Two nearby devices have similar display names. | Use redacted correlation in diagnostics, not raw names. |
| 17 | `pairing-after-bluetooth-toggle` | support | pairing | Bluetooth toggles during pairing. | Move to disconnected/blocking state with reconnect guidance. |
| 18 | `pairing-retry-after-rejection` | product | pairing | User retries after a rejection. | Retry requires an explicit action and stays within click budget. |
| 19 | `pairing-remote-app-not-open` | user | pairing | Remote phone is paired but app is not ready. | Keep connecting/scan guidance visible. |
| 20 | `pairing-stale-bond` | qa | pairing | A stale OS bond exists but socket setup fails. | Disconnect cleanly and keep diagnostics supportable. |
| 21 | `transport-weak-signal-walk-away` | user | transport | One user walks away until signal is weak and frames are missed. | Show unstable/disconnected state instead of pretending audio is live. |
| 22 | `transport-elevator-interference` | qa | transport | Users enter an elevator and heartbeat windows are missed. | Degrade then disconnect based on heartbeat policy. |
| 23 | `transport-pocket-body-block` | support | transport | Phone is in a pocket and body blocks the radio path. | Expose coarse health counters without peer identifiers. |
| 24 | `transport-peer-turns-bluetooth-off` | user | transport | Peer turns Bluetooth off during a session. | Show disconnected and wait for explicit reconnect. |
| 25 | `transport-local-turns-bluetooth-off` | user | transport | Local user turns Bluetooth off while connected. | Block session and route user to Bluetooth settings. |
| 26 | `transport-reconnect-button-after-loss` | product | transport | User taps reconnect after link loss. | Start scanning only after explicit user intent. |
| 27 | `transport-no-auto-reconnect-loop` | engineering | transport | Auto reconnect tick fires after socket failure. | Do not create an unbounded reconnect loop. |
| 28 | `transport-stale-pong` | qa | transport | Pong arrives too late to count as stable. | Keep health degraded/disconnected by policy. |
| 29 | `transport-corrupt-frame` | engineering | transport | Corrupt protocol frame arrives from stream. | Map to recoverable protocol error and avoid payload logging. |
| 30 | `transport-oversized-frame` | security | transport | Oversized payload header is received. | Reject before allocating payload memory. |
| 31 | `transport-split-frame-slow-read` | engineering | transport | Frame bytes arrive split across reads. | Decode only after a complete frame is available. |
| 32 | `transport-concatenated-frames` | engineering | transport | Multiple frames arrive in one read. | Decode frames sequentially without losing boundaries. |
| 33 | `transport-peer-disappears-during-scan` | qa | transport | Peer disappears before scan window ends. | Stop bounded scan and report no peer discovered. |
| 34 | `transport-scan-window-expires` | product | transport | No devices are found before scan timeout. | Show retry guidance and diagnostics elapsed time. |
| 35 | `transport-multiple-nearby-peers` | privacy | transport | Several nearby peers are present. | Do not log raw MAC addresses or names. |
| 36 | `audio-remote-muted-forgotten` | user | audio | Remote user muted phone volume and forgot. | Local can transmit; remote-side UI/diagnostics should make listen/output state obvious later. |
| 37 | `audio-local-muted-forgotten` | user | audio | Local phone output is muted when peer talks. | Receiving state remains visible and should guide the user to volume/output checks. |
| 38 | `audio-mic-blocked-by-os` | support | audio | OS microphone permission is blocked. | PTT asks for microphone and keeps listening available. |
| 39 | `audio-press-before-connected` | qa | audio | User holds PTT before connection is ready. | Ignore transmit and keep state explanatory. |
| 40 | `audio-release-with-no-frame` | qa | audio | User taps PTT quickly and releases before a frame exists. | Return to connected with zero-frame diagnostics. |
| 41 | `audio-remote-frame-before-connection` | security | audio | Remote audio arrives before session connection. | Ignore frame, do not enqueue playback, and avoid received-audio diagnostics. |
| 42 | `audio-payload-redaction` | privacy | audio | Audio payload exists in diagnostics path. | Export payload length and sequence only, never raw bytes. |
| 43 | `audio-jitter-out-of-order` | engineering | audio | Remote frames arrive out of order. | Jitter buffer plays frames in sequence. |
| 44 | `audio-jitter-duplicate` | engineering | audio | Duplicate remote frame arrives. | Drop duplicate and preserve buffer bounds. |
| 45 | `audio-jitter-underrun` | qa | audio | Playback asks for audio when buffer is empty. | Count underrun without crashing. |
| 46 | `audio-busy-no-capture` | privacy | audio | Local user presses PTT while remote is talking. | Enter busy and do not capture microphone audio. |
| 47 | `audio-simultaneous-start` | qa | audio | Both users begin PTT almost simultaneously. | Resolve deterministically to busy. |
| 48 | `audio-long-hold` | product | audio | User holds PTT for a long message. | Continue sending bounded frames and show talking state. |
| 49 | `audio-output-device-change` | user | audio | Output changes from speaker to Bluetooth headset. | Keep receiving state clear and avoid raw device logging. |
| 50 | `audio-noise-burst` | qa | audio | Environment noise creates a burst of audio frames. | Do not log payload; keep byte counts only. |
| 51 | `audio-remote-stops-mid-frame` | engineering | audio | Remote stops while a frame is partially received. | Map stream error and show disconnect or recoverable state. |
| 52 | `audio-local-backgrounded` | product | audio | App is backgrounded while user expects audio. | MVP blocks unsupported background mode and documents no-go state. |
| 53 | `audio-screen-locked` | user | audio | Screen locks during receive. | Future foreground-service gate must preserve listen diagnostics. |
| 54 | `ui-first-run-empty` | user | ui_ux | User opens app for the first time with no permissions. | Primary action explains scan/permission path. |
| 55 | `ui-permission-copy-specific` | ux | ui_ux | Permission is blocked. | Copy names the exact permission and reason. |
| 56 | `ui-ready-state` | ux | ui_ux | Connected and stable. | Show Ready. Hold to talk. and enabled PTT. |
| 57 | `ui-talking-state` | ux | ui_ux | Local user is transmitting. | Show Talking… and release affordance. |
| 58 | `ui-receiving-state` | ux | ui_ux | Peer is talking. | Disable PTT and show listen state. |
| 59 | `ui-busy-state` | ux | ui_ux | Both sides talk at once. | Show peer talking/busy guidance. |
| 60 | `ui-disconnected-state` | ux | ui_ux | Socket fails. | Show Reconnect and diagnostics visibility. |
| 61 | `ui-over-click-budget` | product | ui_ux | Happy path exceeds action budget. | Expose rollback trigger for QA. |
| 62 | `ui-bluetooth-off` | user | ui_ux | Bluetooth is off. | Offer Bluetooth settings action. |
| 63 | `ui-connecting-spinner` | ux | ui_ux | Connection is in progress. | Disable PTT and show wait state. |
| 64 | `ui-scan-timeout` | ux | ui_ux | Scan times out. | Show retry without hiding the reason. |
| 65 | `ui-pairing-rejected` | support | ui_ux | Pairing was rejected. | Show explicit retry guidance. |
| 66 | `ui-diagnostics-copy` | support | ui_ux | Support asks user to copy diagnostics. | Expose coarse diagnostics without identifiers. |
| 67 | `ui-large-font` | accessibility | ui_ux | User has large font enabled. | State labels remain clear and not solely color-coded. |
| 68 | `ui-color-blind` | accessibility | ui_ux | User cannot rely on color. | Text/state labels carry meaning. |
| 69 | `ui-talk-button-disabled` | accessibility | ui_ux | PTT is disabled. | Button disabled state has clear status copy. |
| 70 | `ui-screen-reader-label` | accessibility | ui_ux | Screen reader reads primary action. | Button aria/accessibility label matches action. |
| 71 | `ui-orientation-change` | qa | ui_ux | Device orientation changes. | State survives because presenter is framework-free. |
| 72 | `ui-app-resume` | qa | ui_ux | App resumes after being paused. | Snapshot re-renders current session state. |
| 73 | `ui-no-android-framework-in-core` | engineering | ui_ux | UI work starts before Android module. | Core/preview remain framework-free. |
| 74 | `privacy-peer-mac-redaction` | privacy | privacy | Raw MAC address appears in peer id. | Diagnostics export only peer hash. |
| 75 | `privacy-device-name-redaction` | privacy | privacy | Device name contains a person name. | Diagnostics redact the device name. |
| 76 | `privacy-audio-payload-redaction` | privacy | privacy | Audio payload bytes reach diagnostics. | Diagnostics omit raw payload and show byte count only. |
| 77 | `privacy-preview-no-sensitive-data` | privacy | privacy | Preview HTML is shared in a bug report. | Preview contains only coarse metadata. |
| 78 | `privacy-logs-no-peer-name` | security | privacy | Transport timeline is copied to support. | Timeline reports frame type/counts only. |
| 79 | `privacy-hash-correlation` | support | privacy | Support needs to correlate events. | Use stable peer hash without raw id. |
| 80 | `privacy-crash-after-audio` | security | privacy | Crash occurs after receiving audio. | Support export must not include payload bytes. |
| 81 | `privacy-multiple-users` | privacy | privacy | Several peers are nearby. | Diagnostics must not enumerate raw nearby device names. |
| 82 | `privacy-permission-export` | privacy | privacy | Permission state is exported. | Only coarse grant/block state is exported. |
| 83 | `privacy-qa-screenshot` | privacy | privacy | QA screenshot contains preview cards. | No raw identifiers appear in UI preview. |
| 84 | `reliability-low-battery` | product | reliability | Device enters low battery mode. | Do not claim stable audio without heartbeat evidence. |
| 85 | `reliability-airplane-mode` | user | reliability | Airplane mode interrupts radio. | Show disconnected/Bluetooth blocked path. |
| 86 | `reliability-incoming-call` | user | reliability | Incoming call interrupts microphone/audio route. | Stop or block transmit behind audio seam. |
| 87 | `reliability-notification-sound` | qa | reliability | Notification sound plays during receive. | Keep receive state and avoid payload logging. |
| 88 | `reliability-cpu-pressure` | engineering | reliability | CPU pressure delays frame handling. | Jitter/heartbeat diagnostics reveal degraded health. |
| 89 | `reliability-memory-pressure` | engineering | reliability | Memory pressure occurs during buffering. | Buffers stay bounded. |
| 90 | `reliability-rapid-ptt-toggle` | qa | reliability | User rapidly toggles PTT. | State remains deterministic and counters bounded. |
| 91 | `reliability-double-tap-scan` | qa | reliability | User double taps scan. | Discovery remains bounded and idempotent enough for MVP. |
| 92 | `reliability-peer-reboot` | support | reliability | Peer phone reboots mid-session. | Disconnect and wait for user reconnect. |
| 93 | `reliability-app-process-killed` | engineering | reliability | App process is killed. | Future Android layer must restart cleanly without stale transmit. |
| 94 | `release-two-device-ping-pong` | qa | release | Two physical devices run sustained ping/pong. | Pass physical stability gate before audio transport. |
| 95 | `release-two-device-ptt` | qa | release | Two physical devices complete PTT smoke test. | Meet click budget and audio audibility gate. |
| 96 | `release-diagnostics-export` | support | release | User exports support diagnostics. | Export includes app/coarse counters and no sensitive payload. |
| 97 | `release-rollback-trigger` | product | release | A gate fails during MVP validation. | Rollback/pivot trigger is explicit. |
| 98 | `release-nearby-alternative` | engineering | release | Bluetooth Classic fails reliability gate. | Preserve seams to pivot to Nearby/Wi-Fi Direct. |
| 99 | `release-store-privacy-review` | privacy | release | Store/privacy review asks about identifiers. | Show redaction policy and no raw payload logging. |
| 100 | `release-support-runbook` | support | release | Support needs a troubleshooting path. | Diagnostics and user messages map to runbook actions. |
## Definition of done for 100/100
- Every module score in this document reaches 100 with committed evidence.
- Every stakeholder row has a passed acceptance gate and no open no-go item.
- All 100 simulation scenarios pass and any newly discovered real-device issue is added as scenario 101+ before fixing.
- Physical-device tests prove Bluetooth data stability before realtime audio is accepted.
- Diagnostics export remains useful for support and safe for privacy review.
