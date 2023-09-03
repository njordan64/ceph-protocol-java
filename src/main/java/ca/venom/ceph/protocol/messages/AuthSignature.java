package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthSignature extends ControlFrame {
    private byte[] sha256Digest;

    public byte[] getSha256Digest() {
        return sha256Digest;
    }

    public void setSha256Digest(byte[] sha256Digest) {
        this.sha256Digest = sha256Digest;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            outputStream.writeBytes(sha256Digest);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            sha256Digest = new byte[32];
            byteBuffer.get(sha256Digest);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_SIGNATURE;
    }
}
