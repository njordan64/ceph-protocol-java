package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RetryGlobal extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(globalSeq, stream);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            globalSeq = readUInt64(byteBuffer);
        }
    }

    private UInt64 globalSeq;
    private Segment1 segment1 = new Segment1();

    public UInt64 getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(UInt64 globalSeq) {
        this.globalSeq = globalSeq;
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
        return MessageType.SESSION_RETRY_GLOBAL;
    }
}
