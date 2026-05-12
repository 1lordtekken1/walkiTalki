package com.walkitalki.app;

public final class AppUiCopy {
    private final String title;
    private final String status;
    private final String primaryAction;
    private final String diagnostics;
    private final String peerSummary;
    private final String buttonLabel;
    private final boolean transmitting;
    private final boolean pushToTalkEnabled;

    private AppUiCopy(
            String title,
            String status,
            String primaryAction,
            String diagnostics,
            String peerSummary,
            String buttonLabel,
            boolean transmitting,
            boolean pushToTalkEnabled) {
        this.title = title;
        this.status = status;
        this.primaryAction = primaryAction;
        this.diagnostics = diagnostics;
        this.peerSummary = peerSummary;
        this.buttonLabel = buttonLabel;
        this.transmitting = transmitting;
        this.pushToTalkEnabled = pushToTalkEnabled;
    }

    public static AppUiCopy defaultCopy() {
        return ready("ui_ready:ptt_enabled");
    }

    public static AppUiCopy idle(String diagnosticsSignal) {
        return new AppUiCopy(
                "walkiTalki",
                "Поиск собеседника",
                "Нажмите, чтобы начать поиск",
                "Диагностика без MAC, raw peer ID и аудио payload • " + diagnosticsSignal,
                "Bluetooth Classic MVP • fake peer discovery",
                "SCAN",
                false,
                false);
    }

    public static AppUiCopy scanning(String diagnosticsSignal) {
        return new AppUiCopy(
                "walkiTalki",
                "Ищем ближайшие устройства",
                "Нажмите ещё раз для fake peer",
                "Диагностика без MAC, raw peer ID и аудио payload • " + diagnosticsSignal,
                "Bluetooth Classic MVP • peer discovery active",
                "...",
                false,
                false);
    }

    public static AppUiCopy ready(String diagnosticsSignal) {
        return new AppUiCopy(
                "walkiTalki",
                "Готов к локальной PTT-связи",
                "Нажмите и удерживайте PTT",
                "Диагностика без MAC, raw peer ID и аудио payload • " + diagnosticsSignal,
                "Bluetooth Classic MVP • P2P seam готов",
                "PTT",
                false,
                true);
    }

    public static AppUiCopy transmitting(String diagnosticsSignal) {
        return new AppUiCopy(
                "walkiTalki",
                "Передача голоса активна",
                "Отпустите, чтобы слушать",
                "Диагностика без MAC, raw peer ID и аудио payload • " + diagnosticsSignal,
                "Bluetooth Classic MVP • fake audio frame",
                "TALK",
                true,
                true);
    }

    public String title() {
        return title;
    }

    public String status() {
        return status;
    }

    public String primaryAction() {
        return primaryAction;
    }

    public String diagnostics() {
        return diagnostics;
    }

    public String peerSummary() {
        return peerSummary;
    }

    public String buttonLabel() {
        return buttonLabel;
    }

    public boolean transmitting() {
        return transmitting;
    }

    public boolean pushToTalkEnabled() {
        return pushToTalkEnabled;
    }

    public String diagnosticsSignal() {
        int separator = diagnostics.lastIndexOf("• ");
        if (separator < 0) {
            return diagnostics;
        }
        return diagnostics.substring(separator + 2);
    }
}
