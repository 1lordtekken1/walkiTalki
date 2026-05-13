package com.walkitalki.core.protocol;

public sealed class ProtocolException extends RuntimeException permits
    ProtocolException.InvalidMagic,
    ProtocolException.UnsupportedVersion,
    ProtocolException.PayloadTooLarge,
    ProtocolException.UnknownFrameType,
    ProtocolException.IncompleteFrame,
    ProtocolException.StreamReadFailure {

    private final boolean recoverable;

    private ProtocolException(String message, boolean recoverable) {
        super(message);
        this.recoverable = recoverable;
    }

    public boolean recoverable() {
        return recoverable;
    }

    public static final class InvalidMagic extends ProtocolException {
        public InvalidMagic() {
            super("Invalid protocol magic", false);
        }
    }

    public static final class UnsupportedVersion extends ProtocolException {
        public UnsupportedVersion(int version) {
            super("Unsupported protocol version: " + version, false);
        }
    }

    public static final class PayloadTooLarge extends ProtocolException {
        public PayloadTooLarge(int payloadBytes, int maxPayloadBytes) {
            super("Payload too large: " + payloadBytes + " > " + maxPayloadBytes, false);
        }
    }

    public static final class UnknownFrameType extends ProtocolException {
        private final int rawType;

        public UnknownFrameType(int rawType) {
            super("Unknown frame type: " + rawType, true);
            this.rawType = rawType;
        }

        public int rawType() {
            return rawType;
        }
    }

    public static final class IncompleteFrame extends ProtocolException {
        public IncompleteFrame(int availableBytes, int requiredBytes) {
            super("Incomplete frame: " + availableBytes + " < " + requiredBytes, true);
        }
    }

    public static final class StreamReadFailure extends ProtocolException {
        public StreamReadFailure(String reason, Throwable cause) {
            super("Protocol stream read failed: " + reason, true);
            initCause(cause);
        }
    }
}
