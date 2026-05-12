# walkiTalki: критическая проверка плана реализации

Дата проверки: 2026-05-09.

## 1. Контекст и проверенные источники

Цель проекта — Android walkie-talkie для голосовой связи без интернета, в первую очередь через Bluetooth. Перед началом реализации план нужно не подтвердить, а попытаться сломать: проверить, где он не выдерживает продуктовых, технических, UX, privacy, security, QA и эксплуатационных ограничений.

Проверка опирается на следующие актуальные источники и факты:

- Android 12+ разделил Bluetooth-доступ на runtime permissions `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT`; старые permissions недостаточны для target SDK 31+.
- Android Bluetooth-документация различает Bluetooth Classic и BLE; для постоянного голосового канала BLE не должен быть принят как основной транспорт без отдельного доказательства пропускной способности и задержки.
- Android low-latency audio документация подчёркивает, что задержка зависит от железа, драйверов и аудио-пайплайна; гарантировать одинаковую latency на всех устройствах нельзя.
- Nearby Connections является возможной альтернативой для offline peer-to-peer, но это зависимость от Google Play services и не равно “только Bluetooth”.

Ссылки для архитектурных решений:

- Android Bluetooth permissions: https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
- Android Bluetooth connection/RFCOMM: https://developer.android.com/develop/connectivity/bluetooth/connect-bluetooth-devices
- Android AudioRecord: https://developer.android.com/reference/android/media/AudioRecord
- Android AudioTrack: https://developer.android.com/reference/android/media/AudioTrack
- Android audio latency: https://developer.android.com/ndk/guides/audio/audio-latency
- Nearby Connections codelab: https://developer.android.com/codelabs/nearby-connections

## 2. Исходный план, который атакуем

Базовый план состоял из таких пунктов:

1. Создать новый Android-репозиторий `walkiTalki`.
2. Сделать MVP на Bluetooth Classic RFCOMM, Nearby Connections оставить как альтернативный транспорт.
3. Добавить корректные Android permissions для Bluetooth и микрофона.
4. Реализовать аудио pipeline: `AudioRecord -> codec -> transport -> jitter buffer -> AudioTrack`.
5. Сделать Push-To-Talk UI.
6. Ввести бинарный протокол поверх BluetoothSocket.
7. Добавить обработку задержки, потерь, heartbeat и переподключения.
8. Позже добавить Opus, групповые режимы, Wi-Fi Direct или Nearby Connections.

## 3. Стейкхолдеры и их атака на план

### 3.1 Пользователь

**Что хочет:** нажал кнопку, сказал, другой человек услышал; без регистрации, без интернета, без сложной настройки.

**Атака:**

- Bluetooth pairing и discovery часто выглядят для обычного пользователя как техническая настройка, а не как простой walkie-talkie.
- Runtime permissions “Nearby devices”, “Microphone” и возможный запрос location на старых Android могут вызвать недоверие.
- Если первая попытка соединения занимает больше 20-30 секунд, пользователь решит, что приложение не работает.
- Если пользователь слышит задержку больше 500-700 ms, продукт перестаёт ощущаться как рация.

**Вывод:** MVP должен измеряться не только “соединилось”, а временем до первого успешного разговора, понятностью ошибок и долей успешных соединений между реальными устройствами.

### 3.2 Android-разработчик

**Что хочет:** реализуемая архитектура без преждевременной сложности.

**Атака:**

- Одновременно делать Bluetooth Classic, Opus, jitter buffer, reconnect, Compose UI и абстрактный transport слой — риск распыления.
- BluetoothSocket blocking IO легко привести к зависшим coroutine/thread, утечкам ресурсов и неочевидным race conditions.
- Discovery и connect нельзя смешивать без строгого state machine: Android Bluetooth stack плохо переносит хаотичные scan/connect/cancel операции.
- Half-duplex PTT проще, но всё равно требует arbitration: что делать, если оба нажали кнопку одновременно?

**Вывод:** сначала нужен “thin vertical slice”: два устройства, ручной pairing, один RFCOMM socket, один PTT поток, PCM или простейший codec, затем расширение.

### 3.3 QA-инженер

**Что хочет:** тестируемые критерии и матрица устройств.

**Атака:**

