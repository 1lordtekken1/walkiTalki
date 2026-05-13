package com.walkitalki.app;

import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.permissions.UseCase;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AndroidPermissionEnvironmentAdapterTest {
    @Test
    public void android12ScanUsesNearbyDevicesPermissionsWithoutLeakingManifestNames() {
        AndroidPermissionEnvironmentAdapter adapter = new AndroidPermissionEnvironmentAdapter(
                35,
                granted(AndroidPermissionManifestNames.BLUETOOTH_SCAN),
                true);

        assertEquals(PermissionAction.REQUEST_NEARBY_DEVICES, adapter.decisionFor(UseCase.SCAN_FOR_PEERS).action());
        assertEquals(BluetoothEnvironmentState.MISSING_PERMISSION, adapter.bluetoothStateFor(UseCase.SCAN_FOR_PEERS));
        assertTrue(adapter.decisionFor(UseCase.SCAN_FOR_PEERS).requiredPermissions().contains(com.walkitalki.core.permissions.AndroidPermission.BLUETOOTH_CONNECT));
        assertFalse(adapter.decisionFor(UseCase.SCAN_FOR_PEERS).requiredPermissions().contains(com.walkitalki.core.permissions.AndroidPermission.ACCESS_FINE_LOCATION));
        assertEquals("permission:nearby_devices_missing", adapter.diagnosticsSignalFor(UseCase.SCAN_FOR_PEERS));
        assertFalse(adapter.diagnosticsSignalFor(UseCase.SCAN_FOR_PEERS).contains("BLUETOOTH_CONNECT"));
    }

    @Test
    public void legacyScanUsesLocationGateAndReportsReadyWhenBluetoothIsOn() {
        AndroidPermissionEnvironmentAdapter adapter = new AndroidPermissionEnvironmentAdapter(
                30,
                granted(
                        AndroidPermissionManifestNames.BLUETOOTH,
                        AndroidPermissionManifestNames.BLUETOOTH_ADMIN,
                        AndroidPermissionManifestNames.ACCESS_FINE_LOCATION),
                true);

        assertEquals(PermissionAction.ALLOW, adapter.decisionFor(UseCase.SCAN_FOR_PEERS).action());
        assertEquals(BluetoothEnvironmentState.READY, adapter.bluetoothStateFor(UseCase.SCAN_FOR_PEERS));
        assertEquals("permission:ready", adapter.diagnosticsSignalFor(UseCase.SCAN_FOR_PEERS));
    }

    @Test
    public void bluetoothOffTakesPriorityAfterPermissionCollection() {
        AndroidPermissionEnvironmentAdapter adapter = new AndroidPermissionEnvironmentAdapter(
                35,
                granted(
                        AndroidPermissionManifestNames.BLUETOOTH_SCAN,
                        AndroidPermissionManifestNames.BLUETOOTH_CONNECT),
                false);

        assertEquals(PermissionAction.ALLOW, adapter.decisionFor(UseCase.SCAN_FOR_PEERS).action());
        assertEquals(BluetoothEnvironmentState.BLUETOOTH_OFF, adapter.bluetoothStateFor(UseCase.SCAN_FOR_PEERS));
        assertEquals("bluetooth:off", adapter.diagnosticsSignalFor(UseCase.SCAN_FOR_PEERS));
    }

    @Test
    public void transmitAudioReportsMicrophoneGateSeparatelyFromNearbyDevices() {
        AndroidPermissionEnvironmentAdapter adapter = new AndroidPermissionEnvironmentAdapter(
                35,
                granted(AndroidPermissionManifestNames.BLUETOOTH_CONNECT),
                true);

        assertEquals(PermissionAction.REQUEST_MICROPHONE, adapter.decisionFor(UseCase.TRANSMIT_AUDIO).action());
        assertEquals(BluetoothEnvironmentState.MISSING_PERMISSION, adapter.bluetoothStateFor(UseCase.TRANSMIT_AUDIO));
        assertEquals("permission:microphone_missing", adapter.diagnosticsSignalFor(UseCase.TRANSMIT_AUDIO));
    }

    private static AndroidPermissionEnvironmentAdapter.PermissionChecker granted(String... permissions) {
        Set<String> granted = new HashSet<>(Set.of(permissions));
        return granted::contains;
    }
}
