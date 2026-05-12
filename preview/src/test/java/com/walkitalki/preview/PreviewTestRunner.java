package com.walkitalki.preview;

public final class PreviewTestRunner {
    public static void main(String[] args) {
        previewPageRendersCoreTalkStates();
        previewPageCoversAllMvpInteractionStates();
        previewPageExposesMachineReadableQaMetadata();
        previewPageExposesStableAutomationHooks();
        previewPageKeepsDiagnosticsAndSensitiveDataOutOfMarkup();
        previewPageExposesClickBudgetFailureForQa();
        System.out.println("Preview unit tests passed");
    }

    private static void previewPageRendersCoreTalkStates() {
        String html = TalkScreenPreviewPage.renderDefaultScenarios();

        assertContains(html, "<!doctype html>", "html document");
        assertContains(html, "Enable Nearby devices", "permission card title");
        assertContains(html, "Ready. Hold to talk.", "ready card title");
        assertContains(html, "Disconnected", "disconnected card title");
        assertContains(html, "data-diagnostics=\"ui_ready:ptt_enabled\"", "ready diagnostics hook");
        assertContains(html, "aria-label=\"Hold to talk\"", "button accessibility label");
    }

    private static void previewPageCoversAllMvpInteractionStates() {
        String html = TalkScreenPreviewPage.renderDefaultScenarios();

        assertContains(html, "id=\"permission-blocked\"", "permission scenario id");
        assertContains(html, "id=\"scanning\"", "scanning scenario id");
        assertContains(html, "id=\"connecting\"", "connecting scenario id");
        assertContains(html, "id=\"ready-to-talk\"", "ready scenario id");
        assertContains(html, "id=\"transmitting\"", "transmitting scenario id");
        assertContains(html, "id=\"receiving\"", "receiving scenario id");
        assertContains(html, "id=\"busy\"", "busy scenario id");
        assertContains(html, "id=\"bluetooth-blocked\"", "bluetooth blocked scenario id");
        assertContains(html, "id=\"disconnected-over-budget\"", "disconnected scenario id");
        assertContains(html, "Talking…", "transmitting title");
        assertContains(html, "Peer is talking", "receiving title");
        assertContains(html, "data-diagnostics=\"ui_receiving:BUSY\"", "busy diagnostics hook");
    }

    private static void previewPageExposesMachineReadableQaMetadata() {
        String html = TalkScreenPreviewPage.renderDefaultScenarios();

        assertContains(html, "id=\"ready-to-talk\" data-testid=\"talk-scenario-ready-to-talk\" data-ptt-state=\"CONNECTED\"", "ready ptt state marker");
        assertContains(html, "data-permission-action=\"ALLOW\"", "permission action marker");
        assertContains(html, "data-bluetooth-state=\"BLUETOOTH_OFF\"", "bluetooth state marker");
        assertContains(html, "data-connection-health=\"DISCONNECTED\"", "connection health marker");
        assertContains(html, "data-primary-enabled=\"true\"", "enabled primary action marker");
        assertContains(html, "data-primary-enabled=\"false\"", "disabled primary action marker");
    }

    private static void previewPageExposesStableAutomationHooks() {
        String html = TalkScreenPreviewPage.renderDefaultScenarios();

        assertContains(html, "<main data-preview-schema=\"talk-screen-v1\">", "preview schema marker");
        assertContains(html, "data-testid=\"talk-scenario-ready-to-talk\"", "ready scenario automation id");
        assertContains(html, "data-testid=\"talk-scenario-disconnected-over-budget\"", "disconnected scenario automation id");
    }

    private static void previewPageKeepsDiagnosticsAndSensitiveDataOutOfMarkup() {
        String html = TalkScreenPreviewPage.renderDefaultScenarios();

        assertFalse(html.contains("AA:BB:CC:DD:EE:FF"), "preview must not include raw MAC addresses");
        assertFalse(html.contains("Alice Personal Phone"), "preview must not include raw device names");
        assertFalse(html.contains("11, 12, 13"), "preview must not include audio payload bytes");
        assertContains(html, "ui_blocked:REQUEST_NEARBY_DEVICES", "coarse permission diagnostics signal");
    }

    private static void previewPageExposesClickBudgetFailureForQa() {
        String html = TalkScreenPreviewPage.renderDefaultScenarios();

        assertContains(html, "data-click-budget=\"over\"", "over-budget QA marker");
        assertContains(html, "Rollback if connect-to-talk needs more than 3 actions", "rollback trigger visible");
    }

    private static void assertContains(String actual, String expectedPart, String label) {
        if (!actual.contains(expectedPart)) {
            throw new AssertionError(label + ": expected to contain " + expectedPart + " in " + actual);
        }
    }

    private static void assertFalse(boolean condition, String label) {
        if (condition) {
            throw new AssertionError(label);
        }
    }
}
