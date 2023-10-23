package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class UTime implements CephDataType {
    private long time;
    private long nanoSeconds;

    public UTime() {
    }

    public UTime(long time, long nanoSeconds) {
        this.time = time;
        this.nanoSeconds = nanoSeconds;
    }

    public long getTime() {
        return time;
    }

    public long getNanoSeconds() {
        return nanoSeconds;
    }

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byte[] bytes = new byte[8];
        ByteBuf conversionByteBuf = Unpooled.wrappedBuffer(bytes);

        if (le) {
            conversionByteBuf.writeLongLE(time);
            byteBuf.writeBytes(bytes, 0, 4);

            conversionByteBuf.writerIndex(0);
            conversionByteBuf.writeLongLE(nanoSeconds);
            byteBuf.writeBytes(bytes, 0, 4);
        } else {
            conversionByteBuf.writeLong(time);
            byteBuf.writeBytes(bytes, 4, 4);

            conversionByteBuf.writerIndex(0);
            conversionByteBuf.writeLong(nanoSeconds);
            byteBuf.writeBytes(bytes, 4, 4);
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte[] bytes = new byte[8];
        ByteBuf conversionByteBuf = Unpooled.wrappedBuffer(bytes);

        if (le) {
            byteBuf.readBytes(bytes, 0, 4);
            time = conversionByteBuf.readLongLE();

            conversionByteBuf.readerIndex(0);
            byteBuf.readBytes(bytes, 0, 4);
            nanoSeconds = conversionByteBuf.readLongLE();
        } else {
            byteBuf.readBytes(bytes, 4, 4);
            time = conversionByteBuf.readLong();

            conversionByteBuf.readerIndex(0);
            byteBuf.readBytes(byteBuf, 4, 4);
            nanoSeconds = conversionByteBuf.readLong();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UTime other) {
            return getTime() == other.getTime() && getNanoSeconds() == other.getNanoSeconds();
        }

        return false;
    }

    public int hashCode() {
        return 32 + Long.hashCode(time) + Long.hashCode(nanoSeconds);
    }
}
