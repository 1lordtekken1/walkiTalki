package com.walkitalki.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class MainActivityContractTest {
    @Test
    public void appUiCopyShowsInstallableWalkieTalkieStatus() {
        AppUiCopy copy = AppUiCopy.defaultCopy();

        assertEquals("walkiTalki", copy.title());
        assertEquals("Готов к локальной PTT-связи", copy.status());
        assertTrue(copy.primaryAction().contains("Нажмите и удерживайте"));
        assertTrue(copy.diagnostics().contains("Диагностика без MAC"));
    }
}
