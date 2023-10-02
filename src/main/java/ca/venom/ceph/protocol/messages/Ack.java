package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Ack extends ControlFrame {
    private UInt64 messageSequence;

    public UInt64 getMessageSequence() {
        return messageSequence;
    }

    public void setMessageSequence(UInt64 messageSequence) {
        this.messageSequence = messageSequence;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            messageSequence.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            messageSequence = UInt64.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.ACK;
    }
}
