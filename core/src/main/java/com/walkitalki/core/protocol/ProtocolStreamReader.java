package com.walkitalki.core.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ProtocolStreamReader {
    private final InputStream inputStream;

    public ProtocolStreamReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public ProtocolFrame readNextFrame() {
        byte[] header = readExactly(ProtocolFrameCodec.HEADER_BYTES);
        int payloadBytes = ByteBuffer
            .wrap(header, ProtocolFrameCodec.PAYLOAD_LENGTH_OFFSET, Integer.BYTES)
            .order(ByteOrder.BIG_ENDIAN)
            .getInt();
        if (payloadBytes < 0 || payloadBytes > ProtocolFrameCodec.MAX_PAYLOAD_BYTES) {
            throw new ProtocolException.PayloadTooLarge(payloadBytes, ProtocolFrameCodec.MAX_PAYLOAD_BYTES);
        }
        byte[] payload = readExactly(payloadBytes);
        byte[] frameBytes = new byte[ProtocolFrameCodec.HEADER_BYTES + payloadBytes];
        System.arraycopy(header, 0, frameBytes, 0, header.length);
        System.arraycopy(payload, 0, frameBytes, header.length, payload.length);
        return ProtocolFrameCodec.decode(frameBytes);
    }

    private byte[] readExactly(int byteCount) {
        byte[] bytes = new byte[byteCount];
        int offset = 0;
        while (offset < byteCount) {
            try {
                int read = inputStream.read(bytes, offset, byteCount - offset);
                if (read == -1) {
                    throw new ProtocolException.IncompleteFrame(offset, byteCount);
                }
                offset += read;
            } catch (IOException exception) {
                throw new ProtocolException.StreamReadFailure(exception.getMessage(), exception);
            }
        }
        return bytes;
    }
}
