package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

public class Int8 implements CephDataType {
    private byte value;

    public Int8() {
    }

    public Int8(byte value) {
        this.value = value;
    }

    public Int8(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    public int getValueUnsigned() {
        return 0xff & value;
    }

    @Override
    public int getSize() {
        return 1;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Int8 other) {
            return value == other.value;
        }

        return false;
    }

    public int hashCode() {
        return Byte.valueOf(value).hashCode();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeByte(value);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        value = byteBuf.readByte();
    }
}
