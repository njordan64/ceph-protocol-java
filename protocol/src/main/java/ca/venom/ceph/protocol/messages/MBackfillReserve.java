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

import ca.venom.ceph.encoding.annotations.*;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.types.PlacementGroupId;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MBackfillReserve.h#L22
 */
@CephType
@CephMessagePayload(MessageType.MSG_OSD_BACKFILL_RESERVE)
public class MBackfillReserve extends MessagePayload {
    @Getter
    @Setter
    @CephField(minVersion = 3, maxVersion = 3)
    @CephField(minVersion = 4, maxVersion = 5)
    private PlacementGroupId pgId;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 3, maxVersion = 3)
    @CephField(order = 2, minVersion = 4, maxVersion = 5)
    private int queryEpoch;

    @Getter
    @Setter
    @CephField(order = 3, minVersion = 3, maxVersion = 3)
    @CephField(order = 3, minVersion = 4, maxVersion = 5)
    private int type;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 3, maxVersion = 3)
    @CephField(order = 4, minVersion = 4, maxVersion = 5)
    private int priority;

    @Getter
    @Setter
    @CephField(order = 5, minVersion = 3, maxVersion = 3)
    @CephField(order = 5, minVersion = 4, maxVersion = 5)
    private byte shardId;

    @Getter
    @Setter
    @CephField(order = 6, minVersion = 4, maxVersion = 5)
    private long primaryNumBytes;

    @Getter
    @Setter
    @CephField(order = 7, minVersion = 4, maxVersion = 5)
    private long shardNumBytes;

    @Override
    public short getHeadVersion(BitSet features) {
        if (!CephFeatures.RECOVERY_RESERVATION_2.isEnabled(features)) {
            return 3;
        }

        return 5;
    }

    @Override
    public short getHeadCompatVersion(BitSet features) {
        if (!CephFeatures.RECOVERY_RESERVATION_2.isEnabled(features)) {
            return 3;
        }

        return 4;
    }

    @CephFieldEncode(order = 3, minVersion = 3, maxVersion = 3)
    public void encodeType(ByteBuf byteBuf, boolean le, BitSet features) {
        if (type == 3 || type == 4 || type == 5) {
            CephEncoder.encode(2, byteBuf, le);
        }
    }
}
