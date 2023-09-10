package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class UInt32 implements CephDataType {
    private final ByteBuffer value;

    public UInt32(long value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value & 0xff);
        bytes[1] = (byte) ((value & 0x0000ff00L) >> 8);
        bytes[2] = (byte) ((value & 0x00ff0000L) >> 16);
        bytes[3] = (byte) ((value & 0xff000000L) >> 24);

        this.value = ByteBuffer.wrap(bytes);
    }

    private UInt32(ByteBuffer value) {
        this.value = value;
    }

    public static UInt32 read(ByteBuffer byteBuffer) {
        UInt32 uint32 = new UInt32(byteBuffer.slice(byteBuffer.position(), 4));
        byteBuffer.position(byteBuffer.position() + 4);
        return uint32;
    }

    public long getValue() {
        return ((long) (value.get(3) & 255L) << 24) |
               ((value.get(2) & 255L) << 16) |
               ((value.get(1) & 255L) << 8) |
               (value.get(0) & 255L);
    }

    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value.array(), value.arrayOffset(), 4);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value.array(), value.arrayOffset(), 4);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UInt32) {
            UInt32 other = (UInt32) obj;
            return value.equals(other.value);
        }

        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }
}
