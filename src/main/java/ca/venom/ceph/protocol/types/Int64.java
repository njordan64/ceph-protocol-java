package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Int64 implements CephDataType {
    private final long value;

    public Int64(long value) {
        this.value = value;
    }

    public static Int64 read(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Int64 parsed = new Int64(byteBuffer.getLong());
        byteBuffer.order(originalOrder);
        return parsed;
    }

    public long getValue() {
        return value;
    }

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putLong(value);
        byteBuffer.order(originalOrder);
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        byte[] bytes = new byte[8];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putLong(value);
        outputStream.writeBytes(bytes);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Int64 other) {
            return value == other.value;
        }

        return false;
    }

    public int hashCode() {
        return Long.valueOf(value).hashCode();
    }
}
