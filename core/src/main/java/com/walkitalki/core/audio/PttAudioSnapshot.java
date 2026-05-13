package com.walkitalki.core.audio;

import com.walkitalki.core.domain.PttState;

public record PttAudioSnapshot(
    PttState state,
    String reason,
    long localFramesSent,
    long remoteFramesReceived,
    boolean inputActive
) {
}
