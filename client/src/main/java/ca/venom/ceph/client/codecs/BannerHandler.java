package ca.venom.ceph.client.codecs;

import ca.venom.ceph.client.PipelineInitializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class BannerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(BannerHandler.class);
    private static final String PREFIX = "ceph v2\n";
    private static final int EXPECTED_SIZE = 26;
    private final PipelineInitializer pipelineInitializer;

    public BannerHandler(PipelineInitializer pipelineInitializer) {
        this.pipelineInitializer = pipelineInitializer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        LOG.debug(">>> BannerHandler.channelRead0");

        ByteBuf receivedByteBuf = Unpooled.buffer();
        byteBuf.getBytes(byteBuf.readerIndex(), receivedByteBuf, 0, byteBuf.writerIndex());
        receivedByteBuf.writerIndex(byteBuf.writerIndex());

        if (byteBuf.readableBytes() >= EXPECTED_SIZE) {
            byte[] prefixBytes = new byte[8];
            byteBuf.readBytes(prefixBytes);

            if (!PREFIX.equals(new String(prefixBytes, StandardCharsets.UTF_8))) {
                ctx.close();
            }

            short length = byteBuf.readShortLE();
            if (length != 16) {
                ctx.close();
            }

            long supported = byteBuf.readLongLE();
            long required = byteBuf.readLongLE();

            if ((1L & supported) == 0 || (2L & required) > 0) {
                ctx.close();
            }

            ByteBuf bannerByteBuf = ctx.alloc().buffer(EXPECTED_SIZE);
            bannerByteBuf.writeBytes(PREFIX.getBytes(StandardCharsets.UTF_8));
            bannerByteBuf.writeShortLE(16);
            bannerByteBuf.writeLongLE(3); // Rev1, compression
            bannerByteBuf.writeLongLE(1); // Rev 1 no compression

            ByteBuf sentByteBuf = Unpooled.buffer();
            bannerByteBuf.getBytes(
                    bannerByteBuf.readerIndex(),
                    sentByteBuf,
                    0,
                    bannerByteBuf.writerIndex() - bannerByteBuf.readerIndex());
            sentByteBuf.writerIndex(bannerByteBuf.writerIndex() - bannerByteBuf.readerIndex());

            ctx.writeAndFlush(bannerByteBuf).sync();

            ctx.pipeline().remove(this);
            pipelineInitializer.accept(ctx, receivedByteBuf, sentByteBuf);
        }
    }
}
