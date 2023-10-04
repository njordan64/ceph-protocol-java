package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class RetryGlobalFrame extends ControlFrame {
    private UInt64 globalSeq;

    public UInt64 getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(UInt64 globalSeq) {
        this.globalSeq = globalSeq;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            globalSeq.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            globalSeq = UInt64.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RETRY_GLOBAL;
    }
}
