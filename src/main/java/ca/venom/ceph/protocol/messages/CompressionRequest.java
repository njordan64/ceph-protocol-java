package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class CompressionRequest extends ControlFrame {
    private boolean compress;
    private List<UInt32> preferredMethods;

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public List<UInt32> getPreferredMethods() {
        return preferredMethods;
    }

    public void setPreferredMethods(List<UInt32> preferredMethods) {
        this.preferredMethods = preferredMethods;
    }

    @Override
    protected int encodeSegmentBody(int index, ByteArrayOutputStream outputStream) {
        if (index == 0) {
            write(compress ? (byte) 0xff : (byte) 0x00, outputStream);
            write(preferredMethods, outputStream, UInt32.class);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            compress = byteBuffer.get() != 0;
            preferredMethods = readList(byteBuffer, UInt32.class);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.COMPRESSION_REQUEST;
    }
}
