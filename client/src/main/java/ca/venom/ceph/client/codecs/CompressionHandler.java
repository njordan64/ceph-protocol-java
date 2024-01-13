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

import ca.venom.ceph.protocol.frames.CompressionDoneFrame;
import ca.venom.ceph.protocol.frames.CompressionRequestFrame;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class CompressionHandler extends InitializationHandler<CompressionDoneFrame> {
    public void start(Channel channel) {
        CompressionRequestFrame requestFrame = new CompressionRequestFrame();
        requestFrame.setSegment1(new CompressionRequestFrame.Segment1());
        requestFrame.getSegment1().setCompress(false);
        requestFrame.getSegment1().setPreferredMethods(Collections.emptyList());

        channel.writeAndFlush(requestFrame);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CompressionDoneFrame compressionDoneFrame) {
        triggerNextHandler(ctx.channel());
    }
}
