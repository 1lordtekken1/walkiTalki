# ADR 0015: UI intent ViewModel seam before Compose

Date: 2026-05-12

## Status

Accepted

## Context

MVP roadmap gate 2 requires a thin UI surface over `TalkScreenPresenter`/session state while preserving the rule that Compose or Android views must not call Bluetooth or audio APIs directly. The current Android shell used a custom `View` that called `AppTalkController` methods directly from touch handling. Before adding production Compose, the app needs a small, JVM-testable intent boundary that future Compose can reuse.

## Decision

Add `AppTalkViewModel`, `AppTalkUiIntent`, and `AppTalkRenderModel`/`AppTalkUiRenderer` in the `:app` layer. The ViewModel/render contract:

- exposes `state()` as `AppUiCopy` generated through the existing controller/presenter path;
- accepts UI intents (`PRIMARY_ACTION`, `PUSH_TO_TALK_DOWN`, `PUSH_TO_TALK_UP`) instead of exposing controller mutation methods to Android views;
- ignores PTT-down until presenter state says PTT is enabled;
- returns the existing redacted support report;
- exposes a Compose-ready render model with press/release intents, content description, diagnostics signal, and minimum touch target metadata;
- contains no Android framework imports, Bluetooth APIs, audio APIs, raw peer identifiers, or raw audio payload handling.

The current `WalkiTalkiStatusView` now routes touch events through this intent/render seam. Future Compose should use the same seam or a compatible interface rather than calling transport/audio/platform APIs directly.

## Acceptance criteria

- JVM tests cover primary action and PTT intents through scan, scanning, ready, transmitting, and release states.
- PTT-down before the presenter enables push-to-talk is ignored.
- Support diagnostics remain redacted after UI intents.
- Android view code delegates user intent to `AppTalkViewModel`/`AppTalkUiRenderer` instead of mutating controller state directly.

## Rollback/pivot trigger

Rollback or redesign this seam if future Compose needs state not provided by `AppUiCopy`/`TalkScreenState`, if UI starts duplicating presenter rules, or if Bluetooth/audio calls leak into UI event handlers.

## Diagnostics signal

UI diagnostics remain coarse presenter/app signals such as `ui_idle`, `ui_scanning`, `ui_ready:ptt_enabled`, and `ui_transmitting`. Do not emit MAC addresses, raw peer IDs, unredacted device names, or audio payload data.
