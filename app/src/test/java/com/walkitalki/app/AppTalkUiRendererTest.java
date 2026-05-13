package com.walkitalki.app;

import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.ui.TalkScreenPresenter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AppTalkUiRendererTest {
    @Test
    public void renderModelExposesComposeReadyActionsAndAccessibilityForScan() {
        AppTalkViewModel viewModel = AppTalkViewModel.createMvpDemoModel();

        AppTalkRenderModel model = AppTalkUiRenderer.render(viewModel);

        assertEquals("SCAN", model.buttonLabel());
        assertEquals(AppTalkUiIntent.PRIMARY_ACTION, model.pressIntent());
        assertEquals(AppTalkUiIntent.PUSH_TO_TALK_UP, model.releaseIntent());
        assertFalse(model.pushToTalkEnabled());
        assertEquals("ui_idle", model.diagnosticsSignal());
        assertTrue(model.minimumTouchTargetDp() >= 56);
        assertTrue(model.contentDescription().contains("Поиск собеседника"));
        assertTrue(model.contentDescription().contains("Нажмите, чтобы начать поиск"));
    }

    @Test
    public void renderModelSwitchesToPttDownWhenPresenterEnablesTalk() {
        AppTalkViewModel viewModel = AppTalkViewModel.createMvpDemoModel();
        viewModel.onIntent(AppTalkUiIntent.PRIMARY_ACTION);
        viewModel.onIntent(AppTalkUiIntent.PRIMARY_ACTION);

        AppTalkRenderModel model = AppTalkUiRenderer.render(viewModel);

        assertEquals("PTT", model.buttonLabel());
        assertEquals(AppTalkUiIntent.PUSH_TO_TALK_DOWN, model.pressIntent());
        assertEquals(AppTalkUiIntent.PUSH_TO_TALK_UP, model.releaseIntent());
        assertTrue(model.pushToTalkEnabled());
        assertFalse(model.transmitting());
        assertEquals("ui_ready:ptt_enabled", model.diagnosticsSignal());
    }

    @Test
    public void renderModelKeepsDiagnosticsCoarseAndPrivacySafeWhileTalking() {
        AppTalkViewModel viewModel = AppTalkViewModel.createReadyModel();
        viewModel.onIntent(AppTalkUiIntent.PUSH_TO_TALK_DOWN);

        AppTalkRenderModel model = AppTalkUiRenderer.render(viewModel);
        String exported = model.contentDescription().toLowerCase() + " " + model.diagnosticsSignal().toLowerCase();

        assertEquals("TALK", model.buttonLabel());
        assertTrue(model.transmitting());
        assertEquals("ui_transmitting", model.diagnosticsSignal());
        assertFalse(exported.contains("mac"));
        assertFalse(exported.contains("raw peer"));
        assertFalse(exported.contains("audio payload"));
    }

    @Test
    public void renderModelMatchesPresenterForRoadmapAcceptanceStates() {
        assertPresenterStateRenders(
                PermissionAction.REQUEST_NEARBY_DEVICES,
                PttState.IDLE,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                0,
                3,
                "Enable Nearby devices",
                "Grant Nearby devices",
                AppTalkUiIntent.PRIMARY_ACTION,
                false);
        assertPresenterStateRenders(
                PermissionAction.ALLOW,
                PttState.CONNECTED,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                1,
                3,
                "Ready. Hold to talk.",
                "Hold to talk",
                AppTalkUiIntent.PUSH_TO_TALK_DOWN,
                false);
        assertPresenterStateRenders(
                PermissionAction.ALLOW,
                PttState.TRANSMITTING,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                2,
                3,
                "Talking…",
                "Release to listen",
                AppTalkUiIntent.PUSH_TO_TALK_DOWN,
                true);
        assertPresenterStateRenders(
                PermissionAction.ALLOW,
                PttState.RECEIVING,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                2,
                3,
                "Peer is talking",
                "Listen",
                AppTalkUiIntent.PRIMARY_ACTION,
                false);
        assertPresenterStateRenders(
                PermissionAction.ALLOW,
                PttState.BUSY,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                2,
                3,
                "Peer is talking",
                "Listen",
                AppTalkUiIntent.PRIMARY_ACTION,
                false);
        assertPresenterStateRenders(
                PermissionAction.ALLOW,
                PttState.CONNECTED,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.DISCONNECTED,
                2,
                3,
                "Disconnected",
                "Reconnect",
                AppTalkUiIntent.PRIMARY_ACTION,
                false);

        AppTalkRenderModel overBudget = AppTalkUiRenderer.render(TalkScreenPresenter.render(
                PttState.CONNECTED,
                PermissionAction.ALLOW,
                BluetoothEnvironmentState.READY,
                ConnectionHealth.STABLE,
                4,
                3));

        assertEquals("Action budget exceeded", overBudget.status());
        assertEquals(AppTalkUiIntent.PRIMARY_ACTION, overBudget.pressIntent());
        assertTrue(overBudget.contentDescription().contains("Rollback if connect-to-talk needs more than 3 actions"));
    }

    private static void assertPresenterStateRenders(
            PermissionAction permissionAction,
            PttState pttState,
            BluetoothEnvironmentState bluetoothState,
            ConnectionHealth connectionHealth,
            int userActions,
            int clickBudget,
            String expectedStatus,
            String expectedAction,
            AppTalkUiIntent expectedPressIntent,
            boolean expectedTransmitting) {
        AppTalkRenderModel model = AppTalkUiRenderer.render(TalkScreenPresenter.render(
                pttState,
                permissionAction,
                bluetoothState,
                connectionHealth,
                userActions,
                clickBudget));

        assertEquals(expectedStatus, model.status());
        assertEquals(expectedAction, model.primaryAction());
        assertEquals(expectedAction, model.buttonLabel());
        assertEquals(expectedPressIntent, model.pressIntent());
        assertEquals(expectedTransmitting, model.transmitting());
        assertTrue(model.contentDescription().contains(model.diagnosticsSignal()));
        assertFalse(model.contentDescription().toLowerCase().contains("bluetoothsocket"));
        assertFalse(model.contentDescription().toLowerCase().contains("audiorecord"));
    }

}
