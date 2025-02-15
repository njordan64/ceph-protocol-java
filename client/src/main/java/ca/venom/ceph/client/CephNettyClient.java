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

import ca.venom.ceph.client.codecs.AuthHandler;
import ca.venom.ceph.client.codecs.BannerHandler;
import ca.venom.ceph.client.codecs.CephFrameCodec;
import ca.venom.ceph.client.codecs.CephPreParsedFrameCodec;
import ca.venom.ceph.client.codecs.CompressionHandler;
import ca.venom.ceph.client.codecs.GeneralMessageHandler;
import ca.venom.ceph.client.codecs.HelloFrameHandler;
import ca.venom.ceph.client.codecs.RequestWithFuture;
import ca.venom.ceph.client.codecs.ServerIdentHandler;
import ca.venom.ceph.protocol.frames.ControlFrame;
import ca.venom.ceph.protocol.frames.MessageFrame;
import ca.venom.ceph.protocol.messages.CephMsgHeader2;
import ca.venom.ceph.protocol.messages.MMonMap;
import ca.venom.ceph.types.MessageType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;

public class CephNettyClient {
    private final String hostname;
    private final int port;
    private final String username;
    private final String keyString;
    private Channel channel;

    public CephNettyClient(String hostname, int port, String username, String keyString) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.keyString = keyString;
    }

    public CompletableFuture<Channel> start(EventLoopGroup workerGroup) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        final CompletableFuture<Channel> channelReady = new CompletableFuture<>();

        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new BannerHandler(
                                        (p, r, s) -> initPipeline(p, r, s, channelReady)));
                    }
                    });

        bootstrap.connect(hostname, port).sync();

        return channelReady;
    }

    public CompletableFuture<MMonMap> getMonMap() {
        final MessageFrame getMonMapFrame = new MessageFrame();
        getMonMapFrame.setHead(new CephMsgHeader2());
        getMonMapFrame.getHead().setSeq(1L);
        getMonMapFrame.getHead().setType((short) MessageType.MSG_MON_GET_MAP.getValueInt());
        getMonMapFrame.getHead().setPriority((short) 127);
        getMonMapFrame.getHead().setVersion((short) 1);
        getMonMapFrame.getHead().setFlags((byte) 3);
        getMonMapFrame.getHead().setCompatVersion((short) 1);

        final CompletableFuture<ControlFrame> responseFuture = new CompletableFuture<>();
        channel.writeAndFlush(
                new RequestWithFuture(
                        getMonMapFrame,
                        responseFuture
                )
        );

        final CompletableFuture<MMonMap> monMapFuture = new CompletableFuture<>();
        responseFuture.thenAccept(frame -> {
            monMapFuture.complete((MMonMap) ((MessageFrame) frame).getPayload());
        });

        return monMapFuture;
    }

    public void ping() {
        final MessageFrame pingFrame = new MessageFrame();
        pingFrame.setHead(new CephMsgHeader2());
        pingFrame.getHead().setSeq(1L);
        pingFrame.getHead().setType((short) MessageType.MSG_PING.getValueInt());
        pingFrame.getHead().setPriority((short) 127);
        pingFrame.getHead().setVersion((short) 1);
        pingFrame.getHead().setFlags((byte) 3);
        pingFrame.getHead().setCompatVersion((short) 1);

        final CompletableFuture<ControlFrame> responseFuture = new CompletableFuture<>();
        channel.writeAndFlush(
                new RequestWithFuture(
                        pingFrame,
                        responseFuture
                )
        );

        try {
            ControlFrame responseFrame = responseFuture.get();
            responseFrame = null;
        } catch (Exception e) {
            //
        }
    }

    private void initPipeline(ChannelHandlerContext ctx,
                              ByteBuf receivedByteBuf,
                              ByteBuf sentByteBuf,
                              CompletableFuture<Channel> channelReady) {
        ctx.pipeline().addLast("Frame-Preparser", new CephPreParsedFrameCodec(receivedByteBuf, sentByteBuf));
        ctx.pipeline().addLast("Frame-Codec", new CephFrameCodec());
        ctx.pipeline().addLast("Hello-Handler", new HelloFrameHandler());
        ctx.pipeline().addLast("Auth-Handler", new AuthHandler(username, keyString));
        ctx.pipeline().addLast("Compression-Handler", new CompressionHandler());
        ctx.pipeline().addLast("ServerIdent-Handler", new ServerIdentHandler(channelReady));
        ctx.pipeline().addLast("RequestResponse-Handler", new GeneralMessageHandler());

        channel = ctx.channel();
    }
}
