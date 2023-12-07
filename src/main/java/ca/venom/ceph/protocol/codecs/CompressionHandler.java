package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.protocol.frames.CompressionDoneFrame;
import ca.venom.ceph.protocol.frames.CompressionRequestFrame;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class CompressionHandler extends SimpleChannelInboundHandler<CompressionDoneFrame> {
    private CompletableFuture<Boolean> future;

    public CompletableFuture<Boolean> start(Channel channel) throws Exception {
        CompressionRequestFrame requestFrame = new CompressionRequestFrame();
        requestFrame.setSegment1(new CompressionRequestFrame.Segment1());
        requestFrame.getSegment1().setCompress(false);
        requestFrame.getSegment1().setPreferredMethods(Collections.emptyList());

        future = new CompletableFuture<>();
        channel.writeAndFlush(requestFrame).sync();

        return future;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CompressionDoneFrame compressionDoneFrame) {
        future.complete(compressionDoneFrame.getSegment1().isCompress());
    }
}
