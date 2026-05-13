package com.walkitalki.app;

public final class AppTalkViewModel {
    private final AppTalkController controller;
    private boolean started;

    private AppTalkViewModel(AppTalkController controller) {
        this.controller = controller;
    }

    public static AppTalkViewModel createReadyModel() {
        return new AppTalkViewModel(AppTalkController.createReadyController());
    }

    public static AppTalkViewModel createMvpDemoModel() {
        return new AppTalkViewModel(AppTalkController.createMvpDemoController());
    }

    public static AppTalkViewModel fromController(AppTalkController controller) {
        return new AppTalkViewModel(controller);
    }

    public AppUiCopy state() {
        return controller.currentCopy();
    }

    public void onStart() {
        if (!started) {
            started = true;
            controller.recordLifecycleStart();
        }
    }

    public void onStop() {
        if (started) {
            started = false;
            controller.recordLifecycleStop();
        }
    }

    public void onIntent(AppTalkUiIntent intent) {
        switch (intent) {
            case PRIMARY_ACTION -> controller.advanceSetupStep();
            case PUSH_TO_TALK_DOWN -> {
                if (state().pushToTalkEnabled()) {
                    controller.pressPushToTalk();
                }
            }
            case PUSH_TO_TALK_UP -> controller.releasePushToTalk();
        }
    }

    public AppDiagnosticsReport supportReport() {
        return controller.supportReport();
    }
}
