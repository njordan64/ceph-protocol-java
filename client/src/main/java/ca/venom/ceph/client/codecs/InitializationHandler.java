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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Iterator;
import java.util.Map;

/**
 * A Netty handler that is used for the connection initialization. Initialization includes advertising supported
 * features/version, authentication and feature negotiation.
 */
public abstract class InitializationHandler<T> extends SimpleChannelInboundHandler<T> {
    /**
     * Start the flow for the handler. Usually this will involve sending a message to the server.
     *
     * @param channel channel that the contains the handler's pipeline
     */
    public abstract void start(Channel channel);

    /**
     * Calls start() on the next initialization handler in the pipeline. If this is the last initialization handler,
     * then nothing is called.
     *
     * @param channel channel that contains the handler's pipeline
     */
    protected void triggerNextHandler(Channel channel) {
        boolean foundCurrent = false;

        for (Iterator<Map.Entry<String, ChannelHandler>> it = channel.pipeline().iterator(); it.hasNext(); ) {
            Map.Entry<String,ChannelHandler> entry = it.next();

            if (foundCurrent) {
                if (entry.getValue() instanceof InitializationHandler<?> nextHandler) {
                    nextHandler.start(channel);
                    break;
                }
            } else if (this == entry.getValue()) {
                foundCurrent = true;
            }
        }
    }
}
