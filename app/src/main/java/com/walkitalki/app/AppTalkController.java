package com.walkitalki.app;

import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.ui.TalkScreenPresenter;
import com.walkitalki.core.ui.TalkScreenState;

public final class AppTalkController {
    private PttState pttState;

    private AppTalkController(PttState initialState) {
        this.pttState = initialState;
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
        } else if (pttState == PttState.SCANNING) {
            pttState = PttState.CONNECTED;
        }
    }

    public void pressPushToTalk() {
        if (pttState == PttState.CONNECTED) {
            pttState = PttState.TRANSMITTING;
        }
    }

    public void releasePushToTalk() {
        if (pttState == PttState.TRANSMITTING) {
            pttState = PttState.CONNECTED;
        }
    }
}
