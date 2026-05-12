package com.walkitalki.core.transport;

import com.walkitalki.core.bluetooth.BluetoothEnvironment;
import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.diagnostics.DiagnosticsEvent;
import com.walkitalki.core.diagnostics.DiagnosticsSink;
import com.walkitalki.core.modes.OperationModePolicy;
import com.walkitalki.core.time.Clock;

import java.util.Objects;

public final class PeerDiscoveryController {
    private final VoiceTransport transport;
    private final OperationModePolicy policy;
    private final Clock clock;
    private final DiagnosticsSink diagnosticsSink;
    private boolean active;
    private boolean peerDiscovered;
    private long startedAtMillis;
    private String reason = "idle";

    private PeerDiscoveryController(
        VoiceTransport transport,
        OperationModePolicy policy,
        Clock clock,
        DiagnosticsSink diagnosticsSink
    ) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.diagnosticsSink = Objects.requireNonNull(diagnosticsSink, "diagnosticsSink");
    }

    public static PeerDiscoveryController start(
        VoiceTransport transport,
        OperationModePolicy policy,
        Clock clock,
        DiagnosticsSink diagnosticsSink
    ) {
        return new PeerDiscoveryController(transport, policy, clock, diagnosticsSink);
    }

    public PeerDiscoverySnapshot scanForPairedPeer(String peerId, BluetoothEnvironment environment) {
        Objects.requireNonNull(peerId, "peerId");
        Objects.requireNonNull(environment, "environment");
        long nowMillis = clock.nowMillis();
        BluetoothEnvironmentState state = environment.state();
        if (state != BluetoothEnvironmentState.READY) {
            active = false;
            peerDiscovered = false;
            startedAtMillis = nowMillis;
            reason = state.name();
            diagnosticsSink.record(new DiagnosticsEvent.PeerDiscovery("blocked", reason, 0L, false));
            return snapshotAt(nowMillis);
        }

        startedAtMillis = nowMillis;
        active = true;
        transport.discoverPairedPeer(peerId, environment, nowMillis);
        TransportSessionResult result = transport.snapshotAt(nowMillis);
        peerDiscovered = result.peerDiscovered();
        reason = peerDiscovered ? "paired_peer" : "scanning";
        diagnosticsSink.record(new DiagnosticsEvent.PeerDiscovery("started", reason, 0L, peerDiscovered));
        return snapshotAt(nowMillis);
    }

    public PeerDiscoverySnapshot poll() {
        long nowMillis = clock.nowMillis();
        if (!active) {
            return snapshotAt(nowMillis);
        }
        long elapsedMillis = nowMillis - startedAtMillis;
        if (elapsedMillis >= policy.scanWindowMillis()) {
            active = false;
            reason = "scan_window_elapsed";
            diagnosticsSink.record(new DiagnosticsEvent.PeerDiscovery("stopped", reason, elapsedMillis, peerDiscovered));
        }
        return snapshotAt(nowMillis);
    }

    private PeerDiscoverySnapshot snapshotAt(long nowMillis) {
        long elapsedMillis = Math.max(0L, nowMillis - startedAtMillis);
        return new PeerDiscoverySnapshot(active, peerDiscovered, startedAtMillis, elapsedMillis, reason);
    }
}
