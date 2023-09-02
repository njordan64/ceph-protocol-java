package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ReconnectOk extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(messageSeq, stream);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            messageSeq = readUInt64(byteBuffer);
        }
    }

    private UInt64 messageSeq;
    private Segment1 segment1;

    public UInt64 getMessageSeq() {
        return messageSeq;
    }

    public void setMessageSeq(UInt64 messageSeq) {
        this.messageSeq = messageSeq;
    }

    @Override
    protected Segment getSegment(int index) {
        if (index == 1) {
            return segment1;
        } else {
            return null;
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RECONNECT_OK;
    }
}
