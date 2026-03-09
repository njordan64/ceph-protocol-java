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
import ca.venom.ceph.protocol.types.auth.AuthRequestPayload;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/3b600d625b30c5b8f7864c13307e67bba2ed815e/src/msg/async/frames_v2.h#L532
 */
public class AuthRequestFrame extends AuthFrameBase {
    @CephType
    public static class Segment1 {
        @Getter
        @Setter
        @CephField
        private int authMethod;

        @Getter
        @Setter
        @CephField(order = 2)
        private List<Integer> preferredModes = new ArrayList<>();

        @Getter
        @Setter
        @CephField(order = 3)
        private AuthRequestPayload payload;
    }

    @Getter
    @Setter
    private Segment1 segment1;

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le, BitSet features) throws EncodingException {
        CephEncoder.encode(segment1, byteBuf, le, features);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le, BitSet features) throws DecodingException {
        segment1 = CephDecoder.decode(byteBuf, le, features, Segment1.class);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_REQUEST;
    }
}
