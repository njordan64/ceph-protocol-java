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

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.EncodingException;
import io.netty.buffer.ByteBuf;

public abstract class ControlFrame {
    public abstract ControlFrameType getTag();

    public abstract void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException;

    public void encodeSegment2(ByteBuf byteBuf, boolean le) throws EncodingException {
    }

    public void encodeSegment3(ByteBuf byteBuf, boolean le) throws EncodingException {
    }

    public void encodeSegment4(ByteBuf byteBuf, boolean le) throws EncodingException {
    }

    public abstract void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException;

    public void decodeSegment2(ByteBuf byteBuf, boolean le) throws DecodingException {
    }

    public void decodeSegment3(ByteBuf byteBuf, boolean le) throws DecodingException {
    }

    public void decodeSegment4(ByteBuf byteBuf, boolean le) throws DecodingException {
    }
}
