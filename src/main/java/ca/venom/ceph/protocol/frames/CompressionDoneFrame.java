package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CompressionDoneFrame extends ControlFrame {
    private CephBoolean compress;
    private UInt32 method;

    public boolean isCompress() {
        return compress.getValue();
    }

    public void setCompress(boolean compress) {
        this.compress = new CephBoolean(compress);
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
            compress.encode(outputStream);
            method.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            compress = CephBoolean.read(byteBuffer);
            method = UInt32.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.COMPRESSION_DONE;
    }
}
