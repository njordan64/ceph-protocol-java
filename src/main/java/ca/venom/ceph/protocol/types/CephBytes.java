package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class CephBytes implements CephDataType {
    private byte[] bytes;

    public CephBytes() {
    }

    public CephBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getValue() {
        return bytes;
    }

    @Override
    public int getSize() {
        return 4 + bytes.length;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeIntLE(bytes.length);
        } else {
            byteBuf.writeInt(bytes.length);
        }
        byteBuf.writeBytes(bytes);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        int length;
        if (le) {
            length = byteBuf.readIntLE();
        } else {
            length = byteBuf.readInt();
        }

        bytes = new byte[length];
        byteBuf.readBytes(bytes);
    }
}
