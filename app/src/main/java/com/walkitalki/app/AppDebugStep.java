package com.walkitalki.app;

public record AppDebugStep(
        String action,
        String status,
        String buttonLabel,
        String diagnosticsSignal,
        boolean transmitting,
        boolean pushToTalkEnabled) {
    static AppDebugStep capture(String action, AppUiCopy copy) {
        return new AppDebugStep(
                action,
                copy.status(),
                copy.buttonLabel(),
                copy.diagnosticsSignal(),
                copy.transmitting(),
                copy.pushToTalkEnabled());
    }
}
