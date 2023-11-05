package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.frames.HelloFrame;
import ca.venom.ceph.protocol.types.Addr;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class HelloFrameHandler extends SimpleChannelInboundHandler<HelloFrame> {
    private static final Logger LOG = LoggerFactory.getLogger(HelloFrameHandler.class);

    private final CompletableFuture<Channel> channelReady;

    public HelloFrameHandler(CompletableFuture<Channel> channelReady) {
        this.channelReady = channelReady;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HelloFrame helloFrame) throws Exception {
        LOG.debug(">>> HelloFrameHandler.channelRead0");

        HelloFrame reply = new HelloFrame();
        reply.setNodeType(NodeType.CLIENT);

        Addr addr = new Addr();
        addr.setType(2);
        reply.setAddr(addr);
        addr.setNonce(new byte[4]);
        Addr.Ipv4Details details = new Addr.Ipv4Details();
        addr.setAddrDetails(details);
        InetSocketAddress inetAddress = (InetSocketAddress) ctx.channel().localAddress();
        details.setPort(inetAddress.getPort());
        details.setAddrBytes(inetAddress.getAddress().getAddress());

        ctx.writeAndFlush(reply).sync();

        channelReady.complete(ctx.channel());
    }
}
