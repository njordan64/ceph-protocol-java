package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Int8 implements CephDataType {
    private final byte value;

    public Int8(byte value) {
        this.value = value;
    }

    public static Int8 read(ByteBuffer byteBuffer) {
        return new Int8(byteBuffer.get());
    }

    public byte getValue() {
        return value;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value);
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Int8 other) {
            return value == other.value;
        }

        return false;
    }

    public int hashCode() {
        return Byte.valueOf(value).hashCode();
    }
}
