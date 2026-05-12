package com.walkitalki.core.audio;

import java.util.TreeMap;

public final class JitterBuffer {
    private final int capacity;
    private final TreeMap<Long, AudioFrame> frames = new TreeMap<>();
    private long playedFrames;
    private long droppedFrames;
    private long underruns;
    private long lastPlayedSequenceNumber = -1L;

    private JitterBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public static JitterBuffer withCapacity(int capacity) {
        return new JitterBuffer(capacity);
    }

    public void offer(AudioFrame frame) {
        long sequenceNumber = frame.sequenceNumber();
        if (sequenceNumber <= lastPlayedSequenceNumber || frames.containsKey(sequenceNumber)) {
            droppedFrames++;
            return;
        }

        frames.put(sequenceNumber, frame);
        while (frames.size() > capacity) {
            frames.pollFirstEntry();
            droppedFrames++;
        }
    }

    public AudioFrame poll() {
        if (frames.isEmpty()) {
            underruns++;
            return null;
        }

        AudioFrame frame = frames.pollFirstEntry().getValue();
        lastPlayedSequenceNumber = frame.sequenceNumber();
        playedFrames++;
        return frame;
    }

    public JitterBufferSnapshot snapshot() {
        return new JitterBufferSnapshot(
            capacity,
            frames.size(),
            playedFrames,
            droppedFrames,
            underruns,
            lastPlayedSequenceNumber
        );
    }
}
