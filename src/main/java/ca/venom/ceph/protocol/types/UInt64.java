package ca.venom.ceph.protocol.types;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class UInt64 {
    private final ByteBuffer value;

    public UInt64(ByteBuffer byteBuffer) {
        value = byteBuffer.slice(byteBuffer.position(), 8);
        byteBuffer.position(byteBuffer.position() + 8);
    }

    public static UInt64 fromValue(BigInteger value) {
        byte[] bytesFromValue = value.toByteArray();
        byte[] bytes = new byte[8];

        for (int i = 0; i < 8 && i < bytesFromValue.length; i++) {
            bytes[i] = bytesFromValue[bytesFromValue.length - 1 - i];
        }

        return new UInt64(ByteBuffer.wrap(bytes));
    }

    public BigInteger getValue() {
        byte[] bytesToParse = new byte[9];
        for (int i = 0; i < 8; i++) {
            bytesToParse[8 - i] = value.get(i);
        }

        return new BigInteger(bytesToParse);
    }

    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value.array(), value.arrayOffset(), 8);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UInt64) {
            UInt64 other = (UInt64) obj;
            return value.equals(other.value);
        }

        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }
}
