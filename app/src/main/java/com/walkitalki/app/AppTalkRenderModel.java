package com.walkitalki.app;

public record AppTalkRenderModel(
        String title,
        String status,
        String primaryAction,
        String buttonLabel,
        boolean transmitting,
        boolean pushToTalkEnabled,
        String diagnosticsSignal,
        String contentDescription,
        int minimumTouchTargetDp,
        AppTalkUiIntent pressIntent,
        AppTalkUiIntent releaseIntent) {
    public AppTalkRenderModel {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        if (primaryAction == null || primaryAction.isBlank()) {
            throw new IllegalArgumentException("primaryAction is required");
        }
        if (buttonLabel == null || buttonLabel.isBlank()) {
            throw new IllegalArgumentException("buttonLabel is required");
        }
        if (diagnosticsSignal == null || diagnosticsSignal.isBlank()) {
            throw new IllegalArgumentException("diagnosticsSignal is required");
        }
        if (contentDescription == null || contentDescription.isBlank()) {
            throw new IllegalArgumentException("contentDescription is required");
        }
        if (minimumTouchTargetDp < 48) {
            throw new IllegalArgumentException("minimumTouchTargetDp must be at least 48dp");
        }
        if (pressIntent == null) {
            throw new IllegalArgumentException("pressIntent is required");
        }
        if (releaseIntent == null) {
            throw new IllegalArgumentException("releaseIntent is required");
        }
    }
}
