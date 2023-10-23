package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class CephRawByte implements CephDataType {
    private byte value;

    public CephRawByte() {
    }

    public CephRawByte(byte value) {
        this.value = value;
    }

    public static CephRawByte read(ByteBuffer byteBuffer) {
        return new CephRawByte(byteBuffer.get());
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return 1;
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
