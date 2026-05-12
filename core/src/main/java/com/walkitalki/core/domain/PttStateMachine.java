package com.walkitalki.core.domain;

import java.util.Optional;

public final class PttStateMachine {
    private PttState state;
    private PeerId peerId;
    private String lastReason;

    private PttStateMachine(PttState state, PeerId peerId, String lastReason) {
        this.state = state;
        this.peerId = peerId;
        this.lastReason = lastReason;
    }

    public static PttStateMachine create() {
        return new PttStateMachine(PttState.IDLE, null, "created");
    }

    public static PttStateMachine connected(PeerId peerId) {
        return new PttStateMachine(PttState.CONNECTED, peerId, "connected");
    }

    public static PttStateMachine disconnected(String reason) {
        return new PttStateMachine(PttState.DISCONNECTED, null, reason);
    }

    public void apply(PttEvent event) {
        switch (event) {
            case PttEvent.ScanStarted ignored -> transition(PttState.SCANNING, null, "scan_started");
            case PttEvent.PeerSelected selected -> transition(PttState.CONNECTING, selected.peerId(), "peer_selected");
            case PttEvent.PairingRequired pairing -> transition(PttState.PAIRING_REQUIRED, pairing.peerId(), "pairing_required");
            case PttEvent.PairingRejected ignored -> transition(PttState.PAIRING_REJECTED, peerId, "pairing_rejected");
            case PttEvent.PairingTimedOut ignored -> transition(PttState.PAIRING_TIMED_OUT, peerId, "pairing_timed_out");
            case PttEvent.Connected connected -> transition(PttState.CONNECTED, connected.peerId(), "connected");
            case PttEvent.LocalPttPressed ignored -> handleLocalPttPressed();
            case PttEvent.LocalPttReleased ignored -> handleLocalPttReleased();
            case PttEvent.RemotePttStarted remote -> handleRemotePttStarted(remote.peerId());
            case PttEvent.RemotePttStopped ignored -> handleRemotePttStopped();
            case PttEvent.SocketFailed failed -> transition(PttState.DISCONNECTED, peerId, failed.reason());
            case PttEvent.AutoReconnectTick ignored -> {
                if (state != PttState.DISCONNECTED) {
                    transition(state, peerId, "auto_reconnect_ignored_not_disconnected");
                } else {
                    transition(PttState.DISCONNECTED, peerId, "auto_reconnect_requires_user_action");
                }
            }
            case PttEvent.UserRequestedReconnect ignored -> transition(PttState.SCANNING, null, "user_requested_reconnect");
        }
    }

    public PttState state() {
        return state;
    }

    public Optional<PeerId> peerId() {
        return Optional.ofNullable(peerId);
    }

    public String lastReason() {
        return lastReason;
    }

    private void handleLocalPttPressed() {
        if (state == PttState.CONNECTED) {
            transition(PttState.TRANSMITTING, peerId, "local_ptt_pressed");
        } else if (state == PttState.RECEIVING) {
            transition(PttState.BUSY, peerId, "local_pressed_while_remote_receiving");
        } else {
            transition(state, peerId, "local_ptt_ignored_in_" + state);
        }
    }

    private void handleLocalPttReleased() {
        if (state == PttState.TRANSMITTING || state == PttState.BUSY) {
            transition(PttState.CONNECTED, peerId, "local_ptt_released");
        } else {
            transition(state, peerId, "local_release_ignored_in_" + state);
        }
    }

    private void handleRemotePttStarted(PeerId remotePeerId) {
        if (state == PttState.CONNECTED) {
            transition(PttState.RECEIVING, remotePeerId, "remote_ptt_started");
        } else if (state == PttState.TRANSMITTING) {
            transition(PttState.BUSY, remotePeerId, "remote_started_while_local_transmitting");
        } else {
            transition(state, peerId, "remote_ptt_ignored_in_" + state);
        }
    }

    private void handleRemotePttStopped() {
        if (state == PttState.RECEIVING || state == PttState.BUSY) {
            transition(PttState.CONNECTED, peerId, "remote_ptt_stopped");
        } else {
            transition(state, peerId, "remote_stop_ignored_in_" + state);
        }
    }

    private void transition(PttState newState, PeerId newPeerId, String reason) {
        state = newState;
        peerId = newPeerId;
        lastReason = reason;
    }
}
