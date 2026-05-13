package com.walkitalki.core.transport;

import com.walkitalki.core.modes.OperationModePolicy;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class ClassicBluetoothStreamAdapter {
    private final StreamVoiceTransport transport;
    private final String peerHash;
    private final long connectedAtMillis;

    private ClassicBluetoothStreamAdapter(
        StreamVoiceTransport transport,
        String peerHash,
        long connectedAtMillis
    ) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.peerHash = Objects.requireNonNull(peerHash, "peerHash");
        this.connectedAtMillis = connectedAtMillis;
    }

    public static ClassicBluetoothStreamAdapter connected(
        InputStream inputStream,
        OutputStream outputStream,
        long connectedAtMillis,
        OperationModePolicy policy,
        String rawPeerIdentifier
    ) {
        return new ClassicBluetoothStreamAdapter(
            StreamVoiceTransport.connected(inputStream, outputStream, connectedAtMillis, policy),
            peerHash(rawPeerIdentifier),
            connectedAtMillis
        );
    }

    public VoiceTransport transport() {
        return transport;
    }

    public TransportSessionResult snapshotAt(long nowMillis) {
        TransportSessionResult snapshot = transport.snapshotAt(nowMillis);
        return new TransportSessionResult(
            snapshot.health(),
            snapshot.framesSent(),
            snapshot.framesReceived(),
            snapshot.sendFailures(),
            snapshot.missedHeartbeatWindows(),
            snapshot.peerDiscovered(),
            snapshot.audioFramesSent(),
            snapshot.audioFramesReceived(),
            diagnosticsSignal() + '\n' + snapshot.timeline()
        );
    }

    public String diagnosticsSignal() {
        return "classic_stream:connected peerHash=" + peerHash + " connectedAt=" + connectedAtMillis;
    }

    private static String peerHash(String rawPeerIdentifier) {
        String value = rawPeerIdentifier == null ? "unknown" : rawPeerIdentifier;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed, 0, 6);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for peer hashing", exception);
        }
    }
}