- Эмулятор не проверит реальный Bluetooth audio path.
- Поведение отличается по производителям: Samsung, Pixel, Xiaomi, OnePlus, Motorola могут вести себя по-разному.
- Без автоматизированных contract tests для frame protocol ошибки проявятся только на устройствах.
- Без логирования session metrics невозможно понять, виноват Bluetooth, codec, audio buffer или UI state.

**Вывод:** в MVP нужно включить diagnostic mode, structured logs, synthetic frame tests и обязательную matrix-проверку минимум на 3-4 физических устройствах.

### 3.4 Security / Privacy

**Что хочет:** минимизация утечек, MITM-рисков и скрытой записи.

**Атака:**

- “Работает по Bluetooth” не означает автоматически безопасно: pairing, bonding и доверие к peer нужно явно описать.
- Пользователь должен понимать, кто подключён, и иметь возможность быстро разорвать соединение.
- Аудио нельзя писать на диск по умолчанию.
- Device name может содержать персональные данные; нельзя бездумно логировать и показывать лишнее.
- Если добавить Nearby Connections, модель угроз меняется: появляются Google Play services и другой discovery/connect stack.

**Вывод:** MVP должен иметь explicit consent перед соединением, явный индикатор микрофона/передачи, no-recording-by-default, redacted logs и простую peer verification процедуру.

### 3.5 Product owner

**Что хочет:** быстрый MVP, который можно показать.

**Атака:**

- Bluetooth-only может оказаться менее впечатляющим, чем Wi-Fi Direct/Nearby Connections, из-за дальности и стабильности.
- Групповой walkie-talkie — ожидаемая фича, но Bluetooth Classic в простом RFCOMM MVP естественно ведёт к 1-to-1.
- Если позиционировать как “работает везде без интернета”, придётся поддерживать старые Android и разные устройства, что дорого.

**Вывод:** обещание MVP должно быть узким: “1-to-1 PTT between two nearby Android devices, no internet, best-effort Bluetooth Classic”.

### 3.6 UX/UI дизайнер

**Что хочет:** понятный сценарий и recovery из ошибок.

**Атака:**

- Состояния “scanning”, “pairing”, “connecting”, “connected”, “transmitting”, “receiving”, “permission denied”, “Bluetooth off” нельзя свалить в один экран.
- Пользователь должен понимать разницу между “устройство найдено” и “можно говорить”.
- Ошибка “connection failed” бесполезна; нужны действия: включить Bluetooth, поднести устройства, повторить pairing, перезапустить discovery.

**Вывод:** UI должен проектироваться как state machine с текстом next best action, а не как один экран с кнопкой.

### 3.7 Legal / Compliance

**Что хочет:** не нарушить политики магазинов и privacy expectations.

**Атака:**

- Микрофон и Nearby devices — чувствительные разрешения; в Play Store потребуется понятное раскрытие purpose.
- Если появится запись аудио, хранение истории или analytics, резко растут требования к privacy policy.
- Нельзя обещать “emergency communication”, если приложение не сертифицировано и не гарантирует связь.

**Вывод:** в описании продукта избегать emergency/safety-critical обещаний; privacy policy должна быть подготовлена до публикации.

### 3.8 Support / Operations

**Что хочет:** меньше обращений “не работает”.

**Атака:**

- Bluetooth-проблемы трудно диагностировать удалённо.
- Без “copy diagnostics” пользователь не сможет сообщить модель телефона, Android version, permissions, transport state, error code.
- Если приложение молча падает в disconnected, support не сможет восстановить цепочку событий.

**Вывод:** с первой версии нужен diagnostics screen с exportable, privacy-safe логом.

## 4. Devil's advocate по каждому пункту реализации

### 4.1 Новый репозиторий и Android-скелет

**План:** Kotlin, Jetpack Compose, Gradle Kotlin DSL, minSdk 26+, target SDK актуальный.

**Devil's advocate:**

- Почему minSdk 26? Если продукт про “работает без интернета”, часть старых Android может быть важна.
- Почему Compose? Он ускоряет UI, но увеличивает baseline dependencies и требования к toolchain.
- Почему сразу полноценный Android app, а не proof-of-concept module с минимальным Activity?

