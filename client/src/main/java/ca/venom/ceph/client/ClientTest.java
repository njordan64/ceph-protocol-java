package ca.venom.ceph.client;

import ca.venom.ceph.client.codecs.AuthHandler;
import ca.venom.ceph.client.codecs.CompressionHandler;
import ca.venom.ceph.client.codecs.ServerIdentHandler;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class ClientTest {
    public static void main(String[] args) throws Exception {
        CephNettyClient client = new CephNettyClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Channel clientChannel = client.start(workerGroup).get();

            AuthHandler authHandler = clientChannel.pipeline().get(AuthHandler.class);
            authHandler.start(clientChannel).get();

            CompressionHandler compressionHandler = clientChannel.pipeline().get(CompressionHandler.class);
            compressionHandler.start(clientChannel).get();

            ServerIdentHandler serverIdentHandler = clientChannel.pipeline().get(ServerIdentHandler.class);
            serverIdentHandler.start(clientChannel).get();

            clientChannel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
