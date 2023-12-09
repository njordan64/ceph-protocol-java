package ca.venom.ceph.client.codecs;

import ca.venom.ceph.protocol.NodeType;
import ca.venom.ceph.protocol.frames.HelloFrame;
import ca.venom.ceph.protocol.types.AddrIPv4;
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
        reply.setSegment1(new HelloFrame.Segment1());
        reply.getSegment1().setNodeType(NodeType.CLIENT);

        AddrIPv4 addr = new AddrIPv4();
        reply.getSegment1().setAddr(addr);
        addr.setNonce(0);
        InetSocketAddress inetAddress = (InetSocketAddress) ctx.channel().localAddress();
        addr.setPort((short) inetAddress.getPort());
        addr.setAddrBytes(inetAddress.getAddress().getAddress());

        ctx.writeAndFlush(reply).sync();

        channelReady.complete(ctx.channel());
    }
}
