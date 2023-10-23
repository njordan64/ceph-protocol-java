package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

public class Int16 implements CephDataType {
    private short value;

    public Int16() {
    }

    public Int16(short value) {
        this.value = value;
    }

    public Int16(int value) {
        this.value = (short) value;
    }

    public short getValue() {
        return value;
    }

    public int getValueUnsigned() {
        return 0xffff & value;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeShortLE(value);
        } else {
            byteBuf.writeShort(value);
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        if (le) {
            value = byteBuf.readShortLE();
        } else {
            value = byteBuf.readShort();
        }
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
