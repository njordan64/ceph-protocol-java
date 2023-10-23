package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Int64 implements CephDataType {
    private long value;

    public Int64() {
    }

    public Int64(long value) {
        this.value = value;
    }

    public Int64(BigInteger value) {
        byte[] valueBytes = value.toByteArray();
        byte[] fullBytes = new byte[8];

        if (valueBytes.length >= 8) {
            System.arraycopy(valueBytes, valueBytes.length - 8, fullBytes, 0, 8);
        } else {
            System.arraycopy(valueBytes, 0, fullBytes, 8 - valueBytes.length, valueBytes.length);
        }

        ByteBuf byteBuf = Unpooled.wrappedBuffer(fullBytes);
        this.value = byteBuf.readLong();
    }

    public long getValue() {
        return value;
    }

    public BigInteger getValueUnsigned() {
        byte[] fullBytes = new byte[9];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(fullBytes);
        byteBuf.writerIndex(1);
        byteBuf.writeLong(value);

        return new BigInteger(fullBytes);
    }

    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeLongLE(value);
        } else {
            byteBuf.writeLong(value);
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        if (le) {
            value = byteBuf.readLongLE();
        } else {
            value = byteBuf.readLong();
        }
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
