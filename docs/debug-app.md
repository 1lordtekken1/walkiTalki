# Debug test app harness

The current Android `:app` is still a framework-light MVP/debug shell, not a real Bluetooth/audio release. It is useful for local QA because it runs a deterministic fake peer flow through the same app/controller state used by the UI:

1. launch / `SCAN`;
2. tap to enter scanning;
3. tap again to connect a fake peer and enable `PTT`;
4. hold PTT to enter `TALK`;
5. release PTT to return to listening.

The automated harness is `AppDebugHarness`. It produces an `AppDebugReport` with a PASS/FAIL summary, each UI action, button label, and coarse diagnostics signal. The report intentionally avoids MAC addresses, raw peer identifiers, unredacted device names, and audio payload data.

## What this proves

- Android app imports compile through `:app:compileDebugJavaWithJavac`, so the Java/Android symbols used by the shell are resolvable in the configured SDK.
- `:app:testDebugUnitTest` exercises the fake scan/connect/PTT/release flow and verifies the public labels, diagnostics states, and redaction rules.
- `renderMvpDebugSmokeReport` generates a local markdown artifact from app code, not from hand-written documentation.
- `renderAppScreenshots` renders PNG previews from the same `AppScreenshotCatalog` and `AppTalkController` states used by the app shell, so reviewers can visually inspect whether the MVP controls are in the expected states.
- `packageMvpDebugApk` assembles an installable debug APK and writes a SHA-256 checksum for manual QA handoff.

## What this does not prove yet

- It does not prove real-device Bluetooth Classic, `BluetoothSocket`, `AudioRecord`, or `AudioTrack` behavior. Those remain behind seams until instrumented/physical-device smoke tests are added.
- It does not certify a final 2026 production visual design. The current shell is a deliberately minimal MVP/debug UI; production design still needs accessibility review, responsive layouts, localization review, and physical-device usability checks.
- It does not claim Android 16/API 36 readiness in this container because only `android-35` is installed locally. The build currently targets API 35, which is enough for current Google Play submission policy, while API 36 migration should be a separate tested SDK upgrade.

Run the app unit checks:

```bash
ANDROID_HOME=/opt/android-sdk ./gradlew :app:testDebugUnitTest --no-daemon
```

Render a redacted local smoke report:

```bash
ANDROID_HOME=/opt/android-sdk ./gradlew renderMvpDebugSmokeReport --no-daemon
```

Render screenshot previews:

```bash
ANDROID_HOME=/opt/android-sdk ./gradlew renderAppScreenshots --no-daemon
```

Package the debug APK:

```bash
ANDROID_HOME=/opt/android-sdk ./gradlew packageMvpDebugApk --no-daemon
```

The generated report is written to `build/reports/mvp-debug-smoke-report.md`, screenshots are written under `docs/screenshots/`, and the packaged APK is written under `artifacts/apk/`. Generated binaries/reports are intentionally not checked into Git unless they are small documentation fixtures.
