package com.walkitalki.app;

import java.util.List;

public final class AppScreenshotCatalog {
    private AppScreenshotCatalog() {
    }

    public static List<AppUiCopy> states() {
        AppTalkController controller = AppTalkController.createMvpDemoController();
        AppUiCopy idle = controller.currentCopy();
        controller.advanceSetupStep();
        AppUiCopy scanning = controller.currentCopy();
        controller.advanceSetupStep();
        AppUiCopy ready = controller.currentCopy();
        controller.pressPushToTalk();
        AppUiCopy transmitting = controller.currentCopy();
        return List.of(idle, scanning, ready, transmitting);
    }
}
