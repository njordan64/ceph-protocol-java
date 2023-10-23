package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class CephDataContainer implements CephDataType {
    public int getSize() {
        return 4 + getPayloadSize();
    }

    protected abstract int getPayloadSize();

    protected abstract void encodePayload(ByteBuf byteBuf, boolean le);

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeIntLE(getPayloadSize());
        } else {
            byteBuf.writeInt(getPayloadSize());
        }

        encodePayload(byteBuf, le);
    }

    protected abstract void decodePayload(ByteBuf byteBuf, boolean le);

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byteBuf.skipBytes(4);

        decodePayload(byteBuf, le);
    }
}
