package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephRawBytes;
import io.netty.buffer.ByteBuf;

public class AuthSignatureFrame extends AuthFrameBase {
    private CephRawBytes sha256Digest;

    public byte[] getSha256Digest() {
        return sha256Digest.getValue();
    }

    public void setSha256Digest(byte[] sha256Digest) {
        this.sha256Digest = new CephRawBytes(sha256Digest);
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        sha256Digest.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        sha256Digest = new CephRawBytes(32);
        sha256Digest.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_SIGNATURE;
    }
}
