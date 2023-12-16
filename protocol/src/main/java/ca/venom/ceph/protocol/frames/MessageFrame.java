/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.EncodingException;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class MessageFrame extends ControlFrame {
    @CephType
    public static class Segment {
        @Getter
        @Setter
        @CephField(includeSize = true)
        private byte[] encodedBytes;

        @Getter
        @Setter
        private boolean le;
    }

    @Getter
    @Setter
    private Segment head;

    @Getter
    @Setter
    private Segment front;

    @Getter
    @Setter
    private Segment middle;

    @Getter
    @Setter
    private Segment data;

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.MESSAGE;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        CephEncoder.encode(head, byteBuf, le);
    }

    @Override
    public void encodeSegment2(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (front != null && front.encodedBytes.length > 0) {
            CephEncoder.encode(front, byteBuf, le);
        }
    }

    @Override
    public void encodeSegment3(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (middle != null && middle.encodedBytes.length > 0) {
            CephEncoder.encode(middle, byteBuf, le);
        }
    }

    @Override
    public void encodeSegment4(ByteBuf byteBuf, boolean le) throws EncodingException {
        if (data != null && data.encodedBytes.length > 0) {
            CephEncoder.encode(data, byteBuf, le);
        }
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        head = CephDecoder.decode(byteBuf, le, Segment.class);
        head.setLe(le);
    }

    @Override
    public void decodeSegment2(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (byteBuf.readableBytes() > 0) {
            front = CephDecoder.decode(byteBuf, le, Segment.class);
            front.setLe(le);
        }
    }

    @Override
    public void decodeSegment3(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (byteBuf.readableBytes() > 0) {
            middle = CephDecoder.decode(byteBuf, le, Segment.class);
            middle.setLe(le);
        }
    }

    @Override
    public void decodeSegment4(ByteBuf byteBuf, boolean le) throws DecodingException {
        if (byteBuf.readableBytes() > 0) {
            data = CephDecoder.decode(byteBuf, le, Segment.class);
            data.setLe(le);
        }
    }
}
