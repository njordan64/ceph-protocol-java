package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Retry extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(connectionSeq, stream);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            connectionSeq = readUInt64(byteBuffer);
        }

        @Override
        public int getAlignment() {
            return 0;
        }
    }

    private UInt64 connectionSeq;
    private Segment1 segment1 = new Segment1();

    public UInt64 getConnectionSeq() {
        return connectionSeq;
    }

    public void setConnectionSeq(UInt64 connectionSeq) {
        this.connectionSeq = connectionSeq;
    }

    @Override
    protected Segment getSegment(int index) {
        if (index == 0) {
            return segment1;
        } else {
            return null;
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RETRY;
    }
}
