package com.walkitalki.app;

import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.ui.TalkScreenPresenter;
import com.walkitalki.core.ui.TalkScreenState;
import java.util.ArrayList;
import java.util.List;

public final class AppTalkController {
    private PttState pttState;
    private final List<String> timelineSignals = new ArrayList<>();

    private AppTalkController(PttState initialState) {
        this.pttState = initialState;
        recordCurrentSignal();
    }

    public static AppTalkController createReadyController() {
        return new AppTalkController(PttState.CONNECTED);
    }

    public static AppTalkController createMvpDemoController() {
        return new AppTalkController(PttState.IDLE);
    }

    public AppUiCopy currentCopy() {
        TalkScreenState screenState = TalkScreenPresenter.render(
                pttState,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                1,
                3);
        return switch (pttState) {
            case IDLE -> AppUiCopy.idle(screenState.diagnosticsSignal());
            case SCANNING -> AppUiCopy.scanning(screenState.diagnosticsSignal());
            case TRANSMITTING -> AppUiCopy.transmitting(screenState.diagnosticsSignal());
            default -> AppUiCopy.ready(screenState.diagnosticsSignal());
        };
    }

    public void advanceSetupStep() {
        if (pttState == PttState.IDLE) {
            pttState = PttState.SCANNING;
            recordCurrentSignal();
        } else if (pttState == PttState.SCANNING) {
            pttState = PttState.CONNECTED;
            recordCurrentSignal();
        }
    }

    public void pressPushToTalk() {
        if (pttState == PttState.CONNECTED) {
            pttState = PttState.TRANSMITTING;
            recordCurrentSignal();
        }
    }

    public void releasePushToTalk() {
        if (pttState == PttState.TRANSMITTING) {
            pttState = PttState.CONNECTED;
            recordCurrentSignal();
        }
    }

    public void recordLifecycleStart() {
        recordSignal("ui_lifecycle:start:" + currentCopy().diagnosticsSignal());
    }

    public void recordLifecycleStop() {
        recordSignal("ui_lifecycle:stop:" + currentCopy().diagnosticsSignal());
    }

    public AppDiagnosticsReport supportReport() {
        return new AppDiagnosticsReport(timelineSignals, currentCopy());
    }

    private void recordCurrentSignal() {
        recordSignal(currentCopy().diagnosticsSignal());
    }

    private void recordSignal(String signal) {
        if (timelineSignals.isEmpty() || !timelineSignals.get(timelineSignals.size() - 1).equals(signal)) {
            timelineSignals.add(signal);
        }
    }
}
