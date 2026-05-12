package com.walkitalki.core.bluetooth;

public interface BluetoothEnvironment {
    BluetoothEnvironmentState state();

    boolean isPaired(String peerId);
}
