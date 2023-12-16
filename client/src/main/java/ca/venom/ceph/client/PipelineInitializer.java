/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface PipelineInitializer {
    void accept(ChannelHandlerContext ctx,
                ByteBuf receivedByteBuf,
                ByteBuf sentByteBuf);
}
