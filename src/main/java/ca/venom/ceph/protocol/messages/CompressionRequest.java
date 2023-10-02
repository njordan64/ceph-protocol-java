package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class CompressionRequest extends ControlFrame {
    private CephBoolean compress;
    private CephList<UInt32> preferredMethods;

    public boolean isCompress() {
        return compress.getValue();
    }

    public void setCompress(boolean compress) {
        this.compress = new CephBoolean(compress);
    }

    public List<UInt32> getPreferredMethods() {
        return preferredMethods.getValues();
    }

    public void setPreferredMethods(List<UInt32> preferredMethods) {
        this.preferredMethods = new CephList<>(preferredMethods);
    }

    @Override
    protected int encodeSegmentBody(int index, ByteArrayOutputStream outputStream) {
        if (index == 0) {
            compress.encode(outputStream);
            preferredMethods.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            compress = CephBoolean.read(byteBuffer);
            preferredMethods = CephList.read(byteBuffer, UInt32.class);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.COMPRESSION_REQUEST;
    }
}