**Решение:** оставить Kotlin + Compose, но первый milestone должен быть техническим demo, а не polished app. MinSdk выбрать после оценки целевой аудитории; предварительно minSdk 26 разумен для снижения legacy Bluetooth/audio боли.

**Gate:** репозиторий считается готовым к разработке только после CI, lint, unit test skeleton и README с ограничениями MVP.

### 4.2 Bluetooth Classic RFCOMM как основной транспорт

**План:** использовать Bluetooth Classic RFCOMM для 1-to-1 канала.

**Devil's advocate:**

- RFCOMM не гарантирует низкую latency для аудио и может быть нестабилен на некоторых прошивках.
- Bluetooth discovery медленный и UX-тяжёлый.
- Для группы RFCOMM потребует сложной топологии или отдельных соединений.
- Многие пользователи ожидают, что “Bluetooth walkie-talkie” будет работать без ручного pairing, но Android может потребовать pairing/bonding или discovery confirmation.

**Решение:** RFCOMM принять только как MVP hypothesis, не как окончательную платформенную ставку. Ввести `VoiceTransport` interface, но не переусложнять реализацию.

**Gate:** если на 4 реальных устройствах 1-to-1 connection success rate ниже 80% за 30 секунд, нужно пересмотреть основной транспорт в пользу Nearby Connections/Wi-Fi Direct.

### 4.3 Nearby Connections как fallback

**План:** заложить альтернативный транспорт позже.

**Devil's advocate:**

- Nearby Connections зависит от Google Play services; это исключает часть устройств и меняет privacy story.
- Если добавить fallback слишком рано, QA-матрица удвоится.
- Если добавить слишком поздно, архитектура может уже зацементироваться вокруг BluetoothSocket semantics.

**Решение:** абстрагировать только минимальные события transport слоя: peer found, connected, frame received, disconnected, error. Не реализовывать Nearby в первом milestone.

**Gate:** fallback добавляется только после измеренного провала Bluetooth Classic или явной продуктовой потребности в дальности/группах.

### 4.4 Permissions

**План:** добавить Bluetooth и microphone permissions.

**Devil's advocate:**

- Permissions отличаются по Android version; неправильный manifest приведёт к неработающему discovery/connect.
- `BLUETOOTH_SCAN` может потребовать объяснения, что app не определяет location, если используется `neverForLocation`.
- На Android <= 11 Bluetooth discovery часто связан с location permission, что выглядит подозрительно для пользователя.
- Пользователь может дать microphone, но не nearby devices, или наоборот; UI должен поддерживать partial grants.

**Решение:** сделать permissions отдельным feature/state, не размазывать проверки по transport/audio слоям. В README и privacy policy объяснить назначение каждого permission.

**Gate:** automated permission tests/flows на Android 11, 12, 13+ или документированная ручная matrix-проверка.

### 4.5 AudioRecord / AudioTrack pipeline

**План:** записывать микрофон, кодировать, передавать, буферизовать, воспроизводить.

**Devil's advocate:**

- Raw PCM 16 kHz mono 16-bit уже около 256 kbps без overhead; Bluetooth Classic может потянуть, но latency и стабильность не гарантированы.
- AudioRecord buffer underrun/overrun приведёт к щелчкам и заиканиям.
- Android audio route может уйти в Bluetooth headset, speaker или earpiece не так, как ожидается.
- Echo/feedback возможны, если устройства рядом и громкость высокая.

**Решение:** начать с PCM только для измеримого prototype, но сразу заложить заменяемый codec interface. Включить VAD/AGC/echo considerations в backlog, не в MVP. Для PTT по умолчанию использовать speaker route осторожно и показывать рекомендацию держать устройства на расстоянии.

**Gate:** end-to-end one-way audio latency должна быть измерена; целевой MVP threshold: медиана <= 700 ms, отсутствие постоянных underrun/overrun в 2-минутном PTT тесте.

### 4.6 Opus codec

**План:** добавить Opus после рабочего PCM.

**Devil's advocate:**

- Opus через native library усложнит сборку, ABI, лицензии и crash surface.
- Без codec latency tuning Opus может не решить UX-проблему.
- Преждевременное внедрение codec скроет transport/audio bugs.

**Решение:** Opus не включать в первый vertical slice. Сначала протокол и audio stability на PCM; затем добавить Opus как отдельный ADR и benchmark.

