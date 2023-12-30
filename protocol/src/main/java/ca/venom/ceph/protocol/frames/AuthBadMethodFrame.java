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

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.EncodingException;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AuthBadMethodFrame extends AuthFrameBase {
    @CephType
    public static class Segment1 {
        @Getter
        @Setter
        @CephField
        private int method;

        @Getter
        @Setter
        @CephField(order = 2)
        private int result;

        @Getter
        @Setter
        @CephField(order = 3)
        private List<Integer> allowedMethods;

        @Getter
        @Setter
        @CephField(order = 4)
        private List<Integer> allowedModes;
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
        return ControlFrameType.AUTH_BAD_METHOD;
    }
}
