package com.walkitalki.core.domain;

public sealed interface PttEvent permits
    PttEvent.ScanStarted,
    PttEvent.PeerSelected,
    PttEvent.PairingRequired,
    PttEvent.PairingRejected,
    PttEvent.PairingTimedOut,
    PttEvent.Connected,
    PttEvent.LocalPttPressed,
    PttEvent.LocalPttReleased,
    PttEvent.RemotePttStarted,
    PttEvent.RemotePttStopped,
    PttEvent.SocketFailed,
    PttEvent.AutoReconnectTick,
    PttEvent.UserRequestedReconnect {

    enum ScanStarted implements PttEvent { INSTANCE }
    record PeerSelected(PeerId peerId) implements PttEvent { }
    record PairingRequired(PeerId peerId) implements PttEvent { }
    enum PairingRejected implements PttEvent { INSTANCE }
    enum PairingTimedOut implements PttEvent { INSTANCE }
    record Connected(PeerId peerId) implements PttEvent { }
    enum LocalPttPressed implements PttEvent { INSTANCE }
    enum LocalPttReleased implements PttEvent { INSTANCE }
    record RemotePttStarted(PeerId peerId) implements PttEvent { }
    enum RemotePttStopped implements PttEvent { INSTANCE }
    record SocketFailed(String reason) implements PttEvent { }
    enum AutoReconnectTick implements PttEvent { INSTANCE }
    enum UserRequestedReconnect implements PttEvent { INSTANCE }
}
