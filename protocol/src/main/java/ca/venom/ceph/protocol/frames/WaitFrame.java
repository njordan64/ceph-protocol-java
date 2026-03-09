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
import io.netty.buffer.ByteBuf;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/3b600d625b30c5b8f7864c13307e67bba2ed815e/src/msg/async/frames_v2.h#L730
 */
public class WaitFrame extends ControlFrame {
    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le, BitSet features) {
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le, BitSet features) {
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.WAIT;
    }
}
