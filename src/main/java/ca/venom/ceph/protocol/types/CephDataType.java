package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

public interface CephDataType {
    int getSize();

    void encode(ByteBuf byteBuf, boolean le);

    void decode(ByteBuf byteBuf, boolean le);
}
