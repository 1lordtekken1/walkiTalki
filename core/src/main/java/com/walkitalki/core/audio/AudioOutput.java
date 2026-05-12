package com.walkitalki.core.audio;

public interface AudioOutput {
    void enqueue(AudioFrame frame);

    AudioFrame playNext();

    JitterBufferSnapshot snapshot();
}
