package com.walkitalki.core.session;

import com.walkitalki.core.audio.AudioFrame;
import com.walkitalki.core.audio.AudioInput;
import com.walkitalki.core.audio.AudioOutput;
import com.walkitalki.core.audio.PttAudioController;
import com.walkitalki.core.audio.PttAudioSnapshot;
import com.walkitalki.core.bluetooth.BluetoothEnvironment;
import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.diagnostics.RecordingDiagnosticsSink;
import com.walkitalki.core.domain.PeerId;
import com.walkitalki.core.domain.PttEvent;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.domain.PttStateMachine;
import com.walkitalki.core.modes.OperationModePolicy;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.permissions.PermissionGrantState;
import com.walkitalki.core.permissions.PermissionPolicy;
import com.walkitalki.core.permissions.PlatformVersion;
import com.walkitalki.core.permissions.UseCase;
import com.walkitalki.core.time.Clock;
import com.walkitalki.core.transport.PeerDiscoveryController;
import com.walkitalki.core.transport.PeerDiscoverySnapshot;
import com.walkitalki.core.transport.TransportSessionResult;
import com.walkitalki.core.transport.VoiceTransport;
import com.walkitalki.core.ui.TalkScreenPresenter;
import com.walkitalki.core.ui.TalkScreenState;

import java.util.Objects;

public final class WalkieTalkieSessionController {
    private final PlatformVersion platform;
    private final PermissionGrantState grants;
    private final BluetoothEnvironment environment;
    private final VoiceTransport transport;
    private final Clock clock;
    private final RecordingDiagnosticsSink diagnostics;
    private final int clickBudget;
    private final PttStateMachine stateMachine = PttStateMachine.create();
    private final PeerDiscoveryController discoveryController;
    private final AudioInput audioInput;
    private final AudioOutput audioOutput;
    private PttAudioController audioController;
    private PermissionAction lastPermissionAction = PermissionAction.ALLOW;
    private PeerDiscoverySnapshot discovery = new PeerDiscoverySnapshot(false, false, 0L, 0L, "idle");
    private int userActions;

