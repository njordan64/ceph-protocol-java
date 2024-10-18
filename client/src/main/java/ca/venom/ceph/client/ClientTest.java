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

import ca.venom.ceph.protocol.messages.MonMap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.CompletableFuture;

public class ClientTest {
    public static void main(String[] args) throws Exception {
        CephNettyClient client = new CephNettyClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Channel clientChannel = client.start(workerGroup).get();
            CompletableFuture<MonMap> monMapFuture = client.getMonMap();
            MonMap monMap = monMapFuture.get();
            //client.ping();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
