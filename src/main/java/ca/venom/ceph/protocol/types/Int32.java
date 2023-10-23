package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

public class Int32 implements CephDataType {
    private int value;

    public Int32() {
    }

    public Int32(int value) {
        this.value = value;
    }

    public Int32(long value) {
        this.value = (int) value;
    }

    public int getValue() {
        return value;
    }

    public long getValueUnsigned() {
        return 0xffffffffL & value;
    }

    @Override
    public int getSize() {
        return 4;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeIntLE(value);
        } else {
            byteBuf.writeInt(value);
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        if (le) {
            value = byteBuf.readIntLE();
        } else {
            value = byteBuf.readInt();
        }
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
