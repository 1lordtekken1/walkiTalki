package com.walkitalki.core.transport;

import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.diagnostics.DiagnosticsEvent;
import com.walkitalki.core.diagnostics.DiagnosticsSink;
import com.walkitalki.core.diagnostics.TransportState;
import com.walkitalki.core.modes.OperationModePolicy;
import com.walkitalki.core.time.Clock;

import java.util.Objects;

public final class TransportHeartbeatController {
    private final VoiceTransport transport;
    private final OperationModePolicy policy;
    private final Clock clock;
    private final DiagnosticsSink diagnosticsSink;
    private long lastHeartbeatMillis;
    private long nextSequenceNumber = 1L;
    private ConnectionHealth lastHealth;
    private boolean heartbeatSent;

    private TransportHeartbeatController(
        VoiceTransport transport,
        OperationModePolicy policy,
        Clock clock,
        DiagnosticsSink diagnosticsSink
    ) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.diagnosticsSink = Objects.requireNonNull(diagnosticsSink, "diagnosticsSink");
        this.lastHealth = transport.snapshotAt(clock.nowMillis()).health();
    }

    public static TransportHeartbeatController start(
        VoiceTransport transport,
        OperationModePolicy policy,
        Clock clock,
        DiagnosticsSink diagnosticsSink
    ) {
        return new TransportHeartbeatController(transport, policy, clock, diagnosticsSink);
    }

    public void poll() {
        long nowMillis = clock.nowMillis();
        TransportSessionResult snapshot = transport.snapshotAt(nowMillis);
        ConnectionHealth health = snapshot.health();
        recordHealthTransitionIfNeeded(health);
        if (health == ConnectionHealth.DISCONNECTED) {
            return;
        }
        if (heartbeatDue(nowMillis)) {
            long sequenceNumber = nextSequenceNumber++;
            transport.sendPing(sequenceNumber, nowMillis);
            heartbeatSent = true;
            lastHeartbeatMillis = nowMillis;
            TransportSessionResult afterSend = transport.snapshotAt(nowMillis);
            diagnosticsSink.record(new DiagnosticsEvent.TransportHeartbeat(
                sequenceNumber,
                afterSend.health().name(),
                afterSend.missedHeartbeatWindows()
            ));
        }
    }

    private boolean heartbeatDue(long nowMillis) {
        return !heartbeatSent || nowMillis - lastHeartbeatMillis >= policy.heartbeatIntervalMillis();
    }

    private void recordHealthTransitionIfNeeded(ConnectionHealth health) {
        if (lastHealth != ConnectionHealth.DISCONNECTED && health == ConnectionHealth.DISCONNECTED) {
            diagnosticsSink.record(new DiagnosticsEvent.TransportStateChanged(
                TransportState.CONNECTED,
                TransportState.DISCONNECTED,
                "heartbeat_missed"
            ));
        }
        lastHealth = health;
    }
}
