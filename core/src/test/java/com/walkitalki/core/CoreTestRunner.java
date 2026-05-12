package com.walkitalki.core;

import com.walkitalki.core.audio.AudioFrame;
import com.walkitalki.core.audio.JitterBuffer;
import com.walkitalki.core.audio.AudioInput;
import com.walkitalki.core.audio.AudioOutput;
import com.walkitalki.core.audio.FakeAudioInput;
import com.walkitalki.core.audio.FakeAudioOutput;
import com.walkitalki.core.audio.JitterBufferSnapshot;
import com.walkitalki.core.audio.PttAudioController;
import com.walkitalki.core.audio.PttAudioSnapshot;
import com.walkitalki.core.bluetooth.BluetoothEnvironment;
import com.walkitalki.core.bluetooth.BluetoothEnvironmentState;
import com.walkitalki.core.bluetooth.FakeBluetoothEnvironment;
import com.walkitalki.core.connection.ConnectionHealth;
import com.walkitalki.core.connection.ConnectionHealthMonitor;
import com.walkitalki.core.modes.OperationMode;
import com.walkitalki.core.modes.OperationModePolicy;
import com.walkitalki.core.diagnostics.DiagnosticsEvent;
import com.walkitalki.core.diagnostics.DiagnosticsRedactor;
import com.walkitalki.core.diagnostics.RecordingDiagnosticsSink;
import com.walkitalki.core.diagnostics.TransportState;
import com.walkitalki.core.domain.PeerId;
import com.walkitalki.core.domain.PttEvent;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.domain.PttStateMachine;
import com.walkitalki.core.simulation.SimulationScenario;
import com.walkitalki.core.simulation.SimulationScenarioCatalog;
import com.walkitalki.core.simulation.SimulationScenarioReportRenderer;
import com.walkitalki.core.simulation.SimulationScenarioReportWriter;
import com.walkitalki.core.simulation.ReadinessScore;
import com.walkitalki.core.simulation.ReadinessScorecard;
import com.walkitalki.core.simulation.ReadinessReportRenderer;
import com.walkitalki.core.simulation.ReadinessReportWriter;
import com.walkitalki.core.simulation.MvpGateDecision;
import com.walkitalki.core.simulation.MvpGateEvaluator;
import com.walkitalki.core.simulation.MvpGateReportRenderer;
import com.walkitalki.core.simulation.MvpGateReportWriter;
import com.walkitalki.core.simulation.UserJourney;
import com.walkitalki.core.simulation.UserJourneyResult;
import com.walkitalki.core.session.WalkieTalkieSessionController;
import com.walkitalki.core.session.WalkieTalkieSessionSnapshot;
import com.walkitalki.core.transport.FakeVoiceTransport;
import com.walkitalki.core.transport.VoiceTransport;
import com.walkitalki.core.transport.TransportSessionResult;
import com.walkitalki.core.transport.TransportHeartbeatController;
import com.walkitalki.core.transport.StreamVoiceTransport;
import com.walkitalki.core.transport.PeerDiscoveryController;
import com.walkitalki.core.transport.PeerDiscoverySnapshot;
import com.walkitalki.core.ui.TalkScreenPresenter;
import com.walkitalki.core.ui.TalkScreenState;
import com.walkitalki.core.quality.ArchitectureGuard;
import com.walkitalki.core.time.FakeClock;
import com.walkitalki.core.permissions.AndroidPermission;
import com.walkitalki.core.permissions.PermissionAction;
import com.walkitalki.core.permissions.PermissionGrantState;
import com.walkitalki.core.permissions.PermissionPolicy;
import com.walkitalki.core.permissions.PlatformVersion;
import com.walkitalki.core.permissions.UseCase;
import com.walkitalki.core.protocol.FrameType;
import com.walkitalki.core.protocol.ProtocolException;
import com.walkitalki.core.protocol.ProtocolFrame;
import com.walkitalki.core.protocol.ProtocolFrameCodec;
import com.walkitalki.core.protocol.ProtocolStreamReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class CoreTestRunner {
    public static void main(String[] args) {
        encodesAndDecodesPingFrame();
        decodesFirstFrameFromConcatenatedStreamAndReportsConsumedBytes();
        rejectsInvalidMagic();
        rejectsPayloadAboveMaximumSize();
        unknownFrameTypeIsRecoverableProtocolError();
        diagnosticsExportRedactsSensitiveAudioAndPeerData();
        diagnosticsExportKeepsStateTransitionsVisible();
        android12ScanRequiresNearbyDevicesPermissions();
        android11DiscoveryRequiresLocationWhenNeeded();
        microphoneIsRequiredOnlyForTransmit();
        partialPermissionGrantsReturnSpecificNextAction();
        pttStateMachineCoversConnectTransmitReceiveAndDisconnect();
        simultaneousPttIsDeterministicBusyState();
        pairingFailuresAreExplicitStates();
        manualReconnectRequiresUserAction();
        userCanCompleteHappyPathFromPermissionsToTalk();
        userBlockedByDeniedNearbyDevicesGetsActionableGuidance();
        userRejectedPairingCanRetryWithoutAutoReconnect();
        userSeesBusyWhenPeerTalksDuringLocalTransmit();
        userDisconnectFlowKeepsDiagnosticsVisibleAndRedacted();
        jitterBufferPlaysInOrderFrames();
        jitterBufferReordersOutOfOrderFrames();
        jitterBufferDropsDuplicatesAndOldFrames();
        jitterBufferReportsUnderrunsAndCapsMemory();
        operationModesTuneScanHeartbeatAndBufferSettings();
        diagnosticModeKeepsShortHeartbeatAndVerboseDiagnostics();
        connectionHealthIsStableWithRecentPong();
        connectionHealthDegradesAndDisconnectsAfterMissedHeartbeatWindows();
        userJourneyTracksClickBudgetForHappyPath();
        fakeTransportExchangesPingPongFrames();
        fakeTransportReportsConnectionHealthAfterMissedPongs();
        fakeTransportRejectsSendWhenDisconnected();
        streamReaderDecodesFrameSplitAcrossReads();
        streamReaderDecodesConcatenatedFramesSequentially();
        voiceTransportInterfaceIsImplementedByFakeTransport();
        streamReaderRejectsEofDuringPayload();
        streamReaderRejectsOversizedPayloadBeforeAllocatingPayload();
        streamReaderRejectsInvalidMagicFromStream();
        streamReaderMapsIoFailureToProtocolException();
        fakeTransportReconnectRequiresExplicitUserIntent();
        bluetoothEnvironmentFakeReportsPowerPermissionAndPairingStates();
        audioInputFakeCapturesOnlyWhenStarted();
        audioOutputFakeUsesJitterBufferForPlaybackOrder();
        fakeTransportDiscoversOnlyReadyPairedPeers();
        fakeTransportSendsAndReceivesAudioFramesWithoutPayloadInTimeline();
        talkScreenBlocksPushToTalkUntilNearbyPermissionIsGranted();
        talkScreenEnablesPushToTalkOnlyWhenConnectedAndStable();
        talkScreenShowsReconnectAndDiagnosticsWhenDisconnected();
        heartbeatControllerUsesInjectedClockWithoutRealDelays();
        heartbeatControllerReportsMissedHeartbeatDisconnectWithoutPeerLeak();
        discoveryControllerStopsAfterConfiguredScanWindowWithoutRealDelay();
        discoveryControllerBlocksMissingPermissionAndRedactsPeerIdentifier();
        streamVoiceTransportWritesPingFrameToOutputStream();
        streamVoiceTransportReadsPongAndAudioMetadataWithoutPayloadInTimeline();
        streamVoiceTransportMapsWriteFailureToSendFailure();
        pttAudioControllerStartsCaptureOnlyWhenConnectedAndSendsFrames();
        pttAudioControllerRoutesRemoteFramesThroughJitterWithoutPayloadDiagnostics();
        pttAudioControllerDoesNotCaptureWhenRemoteTalkingBusy();
        sessionControllerBlocksScanUntilNearbyPermissionGranted();
        sessionControllerConnectsAndTransmitsWithinClickBudgetWithoutPeerLeak();
        sessionControllerSendsAudioOnlyWhilePressed();
        sessionControllerRoutesRemoteAudioThroughOutputAndRedactsDiagnostics();
        sessionControllerIgnoresRemoteAudioBeforeConnection();
        simulationCatalogCoversOneHundredStakeholderScenarios();
        simulationScenarioReportExportsCatalogForQa();
        simulationScenarioReportWriterCreatesCiArtifact();
        readinessScorecardKeepsStakeholderAndModulePathsExecutable();
        readinessReportRendererExportsScorecardAndGates();
        readinessReportWriterCreatesCiArtifact();
        mvpGateStaysNoGoUntilAndroidAndDeviceEvidenceExist();
        mvpGateReportExportsNoGoDecisionForStakeholders();
        mvpGateReportWriterCreatesCiArtifact();
        architectureGuardKeepsCoreAndPreviewFrameworkFree();
        System.out.println("Core unit tests passed");
    }

    private static void encodesAndDecodesPingFrame() {
        ProtocolFrame frame = new ProtocolFrame(FrameType.PING, 42L, 123_456_789L, new byte[] {1, 2, 3});

        ProtocolFrame decoded = ProtocolFrameCodec.decode(ProtocolFrameCodec.encode(frame));

        assertEquals(frame.type(), decoded.type(), "type");
        assertEquals(frame.sequenceNumber(), decoded.sequenceNumber(), "sequenceNumber");
        assertEquals(frame.timestampMillis(), decoded.timestampMillis(), "timestampMillis");
        assertByteArrayEquals(frame.payload(), decoded.payload(), "payload");
    }

    private static void decodesFirstFrameFromConcatenatedStreamAndReportsConsumedBytes() {
        ProtocolFrame first = new ProtocolFrame(FrameType.PING, 1L, 10L, new byte[] {7});
        ProtocolFrame second = new ProtocolFrame(FrameType.PONG, 2L, 20L, new byte[] {8, 9});
        byte[] firstBytes = ProtocolFrameCodec.encode(first);
        byte[] secondBytes = ProtocolFrameCodec.encode(second);
        byte[] bytes = concat(firstBytes, secondBytes);

        var result = ProtocolFrameCodec.decodeNext(bytes);

        assertEquals(first, result.frame(), "first decoded frame");
        assertEquals(firstBytes.length, result.consumedBytes(), "consumed bytes");
    }

    private static void rejectsInvalidMagic() {
        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.PING, 1L, 1L, new byte[0]));
        encoded[0] = 0x00;

        assertThrows(ProtocolException.InvalidMagic.class, () -> ProtocolFrameCodec.decode(encoded));
    }

    private static void rejectsPayloadAboveMaximumSize() {
        byte[] oversized = new byte[ProtocolFrameCodec.MAX_PAYLOAD_BYTES + 1];

        assertThrows(
            ProtocolException.PayloadTooLarge.class,
            () -> ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.AUDIO, 1L, 1L, oversized))
        );
    }

    private static void unknownFrameTypeIsRecoverableProtocolError() {
        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.PING, 1L, 1L, new byte[0]));
        encoded[ProtocolFrameCodec.TYPE_OFFSET] = 0x7F;

        ProtocolException.UnknownFrameType error = assertThrows(
            ProtocolException.UnknownFrameType.class,
            () -> ProtocolFrameCodec.decode(encoded)
        );

        assertEquals(0x7F, error.rawType(), "rawType");
        assertTrue(error.recoverable(), "unknown frame type must be recoverable");
    }

    private static void diagnosticsExportRedactsSensitiveAudioAndPeerData() {
        DiagnosticsEvent event = new DiagnosticsEvent.AudioFrameReceived(
            "AA:BB:CC:DD:EE:FF",
            "Alice Personal Phone",
            new byte[] {1, 2, 3, 4},
            7L
        );

        String exported = DiagnosticsRedactor.export(List.of(event));

        assertFalse(exported.contains("AA:BB:CC:DD:EE:FF"), "raw peer id must be redacted");
        assertFalse(exported.contains("Alice Personal Phone"), "device name must be redacted");
        assertFalse(exported.contains("1, 2, 3, 4"), "audio payload must be absent");
        assertContains(exported, "peerHash=", "peer hash must remain for correlation");
        assertContains(exported, "sequenceNumber=7", "sequence number must remain visible");
        assertContains(exported, "payloadBytes=4", "payload byte count must remain visible");
    }

    private static void diagnosticsExportKeepsStateTransitionsVisible() {
        String exported = DiagnosticsRedactor.export(
            List.of(new DiagnosticsEvent.TransportStateChanged(
                TransportState.SCANNING,
                TransportState.CONNECTING,
                "peer_selected"
            ))
        );

        assertContains(exported, "SCANNING->CONNECTING", "state transition must be visible");
        assertContains(exported, "peer_selected", "transition reason must be visible");
    }

    private static void android12ScanRequiresNearbyDevicesPermissions() {
        PermissionPolicy.Decision decision = PermissionPolicy.evaluate(
            PlatformVersion.androidApi(33),
            PermissionGrantState.none(),
            UseCase.SCAN_FOR_PEERS
        );

        assertEquals(PermissionAction.REQUEST_NEARBY_DEVICES, decision.action(), "Android 12+ scan action");
        assertTrue(decision.requiredPermissions().contains(AndroidPermission.BLUETOOTH_SCAN), "BLUETOOTH_SCAN required");
        assertTrue(decision.requiredPermissions().contains(AndroidPermission.BLUETOOTH_CONNECT), "BLUETOOTH_CONNECT required");
    }

    private static void android11DiscoveryRequiresLocationWhenNeeded() {
        PermissionPolicy.Decision decision = PermissionPolicy.evaluate(
            PlatformVersion.androidApi(30),
            PermissionGrantState.granted(AndroidPermission.BLUETOOTH, AndroidPermission.BLUETOOTH_ADMIN),
            UseCase.SCAN_FOR_PEERS
        );

        assertEquals(PermissionAction.REQUEST_LOCATION_FOR_LEGACY_DISCOVERY, decision.action(), "legacy discovery action");
        assertTrue(decision.requiredPermissions().contains(AndroidPermission.ACCESS_FINE_LOCATION), "location required for legacy discovery");
    }

    private static void microphoneIsRequiredOnlyForTransmit() {
        PermissionPolicy.Decision receiveDecision = PermissionPolicy.evaluate(
            PlatformVersion.androidApi(33),
            PermissionGrantState.granted(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT),
            UseCase.RECEIVE_AUDIO
        );
        PermissionPolicy.Decision transmitDecision = PermissionPolicy.evaluate(
            PlatformVersion.androidApi(33),
            PermissionGrantState.granted(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT),
            UseCase.TRANSMIT_AUDIO
        );

        assertEquals(PermissionAction.ALLOW, receiveDecision.action(), "receive without microphone");
        assertEquals(PermissionAction.REQUEST_MICROPHONE, transmitDecision.action(), "transmit needs microphone");
    }

    private static void partialPermissionGrantsReturnSpecificNextAction() {
        PermissionPolicy.Decision decision = PermissionPolicy.evaluate(
            PlatformVersion.androidApi(33),
            PermissionGrantState.granted(AndroidPermission.BLUETOOTH_SCAN),
            UseCase.CONNECT_TO_PEER
        );

        assertEquals(PermissionAction.REQUEST_NEARBY_DEVICES, decision.action(), "partial nearby permission action");
        assertTrue(decision.requiredPermissions().contains(AndroidPermission.BLUETOOTH_CONNECT), "missing connect permission is explicit");
    }

    private static void pttStateMachineCoversConnectTransmitReceiveAndDisconnect() {
        PttStateMachine machine = PttStateMachine.create();
        PeerId peer = PeerId.of("peer-a");

        assertEquals(PttState.IDLE, machine.state(), "initial state");
        machine.apply(PttEvent.ScanStarted.INSTANCE);
        assertEquals(PttState.SCANNING, machine.state(), "scan state");
        machine.apply(new PttEvent.PeerSelected(peer));
        assertEquals(PttState.CONNECTING, machine.state(), "connecting state");
        machine.apply(new PttEvent.Connected(peer));
        assertEquals(PttState.CONNECTED, machine.state(), "connected state");
        machine.apply(PttEvent.LocalPttPressed.INSTANCE);
        assertEquals(PttState.TRANSMITTING, machine.state(), "transmitting state");
        machine.apply(PttEvent.LocalPttReleased.INSTANCE);
        assertEquals(PttState.CONNECTED, machine.state(), "back to connected after release");
        machine.apply(new PttEvent.RemotePttStarted(peer));
        assertEquals(PttState.RECEIVING, machine.state(), "receiving state");
        machine.apply(PttEvent.RemotePttStopped.INSTANCE);
        assertEquals(PttState.CONNECTED, machine.state(), "back to connected after remote stop");
        machine.apply(new PttEvent.SocketFailed("link_loss"));
        assertEquals(PttState.DISCONNECTED, machine.state(), "disconnected state");
    }

    private static void simultaneousPttIsDeterministicBusyState() {
        PttStateMachine machine = PttStateMachine.connected(PeerId.of("peer-a"));

        machine.apply(PttEvent.LocalPttPressed.INSTANCE);
        machine.apply(new PttEvent.RemotePttStarted(PeerId.of("peer-a")));

        assertEquals(PttState.BUSY, machine.state(), "simultaneous PTT resolves to busy");
        assertEquals("remote_started_while_local_transmitting", machine.lastReason(), "busy reason");
    }

    private static void pairingFailuresAreExplicitStates() {
        PttStateMachine machine = PttStateMachine.create();

        machine.apply(new PttEvent.PairingRequired(PeerId.of("peer-a")));
        assertEquals(PttState.PAIRING_REQUIRED, machine.state(), "pairing required state");
        machine.apply(PttEvent.PairingRejected.INSTANCE);
        assertEquals(PttState.PAIRING_REJECTED, machine.state(), "pairing rejected state");

        machine = PttStateMachine.create();
        machine.apply(new PttEvent.PairingRequired(PeerId.of("peer-a")));
        machine.apply(PttEvent.PairingTimedOut.INSTANCE);
        assertEquals(PttState.PAIRING_TIMED_OUT, machine.state(), "pairing timeout state");
    }

    private static void manualReconnectRequiresUserAction() {
        PttStateMachine machine = PttStateMachine.disconnected("link_loss");

        machine.apply(PttEvent.AutoReconnectTick.INSTANCE);
        assertEquals(PttState.DISCONNECTED, machine.state(), "auto reconnect must not start in MVP");
        machine.apply(PttEvent.UserRequestedReconnect.INSTANCE);
        assertEquals(PttState.SCANNING, machine.state(), "manual reconnect starts scanning");
    }

    private static void userCanCompleteHappyPathFromPermissionsToTalk() {
        UserJourneyResult result = UserJourney.onAndroid(33)
            .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT, AndroidPermission.RECORD_AUDIO)
            .scanForPeers()
            .selectPeer("peer-a")
            .connected("peer-a")
            .pressToTalk()
            .releaseToListen()
            .finish();

        assertEquals(PttState.CONNECTED, result.state(), "happy path final state");
        assertContains(result.timeline(), "permission:SCAN_FOR_PEERS=ALLOW", "scan permission timeline");
        assertContains(result.timeline(), "state:TRANSMITTING", "transmit state timeline");
        assertContains(result.userMessage(), "Ready", "happy path user message");
    }

    private static void userBlockedByDeniedNearbyDevicesGetsActionableGuidance() {
        UserJourneyResult result = UserJourney.onAndroid(33)
            .scanForPeers()
            .finish();

        assertEquals(PttState.IDLE, result.state(), "permission denied keeps idle");
        assertEquals(PermissionAction.REQUEST_NEARBY_DEVICES, result.lastPermissionAction(), "nearby devices action");
        assertContains(result.userMessage(), "Enable Nearby devices", "actionable nearby guidance");
    }

    private static void userRejectedPairingCanRetryWithoutAutoReconnect() {
        UserJourneyResult result = UserJourney.onAndroid(33)
            .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT)
            .scanForPeers()
            .pairingRequired("peer-a")
            .pairingRejected()
            .autoReconnectTick()
            .finish();

        assertEquals(PttState.PAIRING_REJECTED, result.state(), "pairing rejected remains explicit");
        assertContains(result.userMessage(), "Pairing rejected", "pairing rejected message");
        assertFalse(result.timeline().contains("user_requested_reconnect"), "auto reconnect must not masquerade as user retry");
    }

    private static void userSeesBusyWhenPeerTalksDuringLocalTransmit() {
        UserJourneyResult result = UserJourney.onAndroid(33)
            .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT, AndroidPermission.RECORD_AUDIO)
            .scanForPeers()
            .selectPeer("peer-a")
            .connected("peer-a")
            .pressToTalk()
            .remoteStartsTalking("peer-a")
            .finish();

        assertEquals(PttState.BUSY, result.state(), "busy state after simultaneous PTT");
        assertContains(result.userMessage(), "Peer is talking", "busy user message");
        assertContains(result.timeline(), "remote_started_while_local_transmitting", "busy reason timeline");
    }

    private static void userDisconnectFlowKeepsDiagnosticsVisibleAndRedacted() {
        UserJourneyResult result = UserJourney.onAndroid(33)
            .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT, AndroidPermission.RECORD_AUDIO)
            .scanForPeers()
            .selectPeer("AA:BB:CC:DD:EE:FF")
            .connected("AA:BB:CC:DD:EE:FF")
            .receiveAudio("AA:BB:CC:DD:EE:FF", "Alice Personal Phone", new byte[] {9, 8, 7})
            .socketFailed("link_loss")
            .finish();

        assertEquals(PttState.DISCONNECTED, result.state(), "disconnect state");
        assertContains(result.userMessage(), "Disconnected", "disconnect user message");
        assertContains(result.diagnostics(), "peerHash=", "diagnostics keep redacted hash");
        assertFalse(result.diagnostics().contains("AA:BB:CC:DD:EE:FF"), "diagnostics redact raw peer id");
        assertFalse(result.diagnostics().contains("Alice Personal Phone"), "diagnostics redact raw device name");
        assertFalse(result.diagnostics().contains("9, 8, 7"), "diagnostics omit audio payload");
    }

    private static void jitterBufferPlaysInOrderFrames() {
        JitterBuffer buffer = JitterBuffer.withCapacity(3);
        buffer.offer(AudioFrame.pcm(1L, 1_000L, new byte[] {1}));
        buffer.offer(AudioFrame.pcm(2L, 1_020L, new byte[] {2}));

        assertByteArrayEquals(new byte[] {1}, buffer.poll().payload(), "first in-order frame");
        assertByteArrayEquals(new byte[] {2}, buffer.poll().payload(), "second in-order frame");
        assertEquals(0L, buffer.snapshot().droppedFrames(), "no drops in-order");
    }

    private static void jitterBufferReordersOutOfOrderFrames() {
        JitterBuffer buffer = JitterBuffer.withCapacity(4);
        buffer.offer(AudioFrame.pcm(3L, 1_040L, new byte[] {3}));
        buffer.offer(AudioFrame.pcm(1L, 1_000L, new byte[] {1}));
        buffer.offer(AudioFrame.pcm(2L, 1_020L, new byte[] {2}));

        assertEquals(1L, buffer.poll().sequenceNumber(), "first reordered frame");
        assertEquals(2L, buffer.poll().sequenceNumber(), "second reordered frame");
        assertEquals(3L, buffer.poll().sequenceNumber(), "third reordered frame");
    }

    private static void jitterBufferDropsDuplicatesAndOldFrames() {
        JitterBuffer buffer = JitterBuffer.withCapacity(4);
        buffer.offer(AudioFrame.pcm(1L, 1_000L, new byte[] {1}));
        buffer.offer(AudioFrame.pcm(1L, 1_000L, new byte[] {9}));
        assertEquals(1L, buffer.poll().sequenceNumber(), "first unique frame");

        buffer.offer(AudioFrame.pcm(1L, 1_000L, new byte[] {1}));

        JitterBufferSnapshot snapshot = buffer.snapshot();
        assertEquals(2L, snapshot.droppedFrames(), "duplicate and old frame drops");
        assertEquals(0, snapshot.bufferedFrames(), "old frame not buffered");
    }

    private static void jitterBufferReportsUnderrunsAndCapsMemory() {
        JitterBuffer buffer = JitterBuffer.withCapacity(2);
        assertEquals(null, buffer.poll(), "empty poll underrun result");

        buffer.offer(AudioFrame.pcm(10L, 2_000L, new byte[] {10}));
        buffer.offer(AudioFrame.pcm(11L, 2_020L, new byte[] {11}));
        buffer.offer(AudioFrame.pcm(12L, 2_040L, new byte[] {12}));

        JitterBufferSnapshot snapshot = buffer.snapshot();
        assertEquals(1L, snapshot.underruns(), "one underrun recorded");
        assertEquals(1L, snapshot.droppedFrames(), "capacity overflow drop recorded");
        assertEquals(2, snapshot.bufferedFrames(), "buffer capped at capacity");
        assertEquals(11L, buffer.poll().sequenceNumber(), "oldest frame dropped on overflow");
    }

    private static void operationModesTuneScanHeartbeatAndBufferSettings() {
        OperationModePolicy performance = OperationModePolicy.forMode(OperationMode.PERFORMANCE);
        OperationModePolicy balanced = OperationModePolicy.forMode(OperationMode.BALANCED);
        OperationModePolicy powerSaver = OperationModePolicy.forMode(OperationMode.POWER_SAVER);

        assertTrue(performance.scanWindowMillis() < balanced.scanWindowMillis(), "performance scan should be shorter than balanced");
        assertTrue(powerSaver.heartbeatIntervalMillis() > balanced.heartbeatIntervalMillis(), "power saver heartbeat should be slower");
        assertTrue(performance.jitterBufferCapacityFrames() >= balanced.jitterBufferCapacityFrames(), "performance keeps enough buffer capacity");
        assertFalse(powerSaver.autoReconnectAllowed(), "MVP modes must not enable auto reconnect");
    }

    private static void diagnosticModeKeepsShortHeartbeatAndVerboseDiagnostics() {
        OperationModePolicy diagnostic = OperationModePolicy.forMode(OperationMode.DIAGNOSTIC);

        assertTrue(diagnostic.verboseDiagnostics(), "diagnostic mode enables verbose diagnostics");
        assertTrue(diagnostic.heartbeatIntervalMillis() <= 1_000L, "diagnostic heartbeat stays short for connection testing");
        assertEquals(2, diagnostic.disconnectAfterMissedHeartbeats(), "diagnostic mode disconnect threshold");
    }

    private static void connectionHealthIsStableWithRecentPong() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        ConnectionHealthMonitor monitor = ConnectionHealthMonitor.startedAt(10_000L, policy);

        monitor.recordPong(10_500L);

        assertEquals(ConnectionHealth.STABLE, monitor.healthAt(11_000L), "recent pong should be stable");
        assertEquals(0L, monitor.missedHeartbeatWindowsAt(11_000L), "no missed heartbeats");
    }

    private static void connectionHealthDegradesAndDisconnectsAfterMissedHeartbeatWindows() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.DIAGNOSTIC);
        ConnectionHealthMonitor monitor = ConnectionHealthMonitor.startedAt(0L, policy);

        assertEquals(ConnectionHealth.DEGRADED, monitor.healthAt(policy.heartbeatIntervalMillis() + 1L), "one missed heartbeat degrades");
        assertEquals(ConnectionHealth.DISCONNECTED, monitor.healthAt((policy.heartbeatIntervalMillis() * 2L) + 1L), "two missed heartbeats disconnect");
    }

    private static void userJourneyTracksClickBudgetForHappyPath() {
        UserJourneyResult result = UserJourney.onAndroid(33)
            .grant(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT, AndroidPermission.RECORD_AUDIO)
            .scanForPeers()
            .selectPeer("peer-a")
            .connected("peer-a")
            .pressToTalk()
            .releaseToListen()
            .finish();

        assertTrue(result.userActions() <= 5, "happy path should stay within click/action budget");
        assertContains(result.timeline(), "action:press_to_talk", "click timeline tracks PTT press");
    }

    private static void fakeTransportExchangesPingPongFrames() {
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, OperationModePolicy.forMode(OperationMode.DIAGNOSTIC));

        TransportSessionResult result = transport.sendPing(1L, 100L).receivePong(1L, 150L).snapshotAt(200L);

        assertEquals(ConnectionHealth.STABLE, result.health(), "ping-pong keeps connection stable");
        assertEquals(1L, result.framesSent(), "one ping sent");
        assertEquals(1L, result.framesReceived(), "one pong received");
        assertContains(result.timeline(), "send:PING#1", "ping timeline");
        assertContains(result.timeline(), "receive:PONG#1", "pong timeline");
    }

    private static void fakeTransportReportsConnectionHealthAfterMissedPongs() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.DIAGNOSTIC);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, policy);

        TransportSessionResult result = transport.sendPing(1L, 0L).snapshotAt((policy.heartbeatIntervalMillis() * 2L) + 1L);

        assertEquals(ConnectionHealth.DISCONNECTED, result.health(), "missed pongs disconnect fake transport");
        assertTrue(result.missedHeartbeatWindows() >= 2L, "missed heartbeat windows tracked");
    }

    private static void fakeTransportRejectsSendWhenDisconnected() {
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, OperationModePolicy.forMode(OperationMode.BALANCED));

        TransportSessionResult result = transport.disconnect("user_left").sendPing(1L, 10L).snapshotAt(20L);

        assertEquals(ConnectionHealth.DISCONNECTED, result.health(), "disconnected transport stays disconnected");
        assertEquals(0L, result.framesSent(), "send after disconnect is rejected");
        assertEquals(1L, result.sendFailures(), "send failure counted");
        assertContains(result.timeline(), "send_failed:not_connected", "send failure timeline");
    }

    private static void streamReaderDecodesFrameSplitAcrossReads() {
        ProtocolFrame frame = new ProtocolFrame(FrameType.PING, 77L, 7_000L, new byte[] {7, 7});
        byte[] encoded = ProtocolFrameCodec.encode(frame);
        ProtocolStreamReader reader = new ProtocolStreamReader(new ChunkedInputStream(encoded, 3));

        ProtocolFrame decoded = reader.readNextFrame();

        assertEquals(frame, decoded, "split stream frame decode");
    }

    private static void streamReaderDecodesConcatenatedFramesSequentially() {
        ProtocolFrame first = new ProtocolFrame(FrameType.PING, 1L, 100L, new byte[] {1});
        ProtocolFrame second = new ProtocolFrame(FrameType.PONG, 2L, 120L, new byte[] {2});
        byte[] bytes = concat(ProtocolFrameCodec.encode(first), ProtocolFrameCodec.encode(second));
        ProtocolStreamReader reader = new ProtocolStreamReader(new ByteArrayInputStream(bytes));

        assertEquals(first, reader.readNextFrame(), "first stream frame");
        assertEquals(second, reader.readNextFrame(), "second stream frame");
    }

    private static void voiceTransportInterfaceIsImplementedByFakeTransport() {
        VoiceTransport transport = FakeVoiceTransport.connectedAt(0L, OperationModePolicy.forMode(OperationMode.BALANCED));

        TransportSessionResult result = transport.sendPing(1L, 0L).receivePong(1L, 100L).snapshotAt(200L);

        assertEquals(ConnectionHealth.STABLE, result.health(), "voice transport fake remains stable");
    }

    private static void streamReaderRejectsEofDuringPayload() {
        ProtocolFrame frame = new ProtocolFrame(FrameType.AUDIO, 9L, 900L, new byte[] {1, 2, 3, 4});
        byte[] encoded = ProtocolFrameCodec.encode(frame);
        byte[] truncated = Arrays.copyOf(encoded, encoded.length - 2);
        ProtocolStreamReader reader = new ProtocolStreamReader(new ByteArrayInputStream(truncated));

        assertThrows(ProtocolException.IncompleteFrame.class, reader::readNextFrame);
    }

    private static void streamReaderRejectsOversizedPayloadBeforeAllocatingPayload() {
        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.AUDIO, 9L, 900L, new byte[0]));
        writeInt(encoded, ProtocolFrameCodec.PAYLOAD_LENGTH_OFFSET, ProtocolFrameCodec.MAX_PAYLOAD_BYTES + 1);
        ProtocolStreamReader reader = new ProtocolStreamReader(new ByteArrayInputStream(encoded));

        assertThrows(ProtocolException.PayloadTooLarge.class, reader::readNextFrame);
    }

    private static void streamReaderRejectsInvalidMagicFromStream() {
        byte[] encoded = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.PING, 1L, 100L, new byte[0]));
        encoded[0] = 0x00;
        ProtocolStreamReader reader = new ProtocolStreamReader(new ByteArrayInputStream(encoded));

        assertThrows(ProtocolException.InvalidMagic.class, reader::readNextFrame);
    }

    private static void streamReaderMapsIoFailureToProtocolException() {
        ProtocolStreamReader reader = new ProtocolStreamReader(new FailingInputStream("socket reset"));

        ProtocolException.StreamReadFailure failure = assertThrows(ProtocolException.StreamReadFailure.class, reader::readNextFrame);

        assertContains(failure.getMessage(), "socket reset", "IO failure reason is preserved without leaking bytes");
        assertTrue(failure.recoverable(), "IO failure is recoverable so callers can reconnect");
    }

    private static void fakeTransportReconnectRequiresExplicitUserIntent() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, policy);

        TransportSessionResult afterAutoTick = transport
            .disconnect("link_loss")
            .autoReconnectTick(100L)
            .snapshotAt(200L);
        assertEquals(ConnectionHealth.DISCONNECTED, afterAutoTick.health(), "auto reconnect tick stays disconnected");
        assertContains(afterAutoTick.timeline(), "auto_reconnect_ignored:requires_user_action", "auto reconnect ignored timeline");

        TransportSessionResult afterUserReconnect = transport
            .userRequestedReconnect(300L)
            .sendPing(2L, 350L)
            .receivePong(2L, 400L)
            .snapshotAt(450L);

        assertEquals(ConnectionHealth.STABLE, afterUserReconnect.health(), "user reconnect restores stable transport");
        assertContains(afterUserReconnect.timeline(), "reconnect:user_requested@300", "user reconnect timeline");
    }


    private static void bluetoothEnvironmentFakeReportsPowerPermissionAndPairingStates() {
        BluetoothEnvironment environment = FakeBluetoothEnvironment.create()
            .withBluetoothEnabled(false)
            .withNearbyPermission(false);

        assertEquals(BluetoothEnvironmentState.BLUETOOTH_OFF, environment.state(), "bluetooth off state");

        environment = FakeBluetoothEnvironment.create()
            .withBluetoothEnabled(true)
            .withNearbyPermission(false);
        assertEquals(BluetoothEnvironmentState.MISSING_PERMISSION, environment.state(), "missing permission state");

        environment = FakeBluetoothEnvironment.create()
            .withBluetoothEnabled(true)
            .withNearbyPermission(true)
            .withPairedPeer("peer-a");
        assertEquals(BluetoothEnvironmentState.READY, environment.state(), "ready state");
        assertTrue(environment.isPaired("peer-a"), "paired peer visible through seam");
    }

    private static void audioInputFakeCapturesOnlyWhenStarted() {
        AudioInput input = FakeAudioInput.withFrames(
            AudioFrame.pcm(1L, 1_000L, new byte[] {1}),
            AudioFrame.pcm(2L, 1_020L, new byte[] {2})
        );

        assertEquals(null, input.readFrame(), "inactive input returns no frame");
        input.start();
        assertEquals(1L, input.readFrame().sequenceNumber(), "first captured frame");
        input.stop();
        assertEquals(null, input.readFrame(), "stopped input returns no frame");
    }

    private static void audioOutputFakeUsesJitterBufferForPlaybackOrder() {
        AudioOutput output = FakeAudioOutput.withJitterCapacity(3);
        output.enqueue(AudioFrame.pcm(3L, 1_040L, new byte[] {3}));
        output.enqueue(AudioFrame.pcm(1L, 1_000L, new byte[] {1}));
        output.enqueue(AudioFrame.pcm(2L, 1_020L, new byte[] {2}));

        assertEquals(1L, output.playNext().sequenceNumber(), "first output frame reordered");
        assertEquals(2L, output.playNext().sequenceNumber(), "second output frame reordered");
        assertEquals(3L, output.playNext().sequenceNumber(), "third output frame reordered");
        assertEquals(3L, output.snapshot().playedFrames(), "played counter exposed");
    }

    private static void fakeTransportDiscoversOnlyReadyPairedPeers() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, policy);
        BluetoothEnvironment blocked = FakeBluetoothEnvironment.create().withNearbyPermission(false);

        TransportSessionResult blockedResult = transport.discoverPairedPeer("peer-a", blocked, 10L).snapshotAt(20L);

        assertFalse(blockedResult.peerDiscovered(), "missing permission prevents discovery");
        assertContains(blockedResult.timeline(), "discover_blocked:MISSING_PERMISSION", "blocked discovery timeline");

        BluetoothEnvironment ready = FakeBluetoothEnvironment.create().withPairedPeer("peer-a");
        TransportSessionResult readyResult = transport.discoverPairedPeer("peer-a", ready, 30L).snapshotAt(40L);

        assertTrue(readyResult.peerDiscovered(), "ready paired peer discovered");
        assertContains(readyResult.timeline(), "discover:paired_peer@30", "paired discovery timeline avoids raw peer id");
    }

    private static void fakeTransportSendsAndReceivesAudioFramesWithoutPayloadInTimeline() {
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, OperationModePolicy.forMode(OperationMode.BALANCED));
        AudioFrame outbound = AudioFrame.pcm(7L, 700L, new byte[] {11, 12, 13});
        AudioFrame inbound = AudioFrame.pcm(8L, 720L, new byte[] {21, 22});

        TransportSessionResult result = transport
            .sendAudioFrame(outbound)
            .receiveAudioFrame(inbound)
            .snapshotAt(800L);

        assertEquals(1L, result.audioFramesSent(), "audio send counter");
        assertEquals(1L, result.audioFramesReceived(), "audio receive counter");
        assertEquals(1L, result.framesSent(), "generic sent counter includes audio frame");
        assertEquals(1L, result.framesReceived(), "generic received counter includes audio");
        assertContains(result.timeline(), "send:AUDIO#7 bytes=3", "audio send timeline includes metadata");
        assertContains(result.timeline(), "receive:AUDIO#8 bytes=2", "audio receive timeline includes metadata");
        assertFalse(result.timeline().contains("11, 12, 13"), "timeline must not log raw audio payload");
    }

    private static void talkScreenBlocksPushToTalkUntilNearbyPermissionIsGranted() {
        TalkScreenState state = TalkScreenPresenter.render(
            PttState.IDLE,
            PermissionAction.REQUEST_NEARBY_DEVICES,
            BluetoothEnvironmentState.READY,
            ConnectionHealth.STABLE,
            1,
            3
        );

        assertEquals("Enable Nearby devices", state.statusTitle(), "permission block title");
        assertEquals("Grant Nearby devices", state.primaryActionLabel(), "permission action label");
        assertFalse(state.pushToTalkEnabled(), "PTT disabled without nearby permission");
        assertTrue(state.withinClickBudget(), "one action stays within budget");
        assertContains(state.acceptanceCriteria(), "permission", "UI acceptance criteria names permission gate");
        assertContains(state.rollbackTrigger(), "more than 3 actions", "rollback trigger exposes click budget");
        assertEquals("ui_blocked:REQUEST_NEARBY_DEVICES", state.diagnosticsSignal(), "permission diagnostics signal");
    }

    private static void talkScreenEnablesPushToTalkOnlyWhenConnectedAndStable() {
        TalkScreenState state = TalkScreenPresenter.render(
            PttState.CONNECTED,
            PermissionAction.ALLOW,
            BluetoothEnvironmentState.READY,
            ConnectionHealth.STABLE,
            2,
            3
        );

        assertEquals("Ready. Hold to talk.", state.statusTitle(), "connected stable title");
        assertEquals("Hold to talk", state.primaryActionLabel(), "connected primary action");
        assertTrue(state.pushToTalkEnabled(), "PTT enabled only when permission, bluetooth, state, and health are ready");
        assertFalse(state.diagnosticsVisible(), "diagnostics hidden for healthy ready state");
        assertEquals("ui_ready:ptt_enabled", state.diagnosticsSignal(), "ready diagnostics signal");
    }

    private static void talkScreenShowsReconnectAndDiagnosticsWhenDisconnected() {
        TalkScreenState state = TalkScreenPresenter.render(
            PttState.DISCONNECTED,
            PermissionAction.ALLOW,
            BluetoothEnvironmentState.READY,
            ConnectionHealth.DISCONNECTED,
            4,
            3
        );

        assertEquals("Disconnected", state.statusTitle(), "disconnected title");
        assertEquals("Reconnect", state.primaryActionLabel(), "disconnected action");
        assertFalse(state.pushToTalkEnabled(), "PTT disabled while disconnected");
        assertTrue(state.diagnosticsVisible(), "diagnostics visible on disconnect");
        assertFalse(state.withinClickBudget(), "four actions exceeds three action budget");
        assertContains(state.rollbackTrigger(), "more than 3 actions", "rollback trigger calls out click budget");
        assertEquals("ui_reconnect:DISCONNECTED", state.diagnosticsSignal(), "disconnect diagnostics signal");
    }

    private static void heartbeatControllerUsesInjectedClockWithoutRealDelays() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeClock clock = FakeClock.at(1_000L);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(clock.nowMillis(), policy);
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        TransportHeartbeatController controller = TransportHeartbeatController.start(transport, policy, clock, diagnostics);

        controller.poll();
        clock.advanceMillis(policy.heartbeatIntervalMillis() - 1L);
        controller.poll();
        clock.advanceMillis(1L);
        controller.poll();

        TransportSessionResult result = transport.snapshotAt(clock.nowMillis());
        assertEquals(2L, result.framesSent(), "first and interval heartbeat sent");
        assertContains(result.timeline(), "send:PING#1", "first heartbeat sequence");
        assertContains(result.timeline(), "send:PING#2", "second heartbeat sequence");
        assertContains(diagnostics.exportRedacted(), "TransportHeartbeat(sequenceNumber=2", "heartbeat diagnostics include sequence metadata");
    }

    private static void heartbeatControllerReportsMissedHeartbeatDisconnectWithoutPeerLeak() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.DIAGNOSTIC);
        FakeClock clock = FakeClock.at(0L);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(clock.nowMillis(), policy);
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        TransportHeartbeatController controller = TransportHeartbeatController.start(transport, policy, clock, diagnostics);

        controller.poll();
        clock.advanceMillis((policy.heartbeatIntervalMillis() * policy.disconnectAfterMissedHeartbeats()) + 1L);
        controller.poll();

        String exported = diagnostics.exportRedacted();
        assertContains(exported, "TransportStateChanged(CONNECTED->DISCONNECTED, reason=heartbeat_missed)", "disconnect diagnostics signal");
        assertFalse(exported.contains("AA:BB:CC:DD:EE:FF"), "heartbeat diagnostics must not contain raw peer identifiers");
        assertEquals(ConnectionHealth.DISCONNECTED, transport.snapshotAt(clock.nowMillis()).health(), "transport health disconnected after missed heartbeat windows");
    }

    private static void discoveryControllerStopsAfterConfiguredScanWindowWithoutRealDelay() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeClock clock = FakeClock.at(5_000L);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(clock.nowMillis(), policy);
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        BluetoothEnvironment environment = FakeBluetoothEnvironment.create().withPairedPeer("AA:BB:CC:DD:EE:FF");
        PeerDiscoveryController controller = PeerDiscoveryController.start(transport, policy, clock, diagnostics);

        PeerDiscoverySnapshot started = controller.scanForPairedPeer("AA:BB:CC:DD:EE:FF", environment);
        clock.advanceMillis(policy.scanWindowMillis() - 1L);
        PeerDiscoverySnapshot beforeTimeout = controller.poll();
        clock.advanceMillis(1L);
        PeerDiscoverySnapshot timedOut = controller.poll();

        assertTrue(started.active(), "scan starts active");
        assertTrue(started.peerDiscovered(), "paired peer discovered through transport seam");
        assertTrue(beforeTimeout.active(), "scan remains active before scan window elapses");
        assertFalse(timedOut.active(), "scan stops at scan window boundary");
        assertEquals("scan_window_elapsed", timedOut.reason(), "scan timeout reason");
        assertContains(diagnostics.exportRedacted(), "PeerDiscovery(state=stopped, reason=scan_window_elapsed", "scan timeout diagnostics");
    }

    private static void discoveryControllerBlocksMissingPermissionAndRedactsPeerIdentifier() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeClock clock = FakeClock.at(0L);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(clock.nowMillis(), policy);
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        BluetoothEnvironment environment = FakeBluetoothEnvironment.create().withNearbyPermission(false);
        PeerDiscoveryController controller = PeerDiscoveryController.start(transport, policy, clock, diagnostics);

        PeerDiscoverySnapshot snapshot = controller.scanForPairedPeer("AA:BB:CC:DD:EE:FF", environment);
        String exported = diagnostics.exportRedacted();

        assertFalse(snapshot.active(), "permission-blocked scan is not active");
        assertFalse(snapshot.peerDiscovered(), "permission-blocked scan does not discover peer");
        assertEquals("MISSING_PERMISSION", snapshot.reason(), "missing permission reason");
        assertContains(exported, "PeerDiscovery(state=blocked, reason=MISSING_PERMISSION", "blocked discovery diagnostics");
        assertFalse(exported.contains("AA:BB:CC:DD:EE:FF"), "discovery diagnostics must not contain raw peer identifiers");
    }

    private static void streamVoiceTransportWritesPingFrameToOutputStream() {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        StreamVoiceTransport transport = StreamVoiceTransport.connected(
            new ByteArrayInputStream(new byte[0]),
            output,
            0L,
            OperationModePolicy.forMode(OperationMode.BALANCED)
        );

        TransportSessionResult result = transport.sendPing(42L, 1_000L).snapshotAt(1_000L);
        ProtocolFrame written = ProtocolFrameCodec.decode(output.toByteArray());

        assertEquals(FrameType.PING, written.type(), "stream transport writes ping type");
        assertEquals(42L, written.sequenceNumber(), "stream transport writes ping sequence");
        assertEquals(1L, result.framesSent(), "stream transport sent counter");
        assertContains(result.timeline(), "write:PING#42", "stream transport write timeline");
    }

    private static void streamVoiceTransportReadsPongAndAudioMetadataWithoutPayloadInTimeline() {
        byte[] pong = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.PONG, 7L, 700L, new byte[0]));
        byte[] audio = ProtocolFrameCodec.encode(new ProtocolFrame(FrameType.AUDIO, 8L, 720L, new byte[] {11, 12, 13}));
        StreamVoiceTransport transport = StreamVoiceTransport.connected(
            new ByteArrayInputStream(concat(pong, audio)),
            new java.io.ByteArrayOutputStream(),
            0L,
            OperationModePolicy.forMode(OperationMode.BALANCED)
        );

        transport.readNextIncomingFrame();
        TransportSessionResult afterPong = transport.readNextIncomingFrame().snapshotAt(800L);

        assertEquals(2L, afterPong.framesReceived(), "stream transport received counter");
        assertEquals(1L, afterPong.audioFramesReceived(), "stream transport audio received counter");
        assertEquals(ConnectionHealth.STABLE, afterPong.health(), "pong updates stream transport health");
        assertContains(afterPong.timeline(), "read:PONG#7", "pong read timeline");
        assertContains(afterPong.timeline(), "read:AUDIO#8 bytes=3", "audio read timeline metadata");
        assertFalse(afterPong.timeline().contains("11, 12, 13"), "stream transport timeline must not log raw audio payload");
    }

    private static void streamVoiceTransportMapsWriteFailureToSendFailure() {
        StreamVoiceTransport transport = StreamVoiceTransport.connected(
            new ByteArrayInputStream(new byte[0]),
            new FailingOutputStream("socket closed"),
            0L,
            OperationModePolicy.forMode(OperationMode.BALANCED)
        );

        TransportSessionResult result = transport.sendPing(1L, 100L).snapshotAt(100L);

        assertEquals(0L, result.framesSent(), "failed stream write does not count as sent");
        assertEquals(1L, result.sendFailures(), "failed stream write increments send failures");
        assertContains(result.timeline(), "write_failed:socket closed", "write failure reason timeline");
    }

    private static void pttAudioControllerStartsCaptureOnlyWhenConnectedAndSendsFrames() {
        FakeAudioInput input = FakeAudioInput.withFrames(
            AudioFrame.pcm(1L, 1_000L, new byte[] {1}),
            AudioFrame.pcm(2L, 1_020L, new byte[] {2}),
            AudioFrame.pcm(3L, 1_040L, new byte[] {3})
        );
        FakeAudioOutput output = FakeAudioOutput.withJitterCapacity(3);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, OperationModePolicy.forMode(OperationMode.BALANCED));
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        PttAudioController controller = PttAudioController.connected(
            PeerId.of("peer-a"),
            input,
            output,
            transport,
            diagnostics
        );

        PttAudioSnapshot afterPress = controller.localPttPressed();
        controller.pumpCapturedFrame();
        controller.pumpCapturedFrame();
        PttAudioSnapshot afterRelease = controller.localPttReleased();
        controller.pumpCapturedFrame();

        TransportSessionResult result = transport.snapshotAt(2_000L);
        assertEquals(PttState.TRANSMITTING, afterPress.state(), "PTT audio starts transmitting");
        assertEquals(PttState.CONNECTED, afterRelease.state(), "PTT audio returns to connected after release");
        assertEquals(2L, result.audioFramesSent(), "only frames captured while transmitting are sent");
        assertContains(diagnostics.exportRedacted(), "AudioPipeline(state=transmitting", "audio pipeline transmitting diagnostics");
        assertContains(diagnostics.exportRedacted(), "AudioPipeline(state=connected, reason=local_ptt_released", "audio pipeline release diagnostics");
    }

    private static void pttAudioControllerRoutesRemoteFramesThroughJitterWithoutPayloadDiagnostics() {
        FakeAudioOutput output = FakeAudioOutput.withJitterCapacity(3);
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        PttAudioController controller = PttAudioController.connected(
            PeerId.of("peer-a"),
            FakeAudioInput.withFrames(),
            output,
            FakeVoiceTransport.connectedAt(0L, OperationModePolicy.forMode(OperationMode.BALANCED)),
            diagnostics
        );

        PttAudioSnapshot snapshot = controller.remoteFrameReceived("AA:BB:CC:DD:EE:FF", "Alice Personal Phone", AudioFrame.pcm(9L, 9_000L, new byte[] {11, 12}));
        AudioFrame played = output.playNext();
        String exported = diagnostics.exportRedacted();

        assertEquals(PttState.RECEIVING, snapshot.state(), "remote audio moves controller to receiving");
        assertEquals(9L, played.sequenceNumber(), "remote audio enqueued through jitter output");
        assertContains(exported, "AudioFrameReceived(peerHash=", "remote audio diagnostics redacts peer id");
        assertContains(exported, "payloadBytes=2", "remote audio diagnostics keeps payload length");
        assertFalse(exported.contains("AA:BB:CC:DD:EE:FF"), "remote diagnostics must not include raw peer id");
        assertFalse(exported.contains("Alice Personal Phone"), "remote diagnostics must not include raw device name");
        assertFalse(exported.contains("11, 12"), "remote diagnostics must not include raw payload");
    }

    private static void pttAudioControllerDoesNotCaptureWhenRemoteTalkingBusy() {
        FakeAudioInput input = FakeAudioInput.withFrames(AudioFrame.pcm(1L, 1_000L, new byte[] {1}));
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, OperationModePolicy.forMode(OperationMode.BALANCED));
        PttAudioController controller = PttAudioController.connected(
            PeerId.of("peer-a"),
            input,
            FakeAudioOutput.withJitterCapacity(3),
            transport,
            RecordingDiagnosticsSink.create()
        );

        controller.remoteFrameReceived("peer-a", "Peer", AudioFrame.pcm(10L, 10_000L, new byte[] {10}));
        PttAudioSnapshot busy = controller.localPttPressed();
        controller.pumpCapturedFrame();

        assertEquals(PttState.BUSY, busy.state(), "local press during remote receive becomes busy");
        assertEquals(0L, transport.snapshotAt(2_000L).audioFramesSent(), "busy state does not capture or send local audio");
    }

    private static void sessionControllerBlocksScanUntilNearbyPermissionGranted() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        WalkieTalkieSessionController session = WalkieTalkieSessionController.create(
            PlatformVersion.androidApi(33),
            PermissionGrantState.none(),
            FakeBluetoothEnvironment.create().withPairedPeer("AA:BB:CC:DD:EE:FF"),
            FakeVoiceTransport.connectedAt(0L, policy),
            FakeClock.at(0L),
            policy,
            diagnostics,
            3
        );

        WalkieTalkieSessionSnapshot snapshot = session.scanForPeer("AA:BB:CC:DD:EE:FF");

        assertEquals(PttState.IDLE, snapshot.pttState(), "permission-blocked scan stays idle");
        assertEquals(PermissionAction.REQUEST_NEARBY_DEVICES, snapshot.permissionAction(), "permission-blocked scan requests nearby devices");
        assertEquals("Enable Nearby devices", snapshot.screen().statusTitle(), "permission-blocked screen title");
        assertFalse(snapshot.discovery().active(), "permission-blocked session discovery inactive");
        assertFalse(snapshot.diagnostics().contains("AA:BB:CC:DD:EE:FF"), "session diagnostics must not leak raw peer id");
    }

    private static void sessionControllerConnectsAndTransmitsWithinClickBudgetWithoutPeerLeak() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        RecordingDiagnosticsSink diagnostics = RecordingDiagnosticsSink.create();
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, policy);
        WalkieTalkieSessionController session = WalkieTalkieSessionController.create(
            PlatformVersion.androidApi(33),
            PermissionGrantState.granted(
                AndroidPermission.BLUETOOTH_SCAN,
                AndroidPermission.BLUETOOTH_CONNECT,
                AndroidPermission.RECORD_AUDIO
            ),
            FakeBluetoothEnvironment.create().withPairedPeer("AA:BB:CC:DD:EE:FF"),
            transport,
            FakeClock.at(0L),
            policy,
            diagnostics,
            4
        );

        session.scanForPeer("AA:BB:CC:DD:EE:FF");
        session.connectToPeer("AA:BB:CC:DD:EE:FF");
        WalkieTalkieSessionSnapshot transmitting = session.pressToTalk();
        WalkieTalkieSessionSnapshot connected = session.releaseToListen();

        assertEquals(PttState.TRANSMITTING, transmitting.pttState(), "session enters transmitting");
        assertEquals(PttState.CONNECTED, connected.pttState(), "session returns to connected");
        assertTrue(connected.screen().withinClickBudget(), "session stays within click budget");
        assertEquals("Ready. Hold to talk.", connected.screen().statusTitle(), "connected session screen title");
        assertContains(connected.diagnostics(), "PeerDiscovery(state=started, reason=paired_peer", "session discovery diagnostics");
        assertFalse(connected.diagnostics().contains("AA:BB:CC:DD:EE:FF"), "session diagnostics must not leak raw peer id");
    }

    private static void sessionControllerSendsAudioOnlyWhilePressed() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, policy);
        WalkieTalkieSessionController session = WalkieTalkieSessionController.createWithAudio(
            PlatformVersion.androidApi(33),
            PermissionGrantState.granted(
                AndroidPermission.BLUETOOTH_SCAN,
                AndroidPermission.BLUETOOTH_CONNECT,
                AndroidPermission.RECORD_AUDIO
            ),
            FakeBluetoothEnvironment.create().withPairedPeer("peer-a"),
            transport,
            FakeClock.at(0L),
            policy,
            RecordingDiagnosticsSink.create(),
            5,
            FakeAudioInput.withFrames(
                AudioFrame.pcm(1L, 1_000L, new byte[] {1}),
                AudioFrame.pcm(2L, 1_020L, new byte[] {2}),
                AudioFrame.pcm(3L, 1_040L, new byte[] {3})
            ),
            FakeAudioOutput.withJitterCapacity(3)
        );

        session.scanForPeer("peer-a");
        session.connectToPeer("peer-a");
        session.pressToTalk();
        session.pumpLocalAudioFrame();
        session.pumpLocalAudioFrame();
        WalkieTalkieSessionSnapshot released = session.releaseToListen();
        WalkieTalkieSessionSnapshot afterExtraPump = session.pumpLocalAudioFrame();

        assertEquals(PttState.CONNECTED, released.pttState(), "session audio returns connected after release");
        assertEquals(2L, afterExtraPump.audio().localFramesSent(), "session audio sends only while pressed");
        assertEquals(2L, transport.snapshotAt(2_000L).audioFramesSent(), "transport sees two local audio frames");
        assertContains(afterExtraPump.diagnostics(), "AudioPipeline(state=connected, reason=local_ptt_released", "session exposes redacted audio release diagnostics");
    }

    private static void sessionControllerRoutesRemoteAudioThroughOutputAndRedactsDiagnostics() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeAudioOutput output = FakeAudioOutput.withJitterCapacity(3);
        WalkieTalkieSessionController session = WalkieTalkieSessionController.createWithAudio(
            PlatformVersion.androidApi(33),
            PermissionGrantState.granted(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT),
            FakeBluetoothEnvironment.create().withPairedPeer("AA:BB:CC:DD:EE:FF"),
            FakeVoiceTransport.connectedAt(0L, policy),
            FakeClock.at(0L),
            policy,
            RecordingDiagnosticsSink.create(),
            5,
            FakeAudioInput.withFrames(),
            output
        );

        session.scanForPeer("AA:BB:CC:DD:EE:FF");
        session.connectToPeer("AA:BB:CC:DD:EE:FF");
        WalkieTalkieSessionSnapshot receiving = session.remoteAudioFrameReceived(
            "AA:BB:CC:DD:EE:FF",
            "Alice Personal Phone",
            AudioFrame.pcm(7L, 7_000L, new byte[] {11, 12, 13})
        );
        AudioFrame played = output.playNext();

        assertEquals(PttState.RECEIVING, receiving.pttState(), "session moves to receiving on remote audio");
        assertEquals(1L, receiving.audio().remoteFramesReceived(), "session remote audio counter");
        assertEquals(7L, played.sequenceNumber(), "session routes remote audio through output");
        assertContains(receiving.diagnostics(), "AudioFrameReceived(peerHash=", "session redacts remote audio peer id");
        assertFalse(receiving.diagnostics().contains("AA:BB:CC:DD:EE:FF"), "session remote diagnostics must not leak raw peer id");
        assertFalse(receiving.diagnostics().contains("Alice Personal Phone"), "session remote diagnostics must not leak device name");
        assertFalse(receiving.diagnostics().contains("11, 12, 13"), "session remote diagnostics must not leak audio payload");
    }

    private static void sessionControllerIgnoresRemoteAudioBeforeConnection() {
        OperationModePolicy policy = OperationModePolicy.forMode(OperationMode.BALANCED);
        FakeAudioOutput output = FakeAudioOutput.withJitterCapacity(3);
        FakeVoiceTransport transport = FakeVoiceTransport.connectedAt(0L, policy);
        WalkieTalkieSessionController session = WalkieTalkieSessionController.createWithAudio(
            PlatformVersion.androidApi(33),
            PermissionGrantState.granted(AndroidPermission.BLUETOOTH_SCAN, AndroidPermission.BLUETOOTH_CONNECT),
            FakeBluetoothEnvironment.create().withPairedPeer("peer-a"),
            transport,
            FakeClock.at(0L),
            policy,
            RecordingDiagnosticsSink.create(),
            5,
            FakeAudioInput.withFrames(),
            output
        );

        WalkieTalkieSessionSnapshot ignored = session.remoteAudioFrameReceived(
            "peer-a",
            "Alice Personal Phone",
            AudioFrame.pcm(9L, 9_000L, new byte[] {42})
        );

        assertEquals(PttState.IDLE, ignored.pttState(), "remote audio before connection keeps session idle");
        assertEquals(0L, ignored.audio().remoteFramesReceived(), "remote audio before connection is not counted");
        assertEquals(0L, transport.snapshotAt(2_000L).audioFramesReceived(), "transport ignores pre-connection remote audio");
        assertEquals(null, output.playNext(), "remote audio before connection is not enqueued for playback");
        assertFalse(ignored.diagnostics().contains("AudioFrameReceived"), "ignored pre-connection audio is not logged as received");
        assertFalse(ignored.diagnostics().contains("Alice Personal Phone"), "ignored pre-connection diagnostics must not leak device name");
    }

    private static void simulationCatalogCoversOneHundredStakeholderScenarios() {
        List<SimulationScenario> scenarios = SimulationScenarioCatalog.all();

        assertEquals(100, scenarios.size(), "simulation catalog scenario count");
        assertEquals(100L, scenarios.stream().map(SimulationScenario::id).distinct().count(), "simulation ids must be unique");
        assertTrue(scenarios.stream().anyMatch(scenario -> scenario.id().equals("audio-remote-muted-forgotten")), "catalog includes muted remote scenario");
        assertTrue(scenarios.stream().anyMatch(scenario -> scenario.id().equals("transport-weak-signal-walk-away")), "catalog includes weak signal walk-away scenario");
        assertTrue(scenarios.stream().anyMatch(scenario -> scenario.category().equals("ui_ux")), "catalog includes UI/UX scenarios");
        assertTrue(scenarios.stream().allMatch(scenario -> !scenario.result().diagnostics().contains("AA:BB:CC:DD:EE:FF")), "scenario diagnostics redact raw peer ids");
        assertTrue(scenarios.stream().allMatch(scenario -> !scenario.result().diagnostics().contains("Alice Personal Phone")), "scenario diagnostics redact raw device names");
        assertTrue(scenarios.stream().allMatch(scenario -> !scenario.result().diagnostics().contains("11, 12, 13")), "scenario diagnostics redact raw payloads");
        assertTrue(scenarios.stream().allMatch(scenario -> !scenario.acceptanceCriteria().isBlank()), "each scenario has acceptance criteria");
        assertTrue(scenarios.stream().allMatch(scenario -> !scenario.rollbackTrigger().isBlank()), "each scenario has rollback trigger");
        assertTrue(scenarios.stream().allMatch(scenario -> !scenario.diagnosticsSignal().isBlank()), "each scenario has diagnostics signal");
    }

    private static void simulationScenarioReportExportsCatalogForQa() {
        String report = SimulationScenarioReportRenderer.renderMarkdown();

        assertContains(report, "# Simulation scenario catalog", "simulation report title");
        assertContains(report, "Total scenarios: 100", "simulation report count");
        assertContains(report, "audio-remote-muted-forgotten", "simulation report includes muted audio scenario");
        assertContains(report, "transport-weak-signal-walk-away", "simulation report includes weak signal scenario");
        assertContains(report, "ui_ux", "simulation report includes UI/UX category");
        assertFalse(report.contains("AA:BB:CC:DD:EE:FF"), "simulation report must not leak raw peer id");
        assertFalse(report.contains("Alice Personal Phone"), "simulation report must not leak device name");
        assertFalse(report.contains("11, 12, 13"), "simulation report must not leak audio payload");
    }

    private static void simulationScenarioReportWriterCreatesCiArtifact() {
        try {
            Path output = Files.createTempFile("walkitalki-simulations", ".md");
            SimulationScenarioReportWriter.main(new String[] {output.toString()});
            String report = Files.readString(output);

            assertContains(report, "# Simulation scenario catalog", "simulation artifact title");
            assertContains(report, "Total scenarios: 100", "simulation artifact count");
            assertContains(report, "transport-weak-signal-walk-away", "simulation artifact weak signal scenario");
        } catch (IOException exception) {
            throw new AssertionError("simulation scenario writer should create an artifact", exception);
        }
    }

    private static void readinessScorecardKeepsStakeholderAndModulePathsExecutable() {
        List<ReadinessScore> modules = ReadinessScorecard.modules();
        List<ReadinessScore> stakeholders = ReadinessScorecard.stakeholders();

        assertEquals(11, modules.size(), "module readiness score count");
        assertEquals(10, stakeholders.size(), "stakeholder readiness score count");
        assertTrue(modules.stream().allMatch(score -> score.score() >= 0 && score.score() <= 100), "module scores stay in 0..100");
        assertTrue(stakeholders.stream().allMatch(score -> score.score() >= 0 && score.score() <= 100), "stakeholder scores stay in 0..100");
        assertTrue(modules.stream().anyMatch(score -> score.name().equals("Android app module") && score.score() == 0), "Android app module is honestly scored zero until it exists");
        assertTrue(stakeholders.stream().anyMatch(score -> score.name().equals("Privacy/security") && score.score() >= 70), "privacy stakeholder readiness reflects redaction coverage");
        assertTrue(modules.stream().allMatch(score -> !score.pathTo100().isBlank()), "each module score has path to 100");
        assertTrue(stakeholders.stream().allMatch(score -> !score.pathTo100().isBlank()), "each stakeholder score has path to 100");
        assertContains(ReadinessScorecard.summary(), "not yet an Android MVP", "readiness summary is honest about MVP status");
    }

    private static void readinessReportRendererExportsScorecardAndGates() {
        String report = ReadinessReportRenderer.renderMarkdown();

        assertContains(report, "# Executable readiness scorecard", "readiness report title");
        assertContains(report, "| Android app module | 0 |", "readiness report includes honest Android app score");
        assertContains(report, "| Privacy/security | 70 |", "readiness report includes privacy stakeholder score");
        assertContains(report, "not yet an Android MVP", "readiness report includes MVP honesty statement");
        assertContains(report, "## Top blockers", "readiness report includes blocker section");
        assertContains(report, "Android app module", "readiness report names app blocker");
        assertContains(report, "Bluetooth Classic adapter", "readiness report names transport blocker");
    }

    private static void readinessReportWriterCreatesCiArtifact() {
        Path output;
        try {
            output = Files.createTempFile("walkitalki-readiness", ".md");
            ReadinessReportWriter.main(new String[] {output.toString()});
            String report = Files.readString(output);

            assertContains(report, "# Executable readiness scorecard", "readiness artifact title");
            assertContains(report, "## Top blockers", "readiness artifact blocker section");
            assertContains(report, "Android app module", "readiness artifact app blocker");
        } catch (IOException exception) {
            throw new AssertionError("readiness report writer should create an artifact", exception);
        }
    }

    private static void mvpGateStaysNoGoUntilAndroidAndDeviceEvidenceExist() {
        MvpGateDecision decision = MvpGateEvaluator.evaluate();

        assertEquals(MvpGateDecision.Status.NO_GO, decision.status(), "MVP gate status before Android/device evidence");
        assertContains(decision.summary(), "not ready for MVP", "MVP gate summary");
        assertTrue(decision.blockers().contains("Android app module"), "MVP gate blocks missing Android app");
        assertTrue(decision.blockers().contains("Bluetooth Classic adapter"), "MVP gate blocks missing transport adapter");
        assertTrue(decision.blockers().contains("Two-device PTT smoke test"), "MVP gate blocks missing physical PTT smoke");
        assertTrue(decision.requiredEvidence().contains("100 simulation scenarios passing"), "MVP gate records simulation evidence");
        assertTrue(decision.requiredEvidence().contains("privacy-safe diagnostics export"), "MVP gate records privacy diagnostics evidence");
    }

    private static void mvpGateReportExportsNoGoDecisionForStakeholders() {
        String report = MvpGateReportRenderer.renderMarkdown();

        assertContains(report, "# MVP GO/NO-GO gate", "MVP gate report title");
        assertContains(report, "Status: NO_GO", "MVP gate report status");
        assertContains(report, "Android app module", "MVP gate report Android blocker");
        assertContains(report, "Two-device PTT smoke test", "MVP gate report physical PTT blocker");
        assertContains(report, "100 simulation scenarios passing", "MVP gate report simulation evidence");
        assertContains(report, "privacy-safe diagnostics export", "MVP gate report privacy evidence");
    }

    private static void mvpGateReportWriterCreatesCiArtifact() {
        try {
            Path output = Files.createTempFile("walkitalki-mvp-gate", ".md");
            MvpGateReportWriter.main(new String[] {output.toString()});
            String report = Files.readString(output);

            assertContains(report, "# MVP GO/NO-GO gate", "MVP gate artifact title");
            assertContains(report, "Status: NO_GO", "MVP gate artifact status");
            assertContains(report, "Physical weak-signal and disconnect matrix", "MVP gate artifact weak-signal blocker");
        } catch (IOException exception) {
            throw new AssertionError("MVP gate report writer should create an artifact", exception);
        }
    }

    private static void architectureGuardKeepsCoreAndPreviewFrameworkFree() {
        ArchitectureGuard.Report report = ArchitectureGuard.scanRepository();

        assertTrue(report.clean(), "core and preview must stay framework-free before Android module gates");
        assertEquals("checked=2 violations=0", report.summary(), "architecture guard summary");
    }

    private static void writeInt(byte[] target, int offset, int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + 1] = (byte) (value >>> 16);
        target[offset + 2] = (byte) (value >>> 8);
        target[offset + 3] = (byte) value;
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] combined = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

    private static <T extends Throwable> T assertThrows(Class<T> expected, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                return expected.cast(actual);
            }
            throw new AssertionError("Expected " + expected.getName() + " but caught " + actual.getClass().getName(), actual);
        }
        throw new AssertionError("Expected exception " + expected.getName());
    }

    private static void assertContains(String actual, String expectedPart, String label) {
        if (!actual.contains(expectedPart)) {
            throw new AssertionError(label + ": expected to contain " + expectedPart + " in " + actual);
        }
    }

    private static void assertFalse(boolean condition, String label) {
        if (condition) {
            throw new AssertionError(label);
        }
    }

    private static void assertTrue(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertByteArrayEquals(byte[] expected, byte[] actual, String label) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(label + ": expected=" + Arrays.toString(expected) + " actual=" + Arrays.toString(actual));
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }

    private static final class FailingOutputStream extends java.io.OutputStream {
        private final String reason;

        private FailingOutputStream(String reason) {
            this.reason = reason;
        }

        @Override
        public void write(int value) throws IOException {
            throw new IOException(reason);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            throw new IOException(reason);
        }
    }

    private static final class FailingInputStream extends InputStream {
        private final String reason;

        private FailingInputStream(String reason) {
            this.reason = reason;
        }

        @Override
        public int read() throws IOException {
            throw new IOException(reason);
        }

        @Override
        public int read(byte[] target, int offset, int length) throws IOException {
            throw new IOException(reason);
        }
    }

    private static final class ChunkedInputStream extends InputStream {
        private final byte[] bytes;
        private final int chunkSize;
        private int position;

        private ChunkedInputStream(byte[] bytes, int chunkSize) {
            this.bytes = bytes;
            this.chunkSize = chunkSize;
        }

        @Override
        public int read() {
            if (position >= bytes.length) {
                return -1;
            }
            return Byte.toUnsignedInt(bytes[position++]);
        }

        @Override
        public int read(byte[] target, int offset, int length) throws IOException {
            if (position >= bytes.length) {
                return -1;
            }
            int count = Math.min(Math.min(length, chunkSize), bytes.length - position);
            System.arraycopy(bytes, position, target, offset, count);
            position += count;
            return count;
        }
    }

}
