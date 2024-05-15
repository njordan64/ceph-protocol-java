/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.decode;

import ca.venom.ceph.protocol.ControlFrameType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class PreParsedFrame {
    public static class Segment {
        @Getter
        private final ByteBuf segmentByteBuf;
        @Getter
        private final int length;
        @Getter
        private final boolean le;

        public Segment(ByteBuf segmentByteBuf, int length, boolean le) {
            this.segmentByteBuf = segmentByteBuf;
            this.length = length;
            this.le = le;
        }
    }

    @Getter
    @Setter
    private ByteBuf headerByteBuf;
    private ControlFrameType controlFrameType;
    @Getter
    @Setter
    private boolean earlyDataCompressed;
    @Getter
    @Setter
    private Segment segment1;
    @Getter
    @Setter
    private Segment segment2;
    @Getter
    @Setter
    private Segment segment3;
    @Getter
    @Setter
    private Segment segment4;
    @Getter
    @Setter
    private boolean needHeaderChecksum;

    public ControlFrameType getMessageType() {
        return controlFrameType;
    }

    public void setMessageType(ControlFrameType controlFrameType) {
        this.controlFrameType = controlFrameType;
    }
}
