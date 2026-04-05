/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.client.codecs;

import ca.venom.ceph.protocol.NodeType;
import ca.venom.ceph.protocol.frames.HelloFrame;
import ca.venom.ceph.protocol.types.CephAddr;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class HelloFrameHandler extends InitializationHandler<HelloFrame> {
    private static final Logger LOG = LoggerFactory.getLogger(HelloFrameHandler.class);

    @Override
    public void start(Channel channel) {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HelloFrame helloFrame) throws Exception {
        LOG.debug(">>> HelloFrameHandler.channelRead0");

        HelloFrame reply = new HelloFrame();
        reply.setSegment1(new HelloFrame.Segment1());
        reply.getSegment1().setNodeType(NodeType.CLIENT);

        CephAddr addr = new CephAddr();
        addr.setSocketAddress((InetSocketAddress) ctx.channel().localAddress());
        reply.getSegment1().setAddr(addr);

        ctx.writeAndFlush(reply).sync();

        triggerNextHandler(ctx.channel());
    }
}
