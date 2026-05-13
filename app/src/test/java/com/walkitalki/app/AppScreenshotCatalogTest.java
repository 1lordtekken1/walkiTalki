package com.walkitalki.app;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppScreenshotCatalogTest {
    @Test
    public void screenshotCatalogUsesTheSameMvpDemoCopyAsTheAppController() {
        List<AppUiCopy> states = AppScreenshotCatalog.states();

        assertEquals(4, states.size());
        assertEquals("SCAN", states.get(0).buttonLabel());
        assertEquals("...", states.get(1).buttonLabel());
        assertEquals("PTT", states.get(2).buttonLabel());
        assertEquals("TALK", states.get(3).buttonLabel());
        assertFalse(states.get(0).pushToTalkEnabled());
        assertFalse(states.get(1).pushToTalkEnabled());
        assertTrue(states.get(2).pushToTalkEnabled());
        assertTrue(states.get(3).transmitting());
        assertTrue(states.get(0).diagnostics().contains("ui_idle"));
        assertTrue(states.get(1).diagnostics().contains("ui_scanning"));
        assertTrue(states.get(2).diagnostics().contains("ui_ready:ptt_enabled"));
        assertTrue(states.get(3).diagnostics().contains("ui_transmitting"));
    }
}
