package com.walkitalki.core.audio;

public record JitterBufferSnapshot(
    int capacity,
    int bufferedFrames,
    long playedFrames,
    long droppedFrames,
    long underruns,
    long lastPlayedSequenceNumber
) {
}
