package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Reset extends ControlFrame {
    boolean fullReset;

    public boolean isFullReset() {
        return fullReset;
    }

    public void setFullReset(boolean fullReset) {
        this.fullReset = fullReset;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            outputStream.write(fullReset ? 1 : 0);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            fullReset = byteBuffer.get() > 0;
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RECONNECT;
    }
}
