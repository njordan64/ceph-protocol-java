package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.auth.AuthRequestMorePayload;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthRequestMoreFrame extends ControlFrame {
    private AuthRequestMorePayload payload;

    public AuthRequestMorePayload getPayload() {
        return payload;
    }

    public void setPayload(AuthRequestMorePayload payload) {
        this.payload = payload;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            payload.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            payload = new AuthRequestMorePayload(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST_MORE;
    }
}
