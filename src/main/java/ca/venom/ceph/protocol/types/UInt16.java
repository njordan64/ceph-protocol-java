package ca.venom.ceph.protocol.types;

import java.nio.ByteBuffer;

public class UInt16 {
    private final ByteBuffer value;

    public UInt16(ByteBuffer byteBuffer) {
        value = byteBuffer.slice(byteBuffer.position(), 2);
        byteBuffer.position(byteBuffer.position() + 2);
    }

    public static UInt16 fromValue(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value >> 8) & 0xff);

        return new UInt16(ByteBuffer.wrap(bytes));
    }

    public int getValue() {
        return ((value.get(1) & 255) << 8) + (value.get(0) & 255);
    }

    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value.array(), value.arrayOffset(), 2);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UInt16) {
            UInt16 other = (UInt16) obj;
            return value.equals(other.value);
        }

        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }
}
