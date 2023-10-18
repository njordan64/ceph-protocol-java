package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephRawBytes;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthSignatureFrame extends ControlFrame {
    private CephRawBytes sha256Digest;

    public byte[] getSha256Digest() {
        return sha256Digest.getValue();
    }

    public void setSha256Digest(byte[] sha256Digest) {
        this.sha256Digest = new CephRawBytes(sha256Digest);
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            sha256Digest.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            sha256Digest = CephRawBytes.read(byteBuffer, 32);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_SIGNATURE;
    }
}
