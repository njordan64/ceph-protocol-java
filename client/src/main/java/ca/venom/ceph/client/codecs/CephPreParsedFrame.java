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

import ca.venom.ceph.protocol.ControlFrameType;
import io.netty.buffer.ByteBuf;

public class CephPreParsedFrame {
    public static class Segment {
        private final ByteBuf segmentByteBuf;
        private final int length;
        private final boolean le;

        public Segment(ByteBuf segmentByteBuf, int length, boolean le) {
            this.segmentByteBuf = segmentByteBuf;
            this.length = length;
            this.le = le;
        }

        public ByteBuf getSegmentByteBuf() {
            return segmentByteBuf;
        }

        public int getLength() {
            return length;
        }

        public boolean isLE() {
            return le;
        }
    }

    private ByteBuf headerByteBuf;
    private ControlFrameType controlFrameType;
    private boolean earlyDataCompressed;
    private Segment segment1;
    private Segment segment2;
    private Segment segment3;
    private Segment segment4;
    private boolean needHeaderChecksum;

    public ByteBuf getHeaderByteBuf() {
        return headerByteBuf;
    }

    public void setHeaderByteBuf(ByteBuf headerByteBuf) {
        this.headerByteBuf = headerByteBuf;
    }

    public ControlFrameType getMessageType() {
        return controlFrameType;
    }

    public void setMessageType(ControlFrameType controlFrameType) {
        this.controlFrameType = controlFrameType;
    }

    public boolean isEarlyDataCompressed() {
        return earlyDataCompressed;
    }

    public void setEarlyDataCompressed(boolean earlyDataCompressed) {
        this.earlyDataCompressed = earlyDataCompressed;
    }

    public Segment getSegment1() {
        return segment1;
    }

    public void setSegment1(Segment segment1) {
        this.segment1 = segment1;
    }

    public Segment getSegment2() {
        return segment2;
    }

    public void setSegment2(Segment segment2) {
        this.segment2 = segment2;
    }

    public Segment getSegment3() {
        return segment3;
    }

    public void setSegment3(Segment segment3) {
        this.segment3 = segment3;
    }

    public Segment getSegment4() {
        return segment4;
    }

    public void setSegment4(Segment segment4) {
        this.segment4 = segment4;
    }

    public boolean isNeedHeaderChecksum() {
        return needHeaderChecksum;
    }

    public void setNeedHeaderChecksum(boolean needHeaderChecksum) {
        this.needHeaderChecksum = needHeaderChecksum;
    }
}
