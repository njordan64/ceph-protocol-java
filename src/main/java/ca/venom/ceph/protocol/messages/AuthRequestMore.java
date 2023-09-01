package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AuthRequestMore extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(authPayload, stream);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            authPayload = readByteArray(byteBuffer);
        }
    }

    private byte[] authPayload;
    private Segment1 segment1 = new Segment1();

    public byte[] getAuthPayload() {
        return authPayload;
    }

    public void setAuthPayload(byte[] authPayload) {
        this.authPayload = authPayload;
    }

    @Override
    protected Segment getSegment(int index) {
        return segment1;
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST_MORE;
    }
}
