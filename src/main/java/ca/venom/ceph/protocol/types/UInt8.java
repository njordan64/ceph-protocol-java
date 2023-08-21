package ca.venom.ceph.protocol.types;

import java.nio.ByteBuffer;

public class UInt8 {
    private final byte value;

    public UInt8(ByteBuffer byteBuffer) {
        this.value = byteBuffer.get();
    }

    private UInt8(byte value) {
        this.value = value;
    }

    public static UInt8 fromValue(int value) {
        return new UInt8((byte) value);
    }

    public int getValue() {
        return value & 0xff;
    }

    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UInt8) {
            UInt8 other = (UInt8) obj;
            return value == other.value;
        }

        return false;
    }

    public int hashCode() {
        return Byte.valueOf(value).hashCode();
    }
}
