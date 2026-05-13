package com.walkitalki.core.audio;

import java.util.Arrays;
import java.util.Objects;

public final class AudioFrame {
    private final long sequenceNumber;
    private final long timestampMillis;
    private final byte[] payload;

    private AudioFrame(long sequenceNumber, long timestampMillis, byte[] payload) {
        this.sequenceNumber = sequenceNumber;
        this.timestampMillis = timestampMillis;
        this.payload = Objects.requireNonNull(payload, "payload").clone();
    }

    public static AudioFrame pcm(long sequenceNumber, long timestampMillis, byte[] payload) {
        return new AudioFrame(sequenceNumber, timestampMillis, payload);
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    public long timestampMillis() {
        return timestampMillis;
    }

    public byte[] payload() {
        return payload.clone();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AudioFrame that)) {
            return false;
        }
        return sequenceNumber == that.sequenceNumber
            && timestampMillis == that.timestampMillis
            && Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sequenceNumber, timestampMillis);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
