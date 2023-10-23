package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class CephBoolean implements CephDataType {
    private boolean value;

    public CephBoolean() {
    }

    public CephBoolean(boolean value) {
        this.value = value;
    }

    public static CephBoolean read(ByteBuffer byteBuffer) {
        return new CephBoolean(byteBuffer.get() != 0);
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeByte(value ? 1 : 0);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        value = byteBuf.readByte() != 0;
    }
}
