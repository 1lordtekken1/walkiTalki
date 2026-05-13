package com.walkitalki.core.transport;

public record PeerDiscoverySnapshot(
    boolean active,
    boolean peerDiscovered,
    long startedAtMillis,
    long elapsedMillis,
    String reason
) {
}
