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

public class AckFrame extends ControlFrame {
    @CephType
    public static class Segment1 {
        @Getter
        @Setter
        @CephField
        long messageSequence;
    }

    @Getter
    @Setter
    private Segment1 segment1;

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        CephEncoder.encode(segment1, byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        segment1 = CephDecoder.decode(byteBuf, le, Segment1.class);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.ACK;
    }
}
