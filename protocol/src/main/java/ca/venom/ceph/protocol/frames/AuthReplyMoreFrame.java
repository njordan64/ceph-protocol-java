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
import ca.venom.ceph.protocol.types.auth.AuthReplyMorePayload;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class AuthReplyMoreFrame extends AuthFrameBase {
    @Getter
    @Setter
    private AuthReplyMorePayload payload;

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        CephEncoder.encode(payload, byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        payload = CephDecoder.decode(byteBuf, le, AuthReplyMorePayload.class);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_REPLY_MORE;
    }
}
