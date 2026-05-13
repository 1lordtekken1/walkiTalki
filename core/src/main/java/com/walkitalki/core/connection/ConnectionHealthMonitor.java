package com.walkitalki.core.connection;

import com.walkitalki.core.modes.OperationModePolicy;

public final class ConnectionHealthMonitor {
    private final OperationModePolicy policy;
    private long lastPongMillis;

    private ConnectionHealthMonitor(long startedAtMillis, OperationModePolicy policy) {
        this.lastPongMillis = startedAtMillis;
        this.policy = policy;
    }

    public static ConnectionHealthMonitor startedAt(long startedAtMillis, OperationModePolicy policy) {
        return new ConnectionHealthMonitor(startedAtMillis, policy);
    }

    public void recordPong(long nowMillis) {
        if (nowMillis < lastPongMillis) {
            throw new IllegalArgumentException("pong timestamp cannot go backwards");
        }
        lastPongMillis = nowMillis;
    }

    public long missedHeartbeatWindowsAt(long nowMillis) {
        if (nowMillis <= lastPongMillis) {
            return 0L;
        }
        long elapsedMillis = nowMillis - lastPongMillis;
        return elapsedMillis / policy.heartbeatIntervalMillis();
    }

    public ConnectionHealth healthAt(long nowMillis) {
        long missed = missedHeartbeatWindowsAt(nowMillis);
        if (missed == 0L) {
            return ConnectionHealth.STABLE;
        }
        if (missed < policy.disconnectAfterMissedHeartbeats()) {
            return ConnectionHealth.DEGRADED;
        }
        return ConnectionHealth.DISCONNECTED;
    }
}