    private WalkieTalkieSessionController(
        PlatformVersion platform,
        PermissionGrantState grants,
        BluetoothEnvironment environment,
        VoiceTransport transport,
        Clock clock,
        OperationModePolicy policy,
        RecordingDiagnosticsSink diagnostics,
        int clickBudget,
        AudioInput audioInput,
        AudioOutput audioOutput
    ) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.grants = Objects.requireNonNull(grants, "grants");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
        this.clickBudget = clickBudget;
        this.audioInput = audioInput;
        this.audioOutput = audioOutput;
        this.discoveryController = PeerDiscoveryController.start(transport, policy, clock, diagnostics);
    }

    public static WalkieTalkieSessionController create(
        PlatformVersion platform,
        PermissionGrantState grants,
        BluetoothEnvironment environment,
        VoiceTransport transport,
        Clock clock,
        OperationModePolicy policy,
        RecordingDiagnosticsSink diagnostics,
        int clickBudget
    ) {
        return new WalkieTalkieSessionController(platform, grants, environment, transport, clock, policy, diagnostics, clickBudget, null, null);
    }

    public static WalkieTalkieSessionController createWithAudio(
        PlatformVersion platform,
        PermissionGrantState grants,
        BluetoothEnvironment environment,
        VoiceTransport transport,
        Clock clock,
        OperationModePolicy policy,
        RecordingDiagnosticsSink diagnostics,
        int clickBudget,
        AudioInput audioInput,
        AudioOutput audioOutput
    ) {
        return new WalkieTalkieSessionController(
            platform,
            grants,
            environment,
            transport,
            clock,
            policy,
            diagnostics,
            clickBudget,
            Objects.requireNonNull(audioInput, "audioInput"),
            Objects.requireNonNull(audioOutput, "audioOutput")
        );
    }

    public WalkieTalkieSessionSnapshot scanForPeer(String peerId) {
        userActions++;
        PermissionPolicy.Decision decision = evaluate(UseCase.SCAN_FOR_PEERS);
        if (decision.action() == PermissionAction.ALLOW) {
            stateMachine.apply(PttEvent.ScanStarted.INSTANCE);
            discovery = discoveryController.scanForPairedPeer(peerId, environment);
        }
        return snapshot();
    }

    public WalkieTalkieSessionSnapshot connectToPeer(String peerId) {
        userActions++;
        PermissionPolicy.Decision decision = evaluate(UseCase.CONNECT_TO_PEER);
        if (decision.action() == PermissionAction.ALLOW && environment.state() == BluetoothEnvironmentState.READY) {
            PeerId peer = PeerId.of(peerId);
            stateMachine.apply(new PttEvent.PeerSelected(peer));
            stateMachine.apply(new PttEvent.Connected(peer));
            ensureAudioController(peer);
        }
        return snapshot();
    }

    public WalkieTalkieSessionSnapshot pressToTalk() {
        userActions++;
        PermissionPolicy.Decision decision = evaluate(UseCase.TRANSMIT_AUDIO);
        if (decision.action() == PermissionAction.ALLOW) {
            stateMachine.apply(PttEvent.LocalPttPressed.INSTANCE);
            if (audioController != null) {
                audioController.localPttPressed();
            }
        }
        return snapshot();
    }

    public WalkieTalkieSessionSnapshot releaseToListen() {
        userActions++;
        stateMachine.apply(PttEvent.LocalPttReleased.INSTANCE);
        if (audioController != null) {
            audioController.localPttReleased();
        }
        lastPermissionAction = PermissionAction.ALLOW;
        return snapshot();
    }

    public WalkieTalkieSessionSnapshot pumpLocalAudioFrame() {
        if (audioController != null) {
            audioController.pumpCapturedFrame();
        }
        return snapshot();
    }

    public WalkieTalkieSessionSnapshot remoteAudioFrameReceived(String peerId, String deviceName, AudioFrame frame) {
        if (audioController == null || (stateMachine.state() != PttState.CONNECTED && stateMachine.state() != PttState.TRANSMITTING)) {
            return snapshot();
        }

        PeerId peer = PeerId.of(peerId);
        stateMachine.apply(new PttEvent.RemotePttStarted(peer));
        audioController.remoteFrameReceived(peerId, deviceName, frame);
        return snapshot();
    }

    public WalkieTalkieSessionSnapshot snapshot() {
        TransportSessionResult transportSnapshot = transport.snapshotAt(clock.nowMillis());
        ConnectionHealth health = transportSnapshot.health();
        TalkScreenState screen = TalkScreenPresenter.render(
            stateMachine.state(),
            lastPermissionAction,
            environment.state(),
            health,
            userActions,
            clickBudget
        );
        return new WalkieTalkieSessionSnapshot(
            stateMachine.state(),
            lastPermissionAction,
            environment.state(),
            health,
            userActions,
            discovery,
            transportSnapshot,
            audioSnapshot(),
            screen,
            diagnostics.exportRedacted()
        );
    }

    private void ensureAudioController(PeerId peer) {
        if (audioController == null && audioInput != null && audioOutput != null) {
            audioController = PttAudioController.connected(peer, audioInput, audioOutput, transport, diagnostics);
        }
    }

    private PttAudioSnapshot audioSnapshot() {
        if (audioController != null) {
            return audioController.snapshot();
        }
        return new PttAudioSnapshot(stateMachine.state(), stateMachine.lastReason(), 0L, 0L, false);
    }

    private PermissionPolicy.Decision evaluate(UseCase useCase) {
        PermissionPolicy.Decision decision = PermissionPolicy.evaluate(platform, grants, useCase);
        lastPermissionAction = decision.action();
        return decision;
    }
}
