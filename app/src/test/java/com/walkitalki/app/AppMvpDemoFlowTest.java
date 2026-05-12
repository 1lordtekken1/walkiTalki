package com.walkitalki.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppMvpDemoFlowTest {
    @Test
    public void mvpDemoWalksFromScanToConnectedToTalkAndBack() {
        AppTalkController controller = AppTalkController.createMvpDemoController();

        AppUiCopy idle = controller.currentCopy();
        assertEquals("Поиск собеседника", idle.status());
        assertEquals("SCAN", idle.buttonLabel());
        assertFalse(idle.pushToTalkEnabled());

        controller.advanceSetupStep();
        AppUiCopy scanning = controller.currentCopy();
        assertEquals("Ищем ближайшие устройства", scanning.status());
        assertEquals("...", scanning.buttonLabel());
        assertFalse(scanning.pushToTalkEnabled());

        controller.advanceSetupStep();
        AppUiCopy connected = controller.currentCopy();
        assertEquals("Готов к локальной PTT-связи", connected.status());
        assertEquals("PTT", connected.buttonLabel());
        assertTrue(connected.pushToTalkEnabled());

        controller.pressPushToTalk();
        AppUiCopy talking = controller.currentCopy();
        assertEquals("Передача голоса активна", talking.status());
        assertEquals("TALK", talking.buttonLabel());
        assertTrue(talking.transmitting());

        controller.releasePushToTalk();
        AppUiCopy listening = controller.currentCopy();
        assertEquals("Готов к локальной PTT-связи", listening.status());
        assertEquals("PTT", listening.buttonLabel());
        assertFalse(listening.transmitting());
    }
}
