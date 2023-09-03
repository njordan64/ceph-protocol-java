package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthRequestMore extends ControlFrame {
    private byte[] authPayload;

    public byte[] getAuthPayload() {
        return authPayload;
    }

    public void setAuthPayload(byte[] authPayload) {
        this.authPayload = authPayload;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            write(authPayload, outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            authPayload = readByteArray(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST_MORE;
    }
}
