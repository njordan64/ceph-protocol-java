package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Int32 implements CephDataType {
    private final int value;

    public Int32(int value) {
        this.value = value;
    }

    public static Int32 read(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Int32 parsed = new Int32(byteBuffer.getInt());
        byteBuffer.order(originalOrder);
        return parsed;
    }

    public int getValue() {
        return value;
    }

    public void encode(ByteArrayOutputStream outputStream) {
        byte[] bytes = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(value);
        outputStream.writeBytes(bytes);
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(value);
        byteBuffer.order(originalOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Int32 other) {
            return value == other.value;
        }

        return false;
    }

    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }
}
