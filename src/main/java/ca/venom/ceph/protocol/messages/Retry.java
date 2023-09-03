package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Retry extends ControlFrame {
    private UInt64 connectionSeq;

    public UInt64 getConnectionSeq() {
        return connectionSeq;
    }

    public void setConnectionSeq(UInt64 connectionSeq) {
        this.connectionSeq = connectionSeq;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            write(connectionSeq, outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            connectionSeq = readUInt64(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RETRY;
    }
}
