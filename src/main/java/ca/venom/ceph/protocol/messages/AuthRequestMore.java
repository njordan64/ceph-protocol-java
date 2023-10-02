package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBytes;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthRequestMore extends ControlFrame {
    private CephBytes authPayload;

    public byte[] getAuthPayload() {
        return authPayload.getValue();
    }

    public void setAuthPayload(byte[] authPayload) {
        this.authPayload = new CephBytes(authPayload);
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            authPayload.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            authPayload = CephBytes.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST_MORE;
    }
}
