package com.walkitalki.core.transport;

import com.walkitalki.core.connection.ConnectionHealth;

public record TransportSessionResult(
    ConnectionHealth health,
    long framesSent,
    long framesReceived,
    long sendFailures,
    long missedHeartbeatWindows,
    boolean peerDiscovered,
    long audioFramesSent,
    long audioFramesReceived,
    String timeline
) {
}
