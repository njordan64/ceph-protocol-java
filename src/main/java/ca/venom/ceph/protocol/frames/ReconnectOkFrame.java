package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ReconnectOkFrame extends ControlFrame {
    private UInt64 messageSeq;

    public UInt64 getMessageSeq() {
        return messageSeq;
    }

    public void setMessageSeq(UInt64 messageSeq) {
        this.messageSeq = messageSeq;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            messageSeq.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            messageSeq = UInt64.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RECONNECT_OK;
    }
}
