package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UTime;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class KeepAliveFrame extends ControlFrame {
    private UTime timestamp;

    public UTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(UTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            timestamp.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            timestamp = UTime.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.KEEPALIVE2;
    }
}
