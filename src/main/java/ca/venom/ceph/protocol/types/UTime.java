package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class UTime implements CephDataType {
    private final ByteBuffer value;

    public UTime(UInt32 time, UInt32 nanoSeconds) {
        byte[] bytes = new byte[8];
        this.value = ByteBuffer.wrap(bytes);

        time.encode(this.value);
        nanoSeconds.encode(this.value);

        this.value.flip();
    }

    private UTime(ByteBuffer value) {
        this.value = value;
    }

    public static UTime read(ByteBuffer byteBuffer) {
        UTime time = new UTime(byteBuffer.slice(byteBuffer.position(), 8));
        byteBuffer.position(byteBuffer.position() + 8);

        return time;
    }

    public long getTime() {
        return 1000L *
                ((long) (value.get(3) & 255L) << 24) |
                ((value.get(2) & 255L) << 16) |
                ((value.get(1) & 255L) << 8) |
                (value.get(0) & 255L);
    }

    public long getNanoSeconds() {
        return ((long) (value.get(7) & 255L) << 24) |
                ((value.get(6) & 255L) << 16) |
                ((value.get(5) & 255L) << 8) |
                (value.get(4) & 255L);
    }

    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value.array(), value.arrayOffset(), 8);
    }

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value.array(), value.arrayOffset(), 8);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UTime other) {
            return getTime() == other.getTime() && getNanoSeconds() == other.getNanoSeconds();
        }

        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }
}
