package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CompressionDone extends ControlFrame {
    private boolean compress;
    private UInt32 method;

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public UInt32 getMethod() {
        return method;
    }

    public void setMethod(UInt32 method) {
        this.method = method;
    }

    @Override
    protected int encodeSegmentBody(int index, ByteArrayOutputStream outputStream) {
        if (index == 0) {
            write(compress ? (byte) 0xff : (byte) 0x00, outputStream);
            write(method, outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            compress = byteBuffer.get() != 0;
            method = readUInt32(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.COMPRESSION_DONE;
    }
}
