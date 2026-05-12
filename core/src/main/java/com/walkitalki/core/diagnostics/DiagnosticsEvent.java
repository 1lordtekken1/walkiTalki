package com.walkitalki.core.diagnostics;

public sealed interface DiagnosticsEvent permits DiagnosticsEvent.AudioFrameReceived, DiagnosticsEvent.TransportStateChanged, DiagnosticsEvent.TransportHeartbeat, DiagnosticsEvent.PeerDiscovery, DiagnosticsEvent.AudioPipeline {
    record AudioFrameReceived(
        String peerId,
        String peerDeviceName,
        byte[] payload,
        long sequenceNumber
    ) implements DiagnosticsEvent {
        public AudioFrameReceived {
            payload = payload.clone();
        }

        @Override
        public byte[] payload() {
            return payload.clone();
        }
    }

    record TransportStateChanged(
        TransportState from,
        TransportState to,
        String reason
    ) implements DiagnosticsEvent {
    }

    record TransportHeartbeat(
        long sequenceNumber,
        String health,
        long missedHeartbeatWindows
    ) implements DiagnosticsEvent {
    }

    record PeerDiscovery(
        String state,
        String reason,
        long elapsedMillis,
        boolean peerDiscovered
    ) implements DiagnosticsEvent {
    }

    record AudioPipeline(
        String state,
        String reason,
        long localFramesSent,
        long remoteFramesReceived
    ) implements DiagnosticsEvent {
    }
}
