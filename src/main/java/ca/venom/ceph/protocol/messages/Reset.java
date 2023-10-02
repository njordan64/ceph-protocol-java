package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBoolean;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Reset extends ControlFrame {
    private CephBoolean fullReset;

    public boolean isFullReset() {
        return fullReset.getValue();
    }

    public void setFullReset(boolean fullReset) {
        this.fullReset = new CephBoolean(fullReset);
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            fullReset.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            fullReset = CephBoolean.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RESET;
    }
}
