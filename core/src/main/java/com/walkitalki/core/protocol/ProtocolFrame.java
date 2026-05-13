package com.walkitalki.core.protocol;

import java.util.Arrays;
import java.util.Objects;

public final class ProtocolFrame {
    private final FrameType type;
    private final long sequenceNumber;
    private final long timestampMillis;
    private final byte[] payload;

    public ProtocolFrame(FrameType type, long sequenceNumber, long timestampMillis, byte[] payload) {
        this.type = Objects.requireNonNull(type, "type");
        this.sequenceNumber = sequenceNumber;
        this.timestampMillis = timestampMillis;
        this.payload = Objects.requireNonNull(payload, "payload").clone();
    }

    public FrameType type() {
        return type;
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
        if (!(other instanceof ProtocolFrame that)) {
            return false;
        }
        return sequenceNumber == that.sequenceNumber
            && timestampMillis == that.timestampMillis
            && type == that.type
            && Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, sequenceNumber, timestampMillis);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
}