**Gate:** Opus добавляется, когда PCM prototype стабилен, а bottleneck подтверждён bandwidth/packet size, а не Bluetooth discovery/connect.

### 4.7 Push-To-Talk UI

**План:** большая кнопка talk, удержание для передачи.

**Devil's advocate:**

- Удержание кнопки может конфликтовать с accessibility и motor impairments.
- Если пользователь отпустил кнопку, последние audio frames могут потеряться без drain/flush.
- Если оба пользователя нажали PTT одновременно, UX должен объяснить busy state.
- Системный microphone privacy indicator может напугать, если запись продолжается после отпускания.

**Решение:** в MVP сделать hold-to-talk и optional lock-to-talk в backlog. После release добавить accessibility режим. Перед остановкой отправки делать короткий drain, но не продолжать запись скрыто.

**Gate:** UI тест: после release микрофон останавливается в течение 300 ms; state явно показывает, кто говорит.

### 4.8 Бинарный протокол фреймов

**План:** header, type, sequence, timestamp, length, payload.

**Devil's advocate:**

- Самописный протокол легко сломать partial reads, endian mismatch, oversized payload, corrupt frames.
- Без version negotiation приложение v1 и v2 могут не понимать друг друга.
- Без limits malicious или buggy peer может вызвать OOM большим length.
- Timestamp разных устройств нельзя напрямую сравнивать как wall-clock.

**Решение:** использовать monotonic sender timestamp только для относительных измерений; добавить max payload, version, magic, CRC или checksum later. Frame decoder должен быть покрыт property/fuzz-like tests на случайные bytes.

**Gate:** unit tests должны покрывать partial reads, concatenated frames, invalid magic, invalid length, unknown type и backward-compatible version rejection.

### 4.9 Jitter buffer, heartbeat, reconnect

**План:** sequence numbers, jitter buffer 60-120 ms, ping/pong, reconnect.

**Devil's advocate:**

- Jitter buffer уменьшает заикания, но добавляет задержку; слишком ранняя оптимизация может ухудшить ощущение рации.
- Автоматический reconnect может вступать в конфликт с user consent и permissions.
- Heartbeat traffic может маскировать зависший audio path, если control channel жив, а playback сломан.

**Решение:** в MVP делать простой buffer и ручной reconnect; auto reconnect добавить только после понятного UX и timeout semantics.

**Gate:** disconnect должен обнаруживаться за <= 5 секунд в idle и <= 2 секунды во время передачи; reconnect не должен стартовать без явного user action в первом MVP.

### 4.10 Group mode / mesh

**План:** позже.

**Devil's advocate:**

- Пользователи могут считать group mode базовой функцией walkie-talkie.
- Bluetooth Classic p2p плохо масштабируется.
- Mesh voice — это уже routing, arbitration, retransmission, latency и battery drain.

**Решение:** явно исключить group mode из MVP. Если группа критична, основной транспорт должен быть пересмотрен до реализации UI.

**Gate:** не начинать group mode, пока 1-to-1 metrics не достигнуты и не выбран транспорт для multi-peer.

### 4.11 Background mode

**План:** не был явно описан.

**Devil's advocate:**

- Пользователь ожидает принимать голос, когда экран выключен.
- Android background execution limits, foreground service и microphone indicators усложнят реализацию.
- Фоновая запись микрофона особенно чувствительна для privacy и Play policy.

**Решение:** MVP работает только foreground. Background receive/transmit — отдельный feature с foreground service notification и privacy review.

**Gate:** background mode запрещён до появления privacy policy, foreground service rationale и QA на battery drain.

### 4.12 Battery and thermal

**План:** не был явно описан.

**Devil's advocate:**

- Постоянный discovery, active socket, microphone и speaker быстро сажают батарею.
- Reconnect loops могут держать radio активным бесконечно.
- На старых устройствах audio processing может вызвать нагрев.

**Решение:** discovery должен быть ограничен по времени, reconnect — с backoff, microphone активен только во время PTT.

**Gate:** 15-минутный тест idle connected и 5-минутный PTT тест должны логировать battery delta, wake locks и active audio time.

### 4.13 Observability and diagnostics

**План:** не был достаточно сильным.

**Devil's advocate:**

