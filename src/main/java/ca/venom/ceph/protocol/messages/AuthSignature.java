package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AuthSignature extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            stream.writeBytes(sha256Digest);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            sha256Digest = new byte[32];
            byteBuffer.get(sha256Digest);
        }
    }

    private byte[] sha256Digest;
    private Segment1 segment1 = new Segment1();

    public byte[] getSha256Digest() {
        return sha256Digest;
    }

    public void setSha256Digest(byte[] sha256Digest) {
        this.sha256Digest = sha256Digest;
    }

    @Override
    protected Segment getSegment(int index) {
        return segment1;
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_SIGNATURE;
    }
}
