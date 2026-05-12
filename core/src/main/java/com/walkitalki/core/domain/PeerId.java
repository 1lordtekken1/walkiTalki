package com.walkitalki.core.domain;

import java.util.Objects;

public record PeerId(String value) {
    public PeerId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PeerId cannot be blank");
        }
    }

    public static PeerId of(String value) {
        return new PeerId(value);
    }
}
