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

import ca.venom.ceph.protocol.decode.PreParsedFrame;
import ca.venom.ceph.protocol.frames.ControlFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CephFrameCodec extends MessageToMessageCodec<PreParsedFrame, ControlFrame> {
    private static final Logger LOG = LoggerFactory.getLogger(CephFrameCodec.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ControlFrame controlFrame, List<Object> list) throws Exception {
        LOG.debug(">>> CephFrameCodec.encode: (" + controlFrame.getTag().name() + ")");

        PreParsedFrame frame = new PreParsedFrame();
        frame.setMessageType(controlFrame.getTag());

        ByteBuf segmentByteBuf = ctx.alloc().buffer();
        controlFrame.encodeSegment1(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment1(new PreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf == null) {
            segmentByteBuf = ctx.alloc().buffer();
        }
        controlFrame.encodeSegment2(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment2(new PreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf == null) {
            segmentByteBuf = ctx.alloc().buffer();
        }
        controlFrame.encodeSegment3(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment3(new PreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf == null) {
            segmentByteBuf = ctx.alloc().buffer();
        }
        controlFrame.encodeSegment4(segmentByteBuf, true);
        if (segmentByteBuf.writerIndex() > 0) {
            frame.setSegment4(new PreParsedFrame.Segment(segmentByteBuf, segmentByteBuf.writerIndex(), true));
            segmentByteBuf = null;
        }

        if (segmentByteBuf != null) {
            segmentByteBuf.release();
        }

        list.add(frame);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, PreParsedFrame frame, List<Object> list) throws Exception {
        LOG.debug(">>> CephFrameCodec.decode: (" + frame.getMessageType().name() + ")");

        ControlFrame controlFrame = frame.getMessageType().getInstance();
        if (frame.getSegment1() != null) {
            controlFrame.decodeSegment1(frame.getSegment1().getSegmentByteBuf(), frame.getSegment1().isLe());
            frame.getSegment1().getSegmentByteBuf().release();
        }
        if (frame.getSegment2() != null) {
            controlFrame.decodeSegment2(frame.getSegment2().getSegmentByteBuf(), frame.getSegment2().isLe());
            frame.getSegment2().getSegmentByteBuf().release();
        }
        if (frame.getSegment3() != null) {
            controlFrame.decodeSegment3(frame.getSegment3().getSegmentByteBuf(), frame.getSegment3().isLe());
            frame.getSegment3().getSegmentByteBuf().release();
        }
        if (frame.getSegment4() != null) {
            controlFrame.decodeSegment4(frame.getSegment4().getSegmentByteBuf(), frame.getSegment4().isLe());
            frame.getSegment4().getSegmentByteBuf().release();
        }

        frame.getHeaderByteBuf().release();

        list.add(controlFrame);
    }
}
