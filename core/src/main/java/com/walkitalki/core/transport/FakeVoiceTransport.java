package com.walkitalki.core.transport;

import com.walkitalki.core.audio.AudioFrame;
import com.walkitalki.core.bluetooth.BluetoothEnvironment;
import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.connection.ConnectionHealthMonitor;
import com.walkitalki.core.modes.OperationModePolicy;
import com.walkitalki.core.protocol.FrameType;
import com.walkitalki.core.protocol.ProtocolFrame;
import com.walkitalki.core.protocol.ProtocolFrameCodec;

public final class FakeVoiceTransport implements VoiceTransport {
    private final OperationModePolicy policy;
    private ConnectionHealthMonitor healthMonitor;
    private final StringBuilder timeline = new StringBuilder();
    private boolean connected = true;
    private long framesSent;
    private long framesReceived;
    private long sendFailures;
    private boolean peerDiscovered;
    private long audioFramesSent;
    private long audioFramesReceived;

    private FakeVoiceTransport(long connectedAtMillis, OperationModePolicy policy) {
        this.policy = policy;
        this.healthMonitor = ConnectionHealthMonitor.startedAt(connectedAtMillis, policy);
        timeline.append("connected@").append(connectedAtMillis).append('\n');
    }

    public static FakeVoiceTransport connectedAt(long connectedAtMillis, OperationModePolicy policy) {
        return new FakeVoiceTransport(connectedAtMillis, policy);
    }

    @Override
    public FakeVoiceTransport sendPing(long sequenceNumber, long nowMillis) {
        if (!connected) {
            sendFailures++;
            timeline.append("send_failed:not_connected").append('\n');
            return this;
        }

        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.PING, sequenceNumber, nowMillis, new byte[0]));
        ProtocolFrame decoded = ProtocolFrameCodec.decode(encoded);
        framesSent++;
        timeline
            .append("send:")
            .append(decoded.type())
            .append('#')
            .append(decoded.sequenceNumber())
            .append('\n');
        return this;
    }

    @Override
    public FakeVoiceTransport receivePong(long sequenceNumber, long nowMillis) {
        if (!connected) {
            timeline.append("receive_ignored:not_connected").append('\n');
            return this;
        }

        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.PONG, sequenceNumber, nowMillis, new byte[0]));
        ProtocolFrame decoded = ProtocolFrameCodec.decode(encoded);
        healthMonitor.recordPong(nowMillis);
        framesReceived++;
        timeline
            .append("receive:")
            .append(decoded.type())
            .append('#')
            .append(decoded.sequenceNumber())
            .append('\n');
        return this;
    }

    @Override
    public FakeVoiceTransport discoverPairedPeer(String peerId, BluetoothEnvironment environment, long nowMillis) {
        BluetoothEnvironmentState state = environment.state();
        if (state != BluetoothEnvironmentState.READY) {
            peerDiscovered = false;
            timeline.append("discover_blocked:").append(state).append('@').append(nowMillis).append('\n');
            return this;
        }
        if (!environment.isPaired(peerId)) {
            peerDiscovered = false;
            timeline.append("discover_miss:not_paired@").append(nowMillis).append('\n');
            return this;
        }

        peerDiscovered = true;
        timeline.append("discover:paired_peer@").append(nowMillis).append('\n');
        return this;
    }

    @Override
    public FakeVoiceTransport sendAudioFrame(AudioFrame frame) {
        if (!connected) {
            sendFailures++;
            timeline.append("send_failed:not_connected").append('\n');
            return this;
        }

        byte[] payload = frame.payload();
        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(
            FrameType.AUDIO,
            frame.sequenceNumber(),
            frame.timestampMillis(),
            payload
        ));
        ProtocolFrame decoded = ProtocolFrameCodec.decode(encoded);
        framesSent++;
        audioFramesSent++;
        timeline
            .append("send:")
            .append(decoded.type())
            .append('#')
            .append(decoded.sequenceNumber())
            .append(" bytes=")
            .append(decoded.payload().length)
            .append('\n');
        return this;
    }

    @Override
    public FakeVoiceTransport receiveAudioFrame(AudioFrame frame) {
        if (!connected) {
            timeline.append("receive_ignored:not_connected").append('\n');
            return this;
        }

        byte[] payload = frame.payload();
        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(
            FrameType.AUDIO,
            frame.sequenceNumber(),
            frame.timestampMillis(),
            payload
        ));
        ProtocolFrame decoded = ProtocolFrameCodec.decode(encoded);
        framesReceived++;
        audioFramesReceived++;
        timeline
            .append("receive:")
            .append(decoded.type())
            .append('#')
            .append(decoded.sequenceNumber())
            .append(" bytes=")
            .append(decoded.payload().length)
            .append('\n');
        return this;
    }

    @Override
    public FakeVoiceTransport disconnect(String reason) {
        connected = false;
        timeline.append("disconnect:").append(reason).append('\n');
        return this;
    }

    public FakeVoiceTransport autoReconnectTick(long nowMillis) {
        if (!connected) {
            timeline.append("auto_reconnect_ignored:requires_user_action@").append(nowMillis).append('\n');
        }
        return this;
    }

    public FakeVoiceTransport userRequestedReconnect(long nowMillis) {
        connected = true;
        healthMonitor = ConnectionHealthMonitor.startedAt(nowMillis, policy);
        timeline.append("reconnect:user_requested@").append(nowMillis).append('\n');
        return this;
    }

    @Override
    public TransportSessionResult snapshotAt(long nowMillis) {
        ConnectionHealth health = connected ? healthMonitor.healthAt(nowMillis) : ConnectionHealth.DISCONNECTED;
        return new TransportSessionResult(
            health,
            framesSent,
            framesReceived,
            sendFailures,
            healthMonitor.missedHeartbeatWindowsAt(nowMillis),
            peerDiscovered,
            audioFramesSent,
            audioFramesReceived,
            timeline.toString()
        );
    }
}
