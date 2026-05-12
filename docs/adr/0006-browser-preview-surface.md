# ADR 0006: Browser preview surface before Android Compose

Date: 2026-05-11.
Status: Accepted.

## Context

The plan calls for an Android module and Compose design-preview surface after the framework-free `TalkScreenPresenter`. This execution environment does not provide `ANDROID_HOME`/`ANDROID_SDK_ROOT`, so Android Gradle Plugin and Compose compilation cannot be verified yet without introducing an untestable build path.

The project still needs reviewable UI states and click-budget checks before real Bluetooth/audio work.

## Decision

Add a dependency-light `:preview` JVM module that renders browser-runnable static HTML from the tested `TalkScreenPresenter` state model.

The preview module must:

- depend on `:core` instead of Android framework APIs;
- render permission-blocked, ready-to-talk, and disconnected/over-budget scenarios;
- expose QA hooks such as `data-diagnostics` and `data-click-budget`;
- keep raw peer identifiers, MAC addresses, device names, and audio payloads out of markup;
- provide a Gradle task that writes `preview/build/reports/talk-screen-preview.html` for browser review.

## Acceptance criteria

- `:preview:test` verifies the rendered markup contains the core talk-screen states, accessible action labels, diagnostics hooks, and click-budget failure markers.
- The preview renderer consumes `TalkScreenState`; it must not inspect Bluetooth, audio, transport, or Android permission APIs directly.
- `./gradlew test --no-daemon` runs both the core harness and preview checks.

## Rollback / pivot trigger

Rollback this preview layer if it starts duplicating UI business rules already owned by `TalkScreenPresenter`, logs sensitive peer/audio data, or blocks migration to an Android Compose renderer when an Android SDK is available.

## Diagnostics signal

Preview markup may include only coarse diagnostics signals already produced by `TalkScreenState`, for example `ui_blocked:REQUEST_NEARBY_DEVICES` and `ui_ready:ptt_enabled`.

## Consequences

Positive:

- UI states are reviewable in a browser without Android SDK setup.
- Click-budget and diagnostics hooks are executable before Compose exists.
- Compose can later reuse the same `TalkScreenPresenter` expectations.

Negative:

- This is not a substitute for Android Compose previews or screenshot tests.
- Styling is intentionally minimal and should not be treated as final Android visual design.
