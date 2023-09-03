package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class UInt8 {
    private final byte value;

    public UInt8(int value) {
        this.value = (byte) value;
    }

    public static UInt8 read(ByteBuffer byteBuffer) {
        return new UInt8(byteBuffer.get());
    }

    public int getValue() {
        return value & 0xff;
    }

    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value);
    }

    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UInt8 other) {
            return value == other.value;
        }

        return false;
    }

    public int hashCode() {
        return Byte.valueOf(value).hashCode();
    }
}
