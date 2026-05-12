package com.walkitalki.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppTalkControllerTest {
    @Test
    public void startsReadyAndDoesNotExposeSensitiveDiagnostics() {
        AppTalkController controller = AppTalkController.createReadyController();

        AppUiCopy copy = controller.currentCopy();

        assertEquals("Готов к локальной PTT-связи", copy.status());
        assertEquals("PTT", copy.buttonLabel());
        assertFalse(copy.transmitting());
        assertTrue(copy.primaryAction().contains("Нажмите и удерживайте"));
        assertTrue(copy.diagnostics().contains("Диагностика без MAC"));
    }

    @Test
    public void pressAndReleasePushToTalkUpdatesVisibleState() {
        AppTalkController controller = AppTalkController.createReadyController();

        controller.pressPushToTalk();
        AppUiCopy talking = controller.currentCopy();

        assertEquals("Передача голоса активна", talking.status());
        assertEquals("TALK", talking.buttonLabel());
        assertTrue(talking.transmitting());
        assertTrue(talking.primaryAction().contains("Отпустите"));
        assertTrue(talking.diagnostics().contains("ui_transmitting"));

        controller.releasePushToTalk();
        AppUiCopy released = controller.currentCopy();

        assertEquals("Готов к локальной PTT-связи", released.status());
        assertEquals("PTT", released.buttonLabel());
        assertFalse(released.transmitting());
    }
}
