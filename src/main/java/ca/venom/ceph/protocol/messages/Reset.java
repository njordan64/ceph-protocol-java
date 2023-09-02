package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Reset extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            stream.write(fullReset ? 1 : 0);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            fullReset = byteBuffer.get() > 0;
        }
    }
    boolean fullReset;
    private Segment1 segment1 = new Segment1();

    public boolean isFullReset() {
        return fullReset;
    }

    public void setFullReset(boolean fullReset) {
        this.fullReset = fullReset;
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
        return MessageType.SESSION_RECONNECT;
    }
}
