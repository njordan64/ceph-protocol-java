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

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephFieldDecode;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/MAuth.h#L27
 */
@CephType
@CephMessagePayload(MessageType.MSG_AUTH)
public class MAuth extends PaxosMessage {
    @Getter
    @Setter
    @CephField(order = 4)
    private int protocol;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true)
    private byte[] authPayload;

    @Getter
    @Setter
    @CephField(order = 6)
    private int monMapEpoch;

    @Override
    public short getHeadVersion(BitSet features) {
        return 0;
    }

    @CephFieldDecode(order = 6)
    public void decodeMonMapEpoch(ByteBuf byteBuf, boolean le, BitSet features) {
        if (byteBuf.writerIndex() >= byteBuf.readerIndex() + 4) {
            monMapEpoch = CephDecoder.decodeInt(byteBuf, le);
        }
    }
}
