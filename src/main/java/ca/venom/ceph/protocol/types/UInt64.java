package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class UInt64 implements CephDataType {
    private final ByteBuffer value;

    public UInt64(BigInteger value) {
        byte[] bytesFromValue = value.toByteArray();
        byte[] bytes = new byte[8];

        for (int i = 0; i < 8 && i < bytesFromValue.length; i++) {
            bytes[i] = bytesFromValue[bytesFromValue.length - 1 - i];
        }

        this.value = ByteBuffer.wrap(bytes);
    }

    private UInt64(ByteBuffer value) {
        this.value = value;
    }

    public static UInt64 read(ByteBuffer byteBuffer) {
        UInt64 uint64 = new UInt64(byteBuffer.slice(byteBuffer.position(), 8));
        byteBuffer.position(byteBuffer.position() + 8);
        return uint64;
    }

    public BigInteger getValue() {
        byte[] bytesToParse = new byte[9];
        for (int i = 0; i < 8; i++) {
            bytesToParse[8 - i] = value.get(i);
        }

        return new BigInteger(bytesToParse);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value.array(), value.arrayOffset(), 8);
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value.array(), value.arrayOffset(), 8);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UInt64 other) {
            return value.equals(other.value);
        }

        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }
}
