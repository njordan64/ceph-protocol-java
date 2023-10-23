package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.protocol.frames.ControlFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class CephFrameCodec extends MessageToMessageCodec<CephPreParsedFrame, ControlFrame> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ControlFrame controlFrame, List<Object> list) throws Exception {
        CephPreParsedFrame frame = new CephPreParsedFrame();
        frame.setMessageType(controlFrame.getTag());

        ByteBuf segmentByteBuf = ctx.alloc().buffer();
        controlFrame.encodeSegment1(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment1(new CephPreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf == null) {
            segmentByteBuf = ctx.alloc().buffer();
        }
        controlFrame.encodeSegment2(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment2(new CephPreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf == null) {
            segmentByteBuf = ctx.alloc().buffer();
        }
        controlFrame.encodeSegment3(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment3(new CephPreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf == null) {
            segmentByteBuf = ctx.alloc().buffer();
        }
        controlFrame.encodeSegment4(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment4(new CephPreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf != null) {
            segmentByteBuf.release();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, CephPreParsedFrame frame, List<Object> list) throws Exception {
        ControlFrame controlFrame = frame.getMessageType().getInstance();
        if (frame.getSegment1() != null) {
            controlFrame.decodeSegment1(frame.getSegment1().getSegmentByteBuf(), frame.getSegment1().isLE());
            frame.getSegment1().getSegmentByteBuf().release();
        }
        if (frame.getSegment2() != null) {
            controlFrame.decodeSegment2(frame.getSegment2().getSegmentByteBuf(), frame.getSegment2().isLE());
            frame.getSegment2().getSegmentByteBuf().release();
        }
        if (frame.getSegment3() != null) {
            controlFrame.decodeSegment3(frame.getSegment3().getSegmentByteBuf(), frame.getSegment3().isLE());
            frame.getSegment3().getSegmentByteBuf().release();
        }
        if (frame.getSegment4() != null) {
            controlFrame.decodeSegment4(frame.getSegment4().getSegmentByteBuf(), frame.getSegment4().isLE());
            frame.getSegment4().getSegmentByteBuf().release();
        }

        frame.getHeaderByteBuf().release();

        list.add(controlFrame);
    }
}
