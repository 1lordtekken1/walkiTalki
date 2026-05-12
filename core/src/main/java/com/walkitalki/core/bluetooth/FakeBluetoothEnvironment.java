package com.walkitalki.core.bluetooth;

import java.util.HashSet;
import java.util.Set;

public final class FakeBluetoothEnvironment implements BluetoothEnvironment {
    private boolean bluetoothEnabled = true;
    private boolean nearbyPermission = true;
    private final Set<String> pairedPeers = new HashSet<>();

    private FakeBluetoothEnvironment() {
    }

    public static FakeBluetoothEnvironment create() {
        return new FakeBluetoothEnvironment();
    }

    public FakeBluetoothEnvironment withBluetoothEnabled(boolean enabled) {
        bluetoothEnabled = enabled;
        return this;
    }

    public FakeBluetoothEnvironment withNearbyPermission(boolean granted) {
        nearbyPermission = granted;
        return this;
    }

    public FakeBluetoothEnvironment withPairedPeer(String peerId) {
        pairedPeers.add(peerId);
        return this;
    }

    @Override
    public BluetoothEnvironmentState state() {
        if (!bluetoothEnabled) {
            return BluetoothEnvironmentState.BLUETOOTH_OFF;
        }
        if (!nearbyPermission) {
            return BluetoothEnvironmentState.MISSING_PERMISSION;
        }
        return BluetoothEnvironmentState.READY;
    }

    @Override
    public boolean isPaired(String peerId) {
        return pairedPeers.contains(peerId);
    }
}
