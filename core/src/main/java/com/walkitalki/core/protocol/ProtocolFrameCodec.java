package com.walkitalki.core.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class ProtocolFrameCodec {
    public static final int MAX_PAYLOAD_BYTES = 4 * 1024;
    public static final int TYPE_OFFSET = 5;
    public static final int HEADER_BYTES = 4 + 1 + 1 + Integer.BYTES + Long.BYTES + Integer.BYTES;
    public static final int PAYLOAD_LENGTH_OFFSET = 4 + 1 + 1 + Integer.BYTES + Long.BYTES;

    private static final byte[] MAGIC = new byte[] {'W', 'K', 'T', 'K'};
    private static final int VERSION = 1;

    private ProtocolFrameCodec() {
    }

    public static byte[] encode(ProtocolFrame frame) {
        byte[] payload = frame.payload();
        if (payload.length > MAX_PAYLOAD_BYTES) {
            throw new ProtocolException.PayloadTooLarge(payload.length, MAX_PAYLOAD_BYTES);
        }

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + payload.length).order(ByteOrder.BIG_ENDIAN);
        buffer.put(MAGIC);
        buffer.put((byte) VERSION);
        buffer.put((byte) frame.type().wireValue());
        buffer.putInt((int) frame.sequenceNumber());
        buffer.putLong(frame.timestampMillis());
        buffer.putInt(payload.length);
        buffer.put(payload);
        return buffer.array();
    }

    public static ProtocolFrame decode(byte[] bytes) {
        DecodedFrame decoded = decodeNext(bytes);
        if (decoded.consumedBytes() != bytes.length) {
            throw new ProtocolException.IncompleteFrame(bytes.length, decoded.consumedBytes());
        }
        return decoded.frame();
    }

    public static DecodedFrame decodeNext(byte[] bytes) {
        if (bytes.length < HEADER_BYTES) {
            throw new ProtocolException.IncompleteFrame(bytes.length, HEADER_BYTES);
        }

        for (int index = 0; index < MAGIC.length; index++) {
            if (bytes[index] != MAGIC[index]) {
                throw new ProtocolException.InvalidMagic();
            }
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        buffer.position(MAGIC.length);
        int version = Byte.toUnsignedInt(buffer.get());
        if (version != VERSION) {
            throw new ProtocolException.UnsupportedVersion(version);
        }

        int rawType = Byte.toUnsignedInt(buffer.get());
        FrameType type = FrameType.fromWireValue(rawType)
            .orElseThrow(() -> new ProtocolException.UnknownFrameType(rawType));
        long sequenceNumber = Integer.toUnsignedLong(buffer.getInt());
        long timestampMillis = buffer.getLong();
        int payloadBytes = buffer.getInt();

        if (payloadBytes > MAX_PAYLOAD_BYTES) {
            throw new ProtocolException.PayloadTooLarge(payloadBytes, MAX_PAYLOAD_BYTES);
        }
        int requiredBytes = HEADER_BYTES + payloadBytes;
        if (bytes.length < requiredBytes) {
            throw new ProtocolException.IncompleteFrame(bytes.length, requiredBytes);
        }

        byte[] payload = Arrays.copyOfRange(bytes, HEADER_BYTES, requiredBytes);
        return new DecodedFrame(new ProtocolFrame(type, sequenceNumber, timestampMillis, payload), requiredBytes);
    }
}
