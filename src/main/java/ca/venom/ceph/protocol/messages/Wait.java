package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Wait extends ControlFrame {
    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        return 0;
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
    }

    @Override
    public MessageType getTag() {
        return MessageType.WAIT;
    }
}
