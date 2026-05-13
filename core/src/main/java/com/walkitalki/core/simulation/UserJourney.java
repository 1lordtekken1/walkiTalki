package com.walkitalki.core.simulation;

import com.walkitalki.core.diagnostics.DiagnosticsEvent;
import com.walkitalki.core.diagnostics.DiagnosticsRedactor;
import com.walkitalki.core.diagnostics.TransportState;
import com.walkitalki.core.domain.PeerId;
import com.walkitalki.core.domain.PttEvent;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.domain.PttStateMachine;
import com.walkitalki.core.permissions.AndroidPermission;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.permissions.PermissionGrantState;
import com.walkitalki.core.permissions.PermissionPolicy;
import com.walkitalki.core.permissions.PlatformVersion;
import com.walkitalki.core.permissions.UseCase;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class UserJourney {
    private final PlatformVersion platform;
    private final EnumSet<AndroidPermission> grantedPermissions = EnumSet.noneOf(AndroidPermission.class);
    private final PttStateMachine stateMachine = PttStateMachine.create();
    private final StringBuilder timeline = new StringBuilder();
    private final List<DiagnosticsEvent> diagnosticsEvents = new ArrayList<>();
    private PermissionAction lastPermissionAction = PermissionAction.ALLOW;
    private String userMessage = "Ready to start";
    private int userActions;

    private UserJourney(PlatformVersion platform) {
        this.platform = platform;
    }

    public static UserJourney onAndroid(int androidApi) {
        return new UserJourney(PlatformVersion.androidApi(androidApi));
    }

    public UserJourney grant(AndroidPermission first, AndroidPermission... rest) {
        grantedPermissions.add(first);
        for (AndroidPermission permission : rest) {
            grantedPermissions.add(permission);
        }
        timeline.append("grant:").append(first);
        for (AndroidPermission permission : rest) {
            timeline.append(",").append(permission);
        }
        timeline.append('\n');
        return this;
    }

    public UserJourney scanForPeers() {
        recordUserAction("scan_for_peers");
        PermissionPolicy.Decision decision = evaluate(UseCase.SCAN_FOR_PEERS);
        if (decision.action() == PermissionAction.ALLOW) {
            PttState from = stateMachine.state();
            stateMachine.apply(PttEvent.ScanStarted.INSTANCE);
            recordState(from);
            userMessage = "Scanning for nearby devices";
        } else if (decision.action() == PermissionAction.REQUEST_NEARBY_DEVICES) {
            userMessage = "Enable Nearby devices to find nearby phones.";
        } else if (decision.action() == PermissionAction.REQUEST_LOCATION_FOR_LEGACY_DISCOVERY) {
            userMessage = "Enable Location so this Android version can discover Bluetooth devices.";
        }
        return this;
    }

    public UserJourney selectPeer(String peerId) {
        recordUserAction("select_peer");
        PttState from = stateMachine.state();
        stateMachine.apply(new PttEvent.PeerSelected(PeerId.of(peerId)));
        recordState(from);
        userMessage = "Connecting to peer";
        return this;
    }

    public UserJourney pairingRequired(String peerId) {
        PttState from = stateMachine.state();
        stateMachine.apply(new PttEvent.PairingRequired(PeerId.of(peerId)));
        recordState(from);
        userMessage = "Pairing required. Confirm the pairing request on both phones.";
        return this;
    }

    public UserJourney pairingRejected() {
        recordUserAction("reject_pairing");
        PttState from = stateMachine.state();
        stateMachine.apply(PttEvent.PairingRejected.INSTANCE);
        recordState(from);
        userMessage = "Pairing rejected. Tap Scan again when both phones are ready.";
        return this;
    }

    public UserJourney connected(String peerId) {
        PttState from = stateMachine.state();
        stateMachine.apply(new PttEvent.Connected(PeerId.of(peerId)));
        recordState(from);
        userMessage = "Ready. Hold to talk.";
        return this;
    }

    public UserJourney pressToTalk() {
        recordUserAction("press_to_talk");
        PermissionPolicy.Decision decision = evaluate(UseCase.TRANSMIT_AUDIO);
        if (decision.action() == PermissionAction.ALLOW) {
            PttState from = stateMachine.state();
            stateMachine.apply(PttEvent.LocalPttPressed.INSTANCE);
            recordState(from);
            userMessage = "Talking…";
        } else if (decision.action() == PermissionAction.REQUEST_MICROPHONE) {
            userMessage = "Enable Microphone to talk. You can still listen.";
        } else {
            userMessage = "Enable Nearby devices before talking.";
        }
        return this;
    }

    public UserJourney releaseToListen() {
        recordUserAction("release_to_listen");
        PttState from = stateMachine.state();
        stateMachine.apply(PttEvent.LocalPttReleased.INSTANCE);
        recordState(from);
        userMessage = "Ready. Hold to talk.";
        return this;
    }

    public UserJourney remoteStartsTalking(String peerId) {
        PttState from = stateMachine.state();
        stateMachine.apply(new PttEvent.RemotePttStarted(PeerId.of(peerId)));
        recordState(from);
        if (stateMachine.state() == PttState.BUSY) {
            userMessage = "Peer is talking. Release Push-To-Talk and listen.";
        } else {
            userMessage = "Receiving…";
        }
        return this;
    }

    public UserJourney receiveAudio(String peerId, String deviceName, byte[] payload) {
        diagnosticsEvents.add(new DiagnosticsEvent.AudioFrameReceived(peerId, deviceName, payload, diagnosticsEvents.size() + 1L));
        timeline.append("diagnostics:audio_frame_received\n");
        return this;
    }

    public UserJourney socketFailed(String reason) {
        PttState from = stateMachine.state();
        stateMachine.apply(new PttEvent.SocketFailed(reason));
        recordState(from);
        userMessage = "Disconnected. Tap reconnect when you are ready.";
        return this;
    }

    public UserJourney autoReconnectTick() {
        PttState from = stateMachine.state();
        stateMachine.apply(PttEvent.AutoReconnectTick.INSTANCE);
        recordState(from);
        if (stateMachine.state() == PttState.PAIRING_REJECTED) {
            userMessage = "Pairing rejected. Tap Scan again when both phones are ready.";
        }
        return this;
    }

    public UserJourneyResult finish() {
        return new UserJourneyResult(
            stateMachine.state(),
            lastPermissionAction,
            userMessage,
            timeline.toString(),
            DiagnosticsRedactor.export(diagnosticsEvents),
            userActions
        );
    }

    private void recordUserAction(String action) {
        userActions++;
        timeline.append("action:").append(action).append('\n');
    }

    private PermissionPolicy.Decision evaluate(UseCase useCase) {
        PermissionPolicy.Decision decision = PermissionPolicy.evaluate(
            platform,
            PermissionGrantState.granted(grantedPermissions),
            useCase
        );
        lastPermissionAction = decision.action();
        timeline
            .append("permission:")
            .append(useCase)
            .append('=')
            .append(decision.action())
            .append('\n');
        return decision;
    }

    private void recordState(PttState from) {
        PttState to = stateMachine.state();
        diagnosticsEvents.add(new DiagnosticsEvent.TransportStateChanged(
            toTransportState(from),
            toTransportState(to),
            stateMachine.lastReason()
        ));
        timeline
            .append("state:")
            .append(to)
            .append(':')
            .append(stateMachine.lastReason())
            .append('\n');
    }

    private static TransportState toTransportState(PttState state) {
        return switch (state) {
            case IDLE -> TransportState.IDLE;
            case SCANNING -> TransportState.SCANNING;
            case PAIRING_REQUIRED, PAIRING_REJECTED, PAIRING_TIMED_OUT -> TransportState.PAIRING_REQUIRED;
            case CONNECTING -> TransportState.CONNECTING;
            case CONNECTED, BUSY -> TransportState.CONNECTED;
            case TRANSMITTING -> TransportState.TRANSMITTING;
            case RECEIVING -> TransportState.RECEIVING;
            case DISCONNECTED -> TransportState.DISCONNECTED;
        };
    }
}
