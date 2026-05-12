package com.walkitalki.core.audio;

public interface AudioInput {
    void start();

    void stop();

    AudioFrame readFrame();
}
