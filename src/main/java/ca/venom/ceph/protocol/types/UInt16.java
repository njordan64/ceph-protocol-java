package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class UInt16 implements CephDataType {
    private final ByteBuffer value;

    public UInt16(int value) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value >> 8) & 0xff);

        this.value = ByteBuffer.wrap(bytes);
    }

    private UInt16(ByteBuffer value) {
        this.value = value;
    }

    public static UInt16 read(ByteBuffer byteBuffer) {
        UInt16 uint16 = new UInt16(byteBuffer.slice(byteBuffer.position(), 2));
        byteBuffer.position(byteBuffer.position() + 2);
        return uint16;
    }

    public int getValue() {
        return ((value.get(1) & 255) << 8) + (value.get(0) & 255);
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value.array(), value.arrayOffset(), 2);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value.array(), value.arrayOffset(), 2);
    }

    public boolean equals(Object obj) {
        if (obj instanceof UInt16 other) {
            return value.equals(other.value);
        }

        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }
}
