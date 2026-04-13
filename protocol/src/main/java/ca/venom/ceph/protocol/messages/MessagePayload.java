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

import java.util.BitSet;

@CephType
public abstract class MessagePayload {
    public short getHeadVersion(BitSet features) {
        return 1;
    }

    public short getHeadCompatVersion(BitSet features) {
        return 0;
    }

    public void prepareForEncode(ByteBuf byteBuf, boolean le, BitSet features) {
    }

    public void prepareForDecode(CephMsgHeader2 header) {
    }

    public void finishDecode(BitSet features, short version) {
    }

    public void encodeMiddle(ByteBuf byteBuf, boolean le, BitSet features) {
    }

    public void decodeMiddle(ByteBuf byteBuf, boolean le, BitSet features) {
    }

    public void encodeData(ByteBuf byteBuf, boolean le, BitSet features) {
    }

    public void decodeData(ByteBuf byteBuf, boolean le, BitSet features) {
    }
}
