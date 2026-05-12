package com.walkitalki.core.ui;

import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.permissions.PermissionAction;

public final class TalkScreenPresenter {
    private TalkScreenPresenter() {
    }

    public static TalkScreenState render(
        PttState pttState,
        PermissionAction permissionAction,
        BluetoothEnvironmentState bluetoothState,
        ConnectionHealth connectionHealth,
        int userActions,
        int clickBudget
    ) {
        boolean withinClickBudget = userActions <= clickBudget;
        String rollbackTrigger = "Rollback if connect-to-talk needs more than " + clickBudget + " actions or hides the blocking reason.";
        String acceptanceCriteria = "User can identify permission, Bluetooth, connection, and Push-To-Talk state without Android framework calls.";

        if (permissionAction != PermissionAction.ALLOW) {
            return blockedByPermission(permissionAction, withinClickBudget, acceptanceCriteria, rollbackTrigger);
        }
        if (bluetoothState != BluetoothEnvironmentState.READY) {
            return blockedByBluetooth(bluetoothState, withinClickBudget, acceptanceCriteria, rollbackTrigger);
        }
        if (connectionHealth == ConnectionHealth.DISCONNECTED || pttState == PttState.DISCONNECTED) {
            return new TalkScreenState(
                "Disconnected",
                "Reconnect",
                false,
                true,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_reconnect:" + connectionHealth
            );
        }
        if (connectionHealth == ConnectionHealth.DEGRADED) {
            return new TalkScreenState(
                "Connection unstable",
                "Keep listening",
                false,
                true,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_degraded:" + pttState
            );
        }

        return switch (pttState) {
            case CONNECTED -> new TalkScreenState(
                "Ready. Hold to talk.",
                "Hold to talk",
                true,
                false,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_ready:ptt_enabled"
            );
            case TRANSMITTING -> new TalkScreenState(
                "Talking…",
                "Release to listen",
                true,
                false,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_transmitting"
            );
            case RECEIVING, BUSY -> new TalkScreenState(
                "Peer is talking",
                "Listen",
                false,
                false,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_receiving:" + pttState
            );
            case SCANNING -> new TalkScreenState(
                "Scanning for nearby devices",
                "Scanning…",
                false,
                false,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_scanning"
            );
            case CONNECTING, PAIRING_REQUIRED, PAIRING_REJECTED, PAIRING_TIMED_OUT -> new TalkScreenState(
                "Connecting",
                "Wait for peer",
                false,
                true,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_connecting:" + pttState
            );
            case IDLE -> new TalkScreenState(
                "Find a nearby phone",
                "Scan",
                false,
                false,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_idle"
            );
            case DISCONNECTED -> new TalkScreenState(
                "Disconnected",
                "Reconnect",
                false,
                true,
                withinClickBudget,
                acceptanceCriteria,
                rollbackTrigger,
                "ui_reconnect:" + connectionHealth
            );
        };
    }

    private static TalkScreenState blockedByPermission(
        PermissionAction permissionAction,
        boolean withinClickBudget,
        String acceptanceCriteria,
        String rollbackTrigger
    ) {
        String title = switch (permissionAction) {
            case REQUEST_NEARBY_DEVICES -> "Enable Nearby devices";
            case REQUEST_LOCATION_FOR_LEGACY_DISCOVERY -> "Enable Location for Bluetooth discovery";
            case REQUEST_MICROPHONE -> "Enable Microphone to talk";
            case ALLOW -> "Ready";
        };
        String action = switch (permissionAction) {
            case REQUEST_NEARBY_DEVICES -> "Grant Nearby devices";
            case REQUEST_LOCATION_FOR_LEGACY_DISCOVERY -> "Grant Location";
            case REQUEST_MICROPHONE -> "Grant Microphone";
            case ALLOW -> "Continue";
        };
        return new TalkScreenState(
            title,
            action,
            false,
            true,
            withinClickBudget,
            acceptanceCriteria,
            rollbackTrigger,
            "ui_blocked:" + permissionAction
        );
    }

    private static TalkScreenState blockedByBluetooth(
        BluetoothEnvironmentState bluetoothState,
        boolean withinClickBudget,
        String acceptanceCriteria,
        String rollbackTrigger
    ) {
        String title = switch (bluetoothState) {
            case BLUETOOTH_OFF -> "Turn on Bluetooth";
            case MISSING_PERMISSION -> "Enable Bluetooth permission";
            case READY -> "Bluetooth ready";
        };
        String action = switch (bluetoothState) {
            case BLUETOOTH_OFF -> "Open Bluetooth settings";
            case MISSING_PERMISSION -> "Grant Bluetooth permission";
            case READY -> "Continue";
        };
        return new TalkScreenState(
            title,
            action,
            false,
            true,
            withinClickBudget,
            acceptanceCriteria,
            rollbackTrigger,
            "ui_blocked:" + bluetoothState
        );
    }
}
