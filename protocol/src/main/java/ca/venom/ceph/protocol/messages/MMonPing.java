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
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.types.EnumWithIntValue;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/MMonPing.h
 */
@CephType
@CephMessagePayload(MessageType.MSG_MON_PING)
public class MMonPing extends MessagePayload {
    public enum PingOp implements EnumWithIntValue {
        PING(1, "ping"),
        PING_REPLY(2, "ping_reply");

        private final int valueInt;
        private final String name;

        PingOp(int valueInt, String name) {
            this.valueInt = valueInt;
            this.name = name;
        }

        public int getValueInt() {
            return valueInt;
        }

        public String getName() {
            return name;
        }

        public static PingOp getFromValueInt(int valueInt) {
            for (PingOp pingOp : values()) {
                if (pingOp.valueInt == valueInt) {
                    return pingOp;
                }
            }

            return null;
        }
    }

    @Getter
    @Setter
    private int minSize;

    private int startIndex;

    @Getter
    @Setter
    @CephField
    private PingOp op;

    @Getter
    @Setter
    @CephField(order = 2)
    private UTime stamp;

    @Getter
    @Setter
    @CephField(order = 3, includeSize = true)
    private byte[] tracker;

    @Override
    public short getHeadCompatVersion(BitSet features) {
        return 1;
    }

    @Override
    public void prepareForEncode(ByteBuf byteBuf, boolean le, BitSet features) {
        startIndex = byteBuf.writerIndex();
    }

    @CephFieldEncode(order = 4)
    public void encodeZeroes(ByteBuf byteBuf, boolean le, BitSet features) {
        int currentSize = byteBuf.writerIndex() - startIndex;
        if (minSize > currentSize) {
            CephEncoder.encode(minSize - currentSize, byteBuf, le);
            byteBuf.writeZero(minSize - currentSize);
        }
    }

    @CephFieldDecode(order = 4)
    public void decodeZeroes(ByteBuf byteBuf, boolean le, BitSet features) {
        int zeroesCount = CephDecoder.decodeInt(byteBuf, le);
        if (zeroesCount > 0) {
            byteBuf.writerIndex(byteBuf.writerIndex() + zeroesCount);
        }
    }
}
