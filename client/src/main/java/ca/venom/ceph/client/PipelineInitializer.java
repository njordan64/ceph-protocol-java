package ca.venom.ceph.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface PipelineInitializer {
    void accept(ChannelHandlerContext ctx,
                ByteBuf receivedByteBuf,
                ByteBuf sentByteBuf);
}
