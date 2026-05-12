package com.walkitalki.core.session;

import com.walkitalki.core.audio.PttAudioSnapshot;
import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.transport.PeerDiscoverySnapshot;
import com.walkitalki.core.transport.TransportSessionResult;
import com.walkitalki.core.ui.TalkScreenState;

public record WalkieTalkieSessionSnapshot(
    PttState pttState,
    PermissionAction permissionAction,
    BluetoothEnvironmentState bluetoothState,
    ConnectionHealth connectionHealth,
    int userActions,
    PeerDiscoverySnapshot discovery,
    TransportSessionResult transport,
    PttAudioSnapshot audio,
    TalkScreenState screen,
    String diagnostics
) {
}
