package com.walkitalki.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppDebugHarnessTest {
    @Test
    public void mvpSmokeTestExercisesScanConnectTalkAndReleaseWithRedactedReport() {
        AppDebugHarness harness = AppDebugHarness.createMvpHarness();

        AppDebugReport report = harness.runMvpSmokeTest();

        assertTrue(report.passed());
        assertEquals(5, report.steps().size());
        assertEquals("launch", report.steps().get(0).action());
        assertEquals("SCAN", report.steps().get(0).buttonLabel());
        assertEquals("tap_scan", report.steps().get(1).action());
        assertEquals("...", report.steps().get(1).buttonLabel());
        assertEquals("tap_fake_peer", report.steps().get(2).action());
        assertEquals("PTT", report.steps().get(2).buttonLabel());
        assertEquals("hold_ptt", report.steps().get(3).action());
        assertEquals("TALK", report.steps().get(3).buttonLabel());
        assertEquals("release_ptt", report.steps().get(4).action());
        assertEquals("PTT", report.steps().get(4).buttonLabel());

        String markdown = report.renderMarkdown();
        assertTrue(markdown.contains("# walkiTalki debug smoke test"));
        assertTrue(markdown.contains("result=PASS"));
        assertTrue(markdown.contains("ui_idle"));
        assertTrue(markdown.contains("ui_scanning"));
        assertTrue(markdown.contains("ui_transmitting"));
        assertFalse(markdown.toLowerCase().contains("mac"));
        assertFalse(markdown.toLowerCase().contains("raw peer"));
        assertFalse(markdown.toLowerCase().contains("audio payload"));
    }
}