- Без метрик невозможно принимать transport decisions.
- Logs могут случайно содержать device names, MAC-like identifiers или audio metadata.
- Crash reports без reproduction context бесполезны для Bluetooth.

**Решение:** добавить privacy-safe diagnostics с session id, device model, Android version, app version, permission state, transport state transitions, frame counters, underrun/overrun counters, но без audio payload и без необработанных peer identifiers.

**Gate:** любая beta-сборка должна иметь “Copy diagnostics” экран.

## 5. Пересмотренная стратегия реализации

### Milestone 0: Project foundation

- Android skeleton.
- CI/lint/unit test baseline.
- README с ограничениями MVP.
- Architecture Decision Records для transport, audio, permissions.

**Exit criteria:** проект собирается в CI; есть документированные non-goals.

### Milestone 1: Bluetooth text/data vertical slice

- Permissions flow.
- Pair/connect/disconnect.
- Binary protocol без audio.
- Test frame exchange между двумя устройствами.

**Exit criteria:** два устройства обмениваются `PING/PONG` и diagnostic counters показывают стабильный socket.

### Milestone 2: Foreground one-way PTT PCM

- AudioRecord на sender.
- AudioTrack на receiver.
- Hold-to-talk UI.
- Sequence numbers, frame counters, basic jitter buffer.

**Exit criteria:** 2-минутный PTT сценарий без падений, понятная задержка, нет постоянных underruns.

### Milestone 3: Reliability hardening

- State machine.
- Error taxonomy.
- Manual reconnect.
- Diagnostics screen.
- Device matrix testing.

**Exit criteria:** success rate >= 80% за 30 секунд на выбранной физической matrix.

### Milestone 4: Codec and transport decision

- Benchmark PCM vs Opus.
- Решение: остаёмся на Bluetooth Classic, добавляем Opus или переходим/добавляем Nearby/Wi-Fi Direct.

**Exit criteria:** решение принято на основе latency, bandwidth, battery и connection-success метрик.

## 6. Non-goals для первого MVP

- Групповой voice chat.
- Mesh routing.
- Background transmission.
- Запись и хранение аудио.
- Emergency/safety-critical positioning.
- Cross-platform iOS support.
- Автоматический reconnect без явного согласия пользователя.

## 7. Минимальная матрица рисков

| Риск | Вероятность | Влияние | Mitigation | Stop / Pivot trigger |
| --- | --- | --- | --- | --- |
| Bluetooth connect нестабилен | Высокая | Высокое | device matrix, diagnostics, ручной pairing fallback | <80% success за 30 sec |
| Задержка слишком высокая | Средняя | Высокое | frame size tuning, jitter metrics, Opus benchmark | median >700 ms после tuning |
| Permissions путают пользователей | Высокая | Среднее | guided permission UI, copy explaining purpose | >25% onboarding drop |
| BLE ошибочно выбран для audio | Средняя | Высокое | запрет BLE as primary audio без benchmark | BLE не проходит bandwidth/latency test |
| Battery drain | Средняя | Среднее | bounded discovery, no background MVP | unacceptable drain in 15-min test |
| Privacy concerns | Средняя | Высокое | no recording, redacted logs, explicit peer | жалобы/политика store |
| Group expectations | Высокая | Среднее | clear MVP scope | пользователи требуют group as core |

## 8. Что нужно сделать прямо следующим коммитом реализации

1. Создать Android skeleton.
2. Добавить `docs/adr/0001-transport-strategy.md`.
3. Добавить `docs/adr/0002-audio-pipeline.md`.
4. Добавить `docs/adr/0003-privacy-permissions.md`.
5. Реализовать только data-channel proof-of-concept до audio.
6. Ввести diagnostics model до первой Bluetooth-реализации, а не после.

## 9. Независимое заключение

Изначальный план реалистичен только при сужении обещания MVP. Самая опасная ошибка — начать с “полноценного walkie-talkie” и одновременно решать discovery, Bluetooth compatibility, realtime audio, codec, UX, permissions и reconnection. Правильный путь — последовательные vertical slices с измеримыми gates.

Рекомендация: стартовать с Bluetooth Classic RFCOMM как проверяемой гипотезы, но заранее признать возможность pivot на Nearby Connections или Wi-Fi Direct, если реальные устройства покажут плохой connection success или latency.
