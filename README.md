# walkiTalki

`walkiTalki` — Android-приложение walkie-talkie для голосовой связи рядом находящихся устройств без интернета, с приоритетом Bluetooth-режима и архитектурной возможностью добавить альтернативные peer-to-peer транспорты.

## Текущее состояние

Репозиторий теперь содержит dependency-light JVM `:core`, browser `:preview` и минимальный installable Android `:app` shell. Android-приложение собирает debug APK, показывает PTT-экран, переключает состояние при удержании PTT-кнопки и остаётся за архитектурными seam'ами: реальный Bluetooth/audio-код ещё не подключён напрямую к UI, а core продолжает валидироваться JVM harness'ом.

## Скриншоты Android app shell

Скриншоты MVP не хранятся в Git, чтобы PR оставался text-only и не падал в Codex UI на binary diff. Они генерируются локально из `AppScreenshotCatalog`, который получает состояния через `AppTalkController`/`TalkScreenPresenter`, а не из отдельной статичной копии renderer-only. Подтверждающий каталог: [docs/screenshots/README.md](docs/screenshots/README.md).

Скриншоты воспроизводятся командой и появляются в `docs/screenshots/*.png`:

```bash
./gradlew renderAppScreenshots --no-daemon
```

## Документы

- [Development Rules](docs/development-rules.md) — TDD-first правила, тестовая пирамида, acceptance gates и definition of done.
- [Design System Draft](docs/design-system.md) — UX-принципы, экраны, состояния и визуальные токены для будущей Compose-реализации.
- [ADR 0002](docs/adr/0002-permissions-and-ptt-state.md) — pure permission policy и PTT state machine до Android UI/Bluetooth-кода.
- [ADR 0003](docs/adr/0003-audio-jitter-buffer.md) — framework-free audio frame и jitter buffer baseline до `AudioRecord`/`AudioTrack`.
- [ADR 0004](docs/adr/0004-protocol-stream-and-transport-seam.md) — protocol stream reader и начальный `VoiceTransport` seam до Bluetooth-кода.
- [ADR 0005](docs/adr/0005-framework-free-ui-state.md) — framework-free talk-screen state до Compose-кода.
- [ADR 0006](docs/adr/0006-browser-preview-surface.md) — browser-runnable preview surface до Android SDK/Compose сборки.
- [ADR 0007](docs/adr/0007-heartbeat-clock-diagnostics-seams.md) — deterministic heartbeat, clock и diagnostics seams до real Bluetooth-кода.
- [ADR 0008](docs/adr/0008-bounded-peer-discovery-controller.md) — bounded peer discovery controller до platform Bluetooth discovery.
- [ADR 0009](docs/adr/0009-stream-voice-transport-data-slice.md) — stream-backed `VoiceTransport` data slice до `BluetoothSocket` wrapper.
- [ADR 0010](docs/adr/0010-ptt-audio-controller.md) — framework-free PTT audio controller до `AudioRecord`/`AudioTrack` adapters.
- [ADR 0011](docs/adr/0011-framework-free-session-controller.md) — framework-free session controller до Android ViewModel/Compose event handlers.
- [ADR 0012](docs/adr/0012-framework-boundary-guard.md) — executable guard для framework-free boundaries `:core`/`:preview`.
- [User Behavior Simulation](docs/user-behavior-simulation.md) — pure JVM симуляции пользовательских сценариев и quality gates до Android UI.
- [Vertical Slice Readiness Roadmap](docs/vertical-slice-readiness-roadmap.md) — module/stakeholder 0-100 readiness, route to 100, and executable 100-scenario simulation catalog.
- [QA Browser/Connection/Click Plan](docs/qa-browser-connection-click-plan.md) — блоки browser testing, stability testing, click budgets и режимы.
- [Development Progress](docs/development-progress.md) — текущий этап разработки, что готово и что осталось.
- [MVP Roadmap](docs/mvp-roadmap.md) — 8 оставшихся major steps/gates до настоящего MVP.
- [Existing Repository Analysis](docs/existing-repositories-analysis.md) — анализ похожих Android/PTT/offline/Bluetooth решений и практик для заимствования.
- [Start Readiness Review](docs/start-readiness-review.md) — повторная атака плана и GO/NO-GO решение перед началом реализации.
- [Critical Plan Review](docs/critical-plan-review.md) — атака на план со стороны стейкхолдеров и devil's advocate по каждому пункту реализации.

## Быстрый старт разработки

```bash
./gradlew test --no-daemon
```

The build uses Gradle Groovy DSL and `javac --release 21`. The Gradle wrapper scripts/properties are checked in; regenerate `gradle/wrapper/gradle-wrapper.jar` locally with a trusted Gradle installation if your environment requires the wrapper JAR.

To build the installable Android debug APK:

```bash
ANDROID_HOME=/opt/android-sdk JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :app:assembleDebug --no-daemon
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

MVP APK для ручного тестирования можно собрать и скопировать в стабильный путь:

```bash
ANDROID_HOME=/opt/android-sdk ./gradlew packageMvpDebugApk --no-daemon
```

The packaged APK is written to `artifacts/apk/walkitalki-mvp-debug.apk`.

To render the browser preview surface:

```bash
./gradlew :preview:renderTalkScreenPreview --no-daemon
```

Open `preview/build/reports/talk-screen-preview.html` in a browser to review the current talk-screen states.
