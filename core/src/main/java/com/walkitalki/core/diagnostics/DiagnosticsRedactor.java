package com.walkitalki.core.diagnostics;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

public final class DiagnosticsRedactor {
    private DiagnosticsRedactor() {
    }

    public static String export(List<DiagnosticsEvent> events) {
        StringBuilder builder = new StringBuilder();
        for (DiagnosticsEvent event : events) {
            switch (event) {
                case DiagnosticsEvent.AudioFrameReceived audio -> builder
                    .append("AudioFrameReceived(")
                    .append("peerHash=").append(hash(audio.peerId()))
                    .append(", deviceName=<redacted>")
                    .append(", payloadBytes=").append(audio.payload().length)
                    .append(", sequenceNumber=").append(Long.toUnsignedString(audio.sequenceNumber()))
                    .append(")\n");
                case DiagnosticsEvent.TransportStateChanged state -> builder
                    .append("TransportStateChanged(")
                    .append(state.from()).append("->").append(state.to())
                    .append(", reason=").append(state.reason())
                    .append(")\n");
                case DiagnosticsEvent.TransportHeartbeat heartbeat -> builder
                    .append("TransportHeartbeat(")
                    .append("sequenceNumber=").append(Long.toUnsignedString(heartbeat.sequenceNumber()))
                    .append(", health=").append(heartbeat.health())
                    .append(", missedHeartbeatWindows=").append(heartbeat.missedHeartbeatWindows())
                    .append(")\n");
                case DiagnosticsEvent.PeerDiscovery discovery -> builder
                    .append("PeerDiscovery(")
                    .append("state=").append(discovery.state())
                    .append(", reason=").append(discovery.reason())
                    .append(", elapsedMillis=").append(discovery.elapsedMillis())
                    .append(", peerDiscovered=").append(discovery.peerDiscovered())
                    .append(")\n");
                case DiagnosticsEvent.AudioPipeline pipeline -> builder
                    .append("AudioPipeline(")
                    .append("state=").append(pipeline.state())
                    .append(", reason=").append(pipeline.reason())
                    .append(", localFramesSent=").append(pipeline.localFramesSent())
                    .append(", remoteFramesReceived=").append(pipeline.remoteFramesReceived())
                    .append(")\n");
            }
        }
        return builder.toString();
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 6);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for diagnostics redaction", exception);
        }
    }
}
