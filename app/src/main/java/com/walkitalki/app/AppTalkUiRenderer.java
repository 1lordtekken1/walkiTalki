package com.walkitalki.app;

import com.walkitalki.core.ui.TalkScreenState;

public final class AppTalkUiRenderer {
    public static final int MINIMUM_TOUCH_TARGET_DP = 56;

    private AppTalkUiRenderer() {
    }

    public static AppTalkRenderModel render(AppTalkViewModel viewModel) {
        return render(viewModel.state());
    }

    public static AppTalkRenderModel render(AppUiCopy copy) {
        AppTalkUiIntent pressIntent = copy.pushToTalkEnabled()
                ? AppTalkUiIntent.PUSH_TO_TALK_DOWN
                : AppTalkUiIntent.PRIMARY_ACTION;
        String contentDescription = copy.title()
                + ": "
                + copy.status()
                + ". "
                + copy.primaryAction()
                + ". "
                + copy.buttonLabel();
        return new AppTalkRenderModel(
                copy.title(),
                copy.status(),
                copy.primaryAction(),
                copy.buttonLabel(),
                copy.transmitting(),
                copy.pushToTalkEnabled(),
                copy.diagnosticsSignal(),
                contentDescription,
                MINIMUM_TOUCH_TARGET_DP,
                pressIntent,
                AppTalkUiIntent.PUSH_TO_TALK_UP);
    }

    public static AppTalkRenderModel render(TalkScreenState state) {
        boolean pttEnabled = state.pushToTalkEnabled() && state.withinClickBudget();
        boolean transmitting = "ui_transmitting".equals(state.diagnosticsSignal());
        AppTalkUiIntent pressIntent = pttEnabled
                ? AppTalkUiIntent.PUSH_TO_TALK_DOWN
                : AppTalkUiIntent.PRIMARY_ACTION;
        String status = state.withinClickBudget()
                ? state.statusTitle()
                : "Action budget exceeded";
        String contentDescription = "walkiTalki: "
                + status
                + ". "
                + state.primaryActionLabel()
                + ". "
                + state.diagnosticsSignal()
                + ". "
                + state.acceptanceCriteria()
                + ". "
                + state.rollbackTrigger();
        return new AppTalkRenderModel(
                "walkiTalki",
                status,
                state.primaryActionLabel(),
                state.primaryActionLabel(),
                transmitting,
                pttEnabled,
                state.diagnosticsSignal(),
                contentDescription,
                MINIMUM_TOUCH_TARGET_DP,
                pressIntent,
                AppTalkUiIntent.PUSH_TO_TALK_UP);
    }
}
