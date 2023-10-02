package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Int16 implements CephDataType {
    private final short value;

    public Int16(short value) {
        this.value = value;
    }

    public static Int16 read(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        Int16 parsed = new Int16(byteBuffer.getShort());
        byteBuffer.order(originalOrder);
        return parsed;
    }

    public short getValue() {
        return value;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value & 0xff);
        outputStream.write((value >> 8) & 0xff);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put((byte) (value & 0xff));
        byteBuffer.put((byte) ((value >> 8) & 0xff));
    }

    public boolean equals(Object obj) {
        if (obj instanceof Int16 other) {
            return value == other.value;
        }

        return false;
    }

    public int hashCode() {
        return Short.valueOf(value).hashCode();
    }
}
