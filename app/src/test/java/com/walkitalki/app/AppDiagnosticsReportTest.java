package com.walkitalki.app;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppDiagnosticsReportTest {
    @Test
    public void supportReportCapturesMvpFlowWithoutSensitiveIdentifiers() {
        AppTalkController controller = AppTalkController.createMvpDemoController();
        controller.advanceSetupStep();
        controller.advanceSetupStep();
        controller.pressPushToTalk();
        controller.releasePushToTalk();

        String report = controller.supportReport().renderMarkdown();

        assertTrue(report.contains("# walkiTalki MVP diagnostics"));
        assertTrue(report.contains("ui_idle"));
        assertTrue(report.contains("ui_scanning"));
        assertTrue(report.contains("ui_ready:ptt_enabled"));
        assertTrue(report.contains("ui_transmitting"));
        assertTrue(report.contains("redaction=enabled"));
        assertFalse(report.toLowerCase().contains("mac"));
        assertFalse(report.toLowerCase().contains("raw peer"));
        assertFalse(report.toLowerCase().contains("audio payload"));
    }
}
