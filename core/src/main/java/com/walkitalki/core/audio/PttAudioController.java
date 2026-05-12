package com.walkitalki.core.audio;

import com.walkitalki.core.diagnostics.DiagnosticsEvent;
import com.walkitalki.core.diagnostics.DiagnosticsSink;
import com.walkitalki.core.domain.PeerId;
import com.walkitalki.core.domain.PttEvent;
import com.walkitalki.core.domain.PttState;
import com.walkitalki.core.domain.PttStateMachine;
import com.walkitalki.core.transport.VoiceTransport;

import java.util.Objects;

public final class PttAudioController {
    private final PttStateMachine stateMachine;
    private final AudioInput input;
    private final AudioOutput output;
    private final VoiceTransport transport;
    private final DiagnosticsSink diagnosticsSink;
    private boolean inputActive;
    private long localFramesSent;
    private long remoteFramesReceived;

    private PttAudioController(
        PeerId peerId,
        AudioInput input,
        AudioOutput output,
        VoiceTransport transport,
        DiagnosticsSink diagnosticsSink
    ) {
        this.stateMachine = PttStateMachine.connected(Objects.requireNonNull(peerId, "peerId"));
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.diagnosticsSink = Objects.requireNonNull(diagnosticsSink, "diagnosticsSink");
    }

    public static PttAudioController connected(
        PeerId peerId,
        AudioInput input,
        AudioOutput output,
        VoiceTransport transport,
        DiagnosticsSink diagnosticsSink
    ) {
        return new PttAudioController(peerId, input, output, transport, diagnosticsSink);
    }

    public PttAudioSnapshot localPttPressed() {
        stateMachine.apply(PttEvent.LocalPttPressed.INSTANCE);
        if (stateMachine.state() == PttState.TRANSMITTING) {
            input.start();
            inputActive = true;
        }
        recordPipeline(stateMachine.state(), stateMachine.lastReason());
        return snapshot();
    }

    public PttAudioSnapshot localPttReleased() {
        stateMachine.apply(PttEvent.LocalPttReleased.INSTANCE);
        if (inputActive) {
            input.stop();
            inputActive = false;
        }
        recordPipeline(stateMachine.state(), stateMachine.lastReason());
        return snapshot();
    }

    public PttAudioSnapshot pumpCapturedFrame() {
        if (stateMachine.state() != PttState.TRANSMITTING || !inputActive) {
            return snapshot();
        }
        AudioFrame frame = input.readFrame();
        if (frame == null) {
            return snapshot();
        }
        transport.sendAudioFrame(frame);
        localFramesSent++;
        recordPipeline(stateMachine.state(), "local_audio_frame_sent");
        return snapshot();
    }

    public PttAudioSnapshot remoteFrameReceived(String peerId, String deviceName, AudioFrame frame) {
        Objects.requireNonNull(frame, "frame");
        stateMachine.apply(new PttEvent.RemotePttStarted(PeerId.of(peerId)));
        output.enqueue(frame);
        transport.receiveAudioFrame(frame);
        remoteFramesReceived++;
        diagnosticsSink.record(new DiagnosticsEvent.AudioFrameReceived(peerId, deviceName, frame.payload(), frame.sequenceNumber()));
        recordPipeline(stateMachine.state(), "remote_audio_frame_received");
        return snapshot();
    }

    public PttAudioSnapshot snapshot() {
        return new PttAudioSnapshot(
            stateMachine.state(),
            stateMachine.lastReason(),
            localFramesSent,
            remoteFramesReceived,
            inputActive
        );
    }

    private void recordPipeline(PttState state, String reason) {
        diagnosticsSink.record(new DiagnosticsEvent.AudioPipeline(
            state.name().toLowerCase(),
            reason,
            localFramesSent,
            remoteFramesReceived
        ));
    }
}
