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
import com.walkitalki.core.protocol.ProtocolStreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public final class StreamVoiceTransport implements VoiceTransport {
    private final ProtocolStreamReader reader;
    private final OutputStream outputStream;
    private final OperationModePolicy policy;
    private ConnectionHealthMonitor healthMonitor;
    private final StringBuilder timeline = new StringBuilder();
    private boolean connected = true;
    private boolean peerDiscovered;
    private long framesSent;
    private long framesReceived;
    private long sendFailures;
    private long audioFramesSent;
    private long audioFramesReceived;

    private StreamVoiceTransport(
        InputStream inputStream,
        OutputStream outputStream,
        long connectedAtMillis,
        OperationModePolicy policy
    ) {
        this.reader = new ProtocolStreamReader(Objects.requireNonNull(inputStream, "inputStream"));
        this.outputStream = Objects.requireNonNull(outputStream, "outputStream");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.healthMonitor = ConnectionHealthMonitor.startedAt(connectedAtMillis, policy);
        timeline.append("connected@").append(connectedAtMillis).append('\n');
    }

    public static StreamVoiceTransport connected(
        InputStream inputStream,
        OutputStream outputStream,
        long connectedAtMillis,
        OperationModePolicy policy
    ) {
        return new StreamVoiceTransport(inputStream, outputStream, connectedAtMillis, policy);
    }

    @Override
    public StreamVoiceTransport sendPing(long sequenceNumber, long nowMillis) {
        return writeFrame(new ProtocolFrame(FrameType.PING, sequenceNumber, nowMillis, new byte[0]));
    }

    @Override
    public StreamVoiceTransport receivePong(long sequenceNumber, long nowMillis) {
        if (!connected) {
            timeline.append("read_ignored:not_connected").append('\n');
            return this;
        }
        healthMonitor.recordPong(nowMillis);
        framesReceived++;
        timeline.append("read:PONG#").append(sequenceNumber).append('\n');
        return this;
    }

    @Override
    public StreamVoiceTransport discoverPairedPeer(String peerId, BluetoothEnvironment environment, long nowMillis) {
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
    public StreamVoiceTransport sendAudioFrame(AudioFrame frame) {
        byte[] payload = frame.payload();
        return writeFrame(new ProtocolFrame(FrameType.AUDIO, frame.sequenceNumber(), frame.timestampMillis(), payload));
    }

    @Override
    public StreamVoiceTransport receiveAudioFrame(AudioFrame frame) {
        if (!connected) {
            timeline.append("read_ignored:not_connected").append('\n');
            return this;
        }
        framesReceived++;
        audioFramesReceived++;
        timeline
            .append("read:AUDIO#")
            .append(frame.sequenceNumber())
            .append(" bytes=")
            .append(frame.payload().length)
            .append('\n');
        return this;
    }

    public StreamVoiceTransport readNextIncomingFrame() {
        if (!connected) {
            timeline.append("read_ignored:not_connected").append('\n');
            return this;
        }
        ProtocolFrame frame = reader.readNextFrame();
        switch (frame.type()) {
            case PONG -> receivePong(frame.sequenceNumber(), frame.timestampMillis());
            case AUDIO -> {
                framesReceived++;
                audioFramesReceived++;
                timeline
                    .append("read:AUDIO#")
                    .append(frame.sequenceNumber())
                    .append(" bytes=")
                    .append(frame.payload().length)
                    .append('\n');
            }
            case PING -> {
                framesReceived++;
                timeline.append("read:PING#").append(frame.sequenceNumber()).append('\n');
            }
        }
        return this;
    }

    @Override
    public StreamVoiceTransport disconnect(String reason) {
        connected = false;
        timeline.append("disconnect:").append(reason).append('\n');
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

    private StreamVoiceTransport writeFrame(ProtocolFrame frame) {
        if (!connected) {
            sendFailures++;
            timeline.append("write_failed:not_connected").append('\n');
            return this;
        }
        try {
            byte[] encoded = ProtocolFrameCodec.encode(frame);
            outputStream.write(encoded);
            outputStream.flush();
            framesSent++;
            if (frame.type() == FrameType.AUDIO) {
                audioFramesSent++;
            }
            timeline
                .append("write:")
                .append(frame.type())
                .append('#')
                .append(frame.sequenceNumber());
            if (frame.type() == FrameType.AUDIO) {
                timeline.append(" bytes=").append(frame.payload().length);
            }
            timeline.append('\n');
        } catch (IOException exception) {
            sendFailures++;
            timeline.append("write_failed:").append(reason(exception)).append('\n');
        }
        return this;
    }

    private static String reason(IOException exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
