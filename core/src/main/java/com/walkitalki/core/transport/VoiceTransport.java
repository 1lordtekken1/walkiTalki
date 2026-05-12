package com.walkitalki.core.transport;

import com.walkitalki.core.audio.AudioFrame;
import com.walkitalki.core.bluetooth.BluetoothEnvironment;

public interface VoiceTransport {
    VoiceTransport sendPing(long sequenceNumber, long nowMillis);

    VoiceTransport receivePong(long sequenceNumber, long nowMillis);

    VoiceTransport discoverPairedPeer(String peerId, BluetoothEnvironment environment, long nowMillis);

    VoiceTransport sendAudioFrame(AudioFrame frame);

    VoiceTransport receiveAudioFrame(AudioFrame frame);

    VoiceTransport disconnect(String reason);

    TransportSessionResult snapshotAt(long nowMillis);
}
