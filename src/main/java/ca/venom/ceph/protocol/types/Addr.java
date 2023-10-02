package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public abstract class Addr implements CephDataType {
    private UInt32 nonce;

    protected Addr() {
        nonce = new UInt32(0);
    }

    protected Addr(ByteBuffer byteBuffer) {
        nonce = UInt32.read(byteBuffer);
        byteBuffer.position(byteBuffer.position() + 6); // Skip the size
        decodeDetails(byteBuffer);
    }

    public static Addr read(ByteBuffer byteBuffer) {
        byteBuffer.position(byteBuffer.position() + 4); // Skip the size and type
        UInt32 type = UInt32.read(byteBuffer);

        if (type.getValue() == AddrIPv4.TYPE.getValue()) {
            return new AddrIPv4(byteBuffer);
        } else if (type.getValue() == AddrIPv6.TYPE.getValue()) {
            return new AddrIPv6(byteBuffer);
        } else {
            return null;
        }
    }

    public UInt32 getNonce() {
        return nonce;
    }

    public void setNonce(UInt32 nonce) {
        this.nonce = nonce;
    }

    protected abstract UInt16 getType();

    protected abstract void encodeDetails(ByteArrayOutputStream outputStream);

    protected abstract void encodeDetails(ByteBuffer byteBuffer);

    protected abstract void decodeDetails(ByteBuffer byteBuffer);

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        new UInt32(14 + getSize()).encode(outputStream);
        new UInt32(getType().getValue()).encode(outputStream);
        nonce.encode(outputStream);
        new UInt32(getSize() + 2).encode(outputStream);
        getType().encode(outputStream);
        encodeDetails(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        new UInt32(14 + getSize()).encode(byteBuffer);
        new UInt32(getType().getValue()).encode(byteBuffer);
        nonce.encode(byteBuffer);
        new UInt32(getSize() + 2).encode(byteBuffer);
        getType().encode(byteBuffer);
        encodeDetails(byteBuffer);
    }
}
