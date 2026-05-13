package com.walkitalki.core.audio;

public final class FakeAudioOutput implements AudioOutput {
    private final JitterBuffer jitterBuffer;

    private FakeAudioOutput(int jitterCapacity) {
        jitterBuffer = JitterBuffer.withCapacity(jitterCapacity);
    }

    public static FakeAudioOutput withJitterCapacity(int jitterCapacity) {
        return new FakeAudioOutput(jitterCapacity);
    }

    @Override
    public void enqueue(AudioFrame frame) {
        jitterBuffer.offer(frame);
    }

    @Override
    public AudioFrame playNext() {
        return jitterBuffer.poll();
    }

    @Override
    public JitterBufferSnapshot snapshot() {
        return jitterBuffer.snapshot();
    }
}
