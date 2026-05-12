package com.walkitalki.core.audio;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public final class FakeAudioInput implements AudioInput {
    private final Queue<AudioFrame> frames;
    private boolean started;

    private FakeAudioInput(AudioFrame[] frames) {
        this.frames = new ArrayDeque<>(Arrays.asList(frames));
    }

    public static FakeAudioInput withFrames(AudioFrame... frames) {
        return new FakeAudioInput(frames);
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public AudioFrame readFrame() {
        if (!started) {
            return null;
        }
        return frames.poll();
    }
}
