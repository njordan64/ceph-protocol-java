/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.encoding.annotations.CephType;
import io.netty.buffer.ByteBuf;

@CephType
public abstract class MessagePayload {
    public short getHeadVersion(long features) {
        return 1;
    }

    public short getHeadCompatVersion(long features) {
        return 0;
    }

    public void prepareForEncode() {
    }

    public void finishDecode() {
    }

    public void encodeMiddle(ByteBuf byteBuf, boolean le, long features) {
    }

    public void decodeMiddle(ByteBuf byteBuf, boolean le, long features) {
    }

    public void encodeData(ByteBuf byteBuf, boolean le, long features) {
    }

    public void decodeData(ByteBuf byteBuf, boolean le, long features) {
    }
}
