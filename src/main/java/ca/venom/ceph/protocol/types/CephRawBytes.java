package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

public class CephRawBytes implements CephDataType {
    private byte[] bytes;
    int length;

    public CephRawBytes(int length) {
        this.length = length;
    }

    public CephRawBytes(byte[] bytes) {
        this.bytes = bytes;
        this.length = bytes.length;
    }

    public byte[] getValue() {
        return bytes;
    }

    @Override
    public int getSize() {
        return bytes.length;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeBytes(bytes);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        bytes = new byte[length];
        byteBuf.readBytes(bytes);
    }
}
