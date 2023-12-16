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

public class WaitFrame extends ControlFrame {
    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.WAIT;
    }
}
