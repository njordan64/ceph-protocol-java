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
import ca.venom.ceph.protocol.types.auth.AuthRequestMorePayload;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/msg/async/frames_v2.h#L579
 */
public class AuthRequestMoreFrame extends AuthFrameBase {
    @Getter
    @Setter
    private AuthRequestMorePayload authPayload;

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le, BitSet features) throws EncodingException {
        CephEncoder.encode(authPayload, byteBuf, le, features);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le, BitSet features) throws DecodingException {
        authPayload = CephDecoder.decode(byteBuf, le, features, AuthRequestMorePayload.class);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_REQUEST_MORE;
    }
}
