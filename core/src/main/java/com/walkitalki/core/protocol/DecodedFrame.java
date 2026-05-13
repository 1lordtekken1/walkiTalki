package com.walkitalki.core.protocol;

public record DecodedFrame(ProtocolFrame frame, int consumedBytes) {
}
