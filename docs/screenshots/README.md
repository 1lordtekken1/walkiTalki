# walkiTalki app screenshots

PNG screenshots are generated locally from `AppScreenshotCatalog`, which uses `AppTalkController` and `TalkScreenPresenter` state rather than hard-coded renderer-only copy. The generated PNG files are ignored by Git by default; this README embeds them when they are present locally so reviewers can inspect element placement after running the renderer.

Generate them with:

```bash
ANDROID_HOME=/opt/android-sdk ./gradlew renderAppScreenshots --no-daemon
```

| State | Image | Diagnostics signal |
| --- | --- | --- |
| Launch / scan | ![Launch / scan](walkitalki-mvp-01-scan.png) | `ui_idle` |
| Scanning | ![Scanning](walkitalki-mvp-02-scanning.png) | `ui_scanning` |
| Ready PTT | ![Ready PTT](walkitalki-main.png) | `ui_ready:ptt_enabled` |
| Transmitting | ![Transmitting](walkitalki-ptt-active.png) | `ui_transmitting` |
