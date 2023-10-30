package ca.venom.ceph;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface TriConsumer {
    void accept(ChannelHandlerContext ctx, ByteBuf receivedByteBuf, ByteBuf sentByteBuf);
}
