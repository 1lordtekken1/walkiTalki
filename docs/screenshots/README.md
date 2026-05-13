# walkiTalki app screenshots

PNG screenshots are generated locally and intentionally ignored by Git so PRs remain text-only. The renderer uses `AppScreenshotCatalog`, which uses `AppTalkController` and `TalkScreenPresenter` state rather than hard-coded renderer-only copy.

Generate them with:

```bash
ANDROID_HOME=/opt/android-sdk ./gradlew renderAppScreenshots --no-daemon
```

| State | Generated image path | Diagnostics signal |
| --- | --- | --- |
| Launch / scan | `docs/screenshots/walkitalki-mvp-01-scan.png` | `ui_idle` |
| Scanning | `docs/screenshots/walkitalki-mvp-02-scanning.png` | `ui_scanning` |
| Ready PTT | `docs/screenshots/walkitalki-main.png` | `ui_ready:ptt_enabled` |
| Transmitting | `docs/screenshots/walkitalki-ptt-active.png` | `ui_transmitting` |
