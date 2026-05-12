package com.walkitalki.core.protocol;

import java.util.Optional;

public enum FrameType {
    HELLO(1),
    AUDIO(2),
    PTT_START(3),
    PTT_STOP(4),
    PING(5),
    PONG(6),
    ERROR(7);

    private final int wireValue;

    FrameType(int wireValue) {
        this.wireValue = wireValue;
    }

    public int wireValue() {
        return wireValue;
    }

    public static Optional<FrameType> fromWireValue(int wireValue) {
        for (FrameType type : values()) {
            if (type.wireValue == wireValue) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
