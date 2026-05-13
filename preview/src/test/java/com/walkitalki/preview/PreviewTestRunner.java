package com.walkitalki.preview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PreviewTestRunner {
    public static void main(String[] args) {
        previewPageRendersCoreTalkStates();
        previewPageCoversAllMvpInteractionStates();
        previewPageExposesMachineReadableQaMetadata();
        previewPageExposesStableAutomationHooks();
        previewPageKeepsDiagnosticsAndSensitiveDataOutOfMarkup();
        previewPageExposesClickBudgetFailureForQa();
        previewAutomationAuditPassesForDefaultScenarios();
        previewAutomationAuditMarkdownIsCiFriendlyAndPrivacySafe();
        previewAutomationAuditWriterCreatesCiArtifact();
        previewVisualBaselineManifestIsDeterministicAndPrivacySafe();
        previewVisualBaselineWriterCreatesCiArtifact();
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

    private static void previewAutomationAuditPassesForDefaultScenarios() {
        PreviewAutomationAudit.Report report = PreviewAutomationAudit.auditDefaultScenarios();

        assertTrue(report.passed(), "default preview automation audit should pass");
        assertEquals(9, report.scenarioCount(), "default scenario count");
        assertEquals(0, report.missingRequiredStates().size(), "required states missing count");
        assertEquals(0, report.privacyViolations().size(), "privacy violation count");
        assertContains(report.renderMarkdown(), "requiredStates=PASS", "audit required states markdown");
        assertContains(report.renderMarkdown(), "privacy=PASS", "audit privacy markdown");
    }

    private static void previewAutomationAuditMarkdownIsCiFriendlyAndPrivacySafe() {
        String markdown = PreviewAutomationAudit.auditDefaultScenarios().renderMarkdown();

        assertContains(markdown, "# Talk screen preview automation audit", "audit title");
        assertContains(markdown, "automationHooks=PASS", "audit hook status");
        assertContains(markdown, "clickBudget=PASS", "audit click budget status");
        assertFalse(markdown.toLowerCase().contains("aa:bb"), "audit markdown must not expose MAC addresses");
        assertFalse(markdown.toLowerCase().contains("audio payload"), "audit markdown must not expose raw audio payload text");
    }

    private static void previewAutomationAuditWriterCreatesCiArtifact() {
        try {
            Path output = Files.createTempFile("walkitalki-preview-audit", ".md");
            PreviewAutomationAuditWriter.main(new String[] {output.toString()});
            String markdown = Files.readString(output);

            assertContains(markdown, "# Talk screen preview automation audit", "preview audit artifact title");
            assertContains(markdown, "result=PASS", "preview audit artifact result");
            assertContains(markdown, "scenarioCount=9", "preview audit artifact scenario count");
        } catch (IOException exception) {
            throw new AssertionError("preview automation audit writer should create an artifact", exception);
        }
    }

    private static void previewVisualBaselineManifestIsDeterministicAndPrivacySafe() {
        PreviewVisualBaseline.Manifest manifest = PreviewVisualBaseline.captureDefaultScenarios();
        String markdown = manifest.renderMarkdown();

        assertEquals(9, manifest.scenarioCount(), "visual baseline scenario count");
        assertEquals(64, manifest.sha256().length(), "visual baseline sha length");
        assertContains(markdown, "# Talk screen visual baseline", "visual baseline title");
        assertContains(markdown, "previewSchema=talk-screen-v1", "visual baseline schema");
        assertContains(markdown, "audit=PASS", "visual baseline audit status");
        assertContains(markdown, "scenarioIds=permission-blocked,scanning,connecting,ready-to-talk,transmitting,receiving,busy,bluetooth-blocked,disconnected-over-budget", "visual baseline scenario ids");
        assertFalse(markdown.contains("AA:BB:CC:DD:EE:FF"), "visual baseline must not include raw MAC addresses");
        assertFalse(markdown.contains("Alice Personal Phone"), "visual baseline must not include device names");
        assertFalse(markdown.contains("11, 12, 13"), "visual baseline must not include audio payload bytes");
    }

    private static void previewVisualBaselineWriterCreatesCiArtifact() {
        try {
            Path output = Files.createTempFile("walkitalki-preview-visual-baseline", ".md");
            PreviewVisualBaselineWriter.main(new String[] {output.toString()});
            String markdown = Files.readString(output);

            assertContains(markdown, "# Talk screen visual baseline", "visual baseline artifact title");
            assertContains(markdown, "audit=PASS", "visual baseline artifact audit");
            assertContains(markdown, "scenarioCount=9", "visual baseline artifact scenario count");
        } catch (IOException exception) {
            throw new AssertionError("preview visual baseline writer should create an artifact", exception);
        }
    }

    private static void assertContains(String actual, String expectedPart, String label) {
        if (!actual.contains(expectedPart)) {
            throw new AssertionError(label + ": expected to contain " + expectedPart + " in " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but was " + actual);
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void assertFalse(boolean condition, String label) {
        if (condition) {
            throw new AssertionError(label);
        }
    }
}
