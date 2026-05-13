package com.walkitalki.app;

import java.util.ArrayList;
import java.util.List;

public final class AppDebugHarness {
    private final AppTalkController controller;

    private AppDebugHarness(AppTalkController controller) {
        this.controller = controller;
    }

    public static AppDebugHarness createMvpHarness() {
        return new AppDebugHarness(AppTalkController.createMvpDemoController());
    }

    public AppDebugReport runMvpSmokeTest() {
        List<AppDebugStep> steps = new ArrayList<>();
        steps.add(AppDebugStep.capture("launch", controller.currentCopy()));

        controller.advanceSetupStep();
        steps.add(AppDebugStep.capture("tap_scan", controller.currentCopy()));

        controller.advanceSetupStep();
        steps.add(AppDebugStep.capture("tap_fake_peer", controller.currentCopy()));

        controller.pressPushToTalk();
        steps.add(AppDebugStep.capture("hold_ptt", controller.currentCopy()));

        controller.releasePushToTalk();
        steps.add(AppDebugStep.capture("release_ptt", controller.currentCopy()));

        return new AppDebugReport(steps, controller.supportReport());
    }
}
