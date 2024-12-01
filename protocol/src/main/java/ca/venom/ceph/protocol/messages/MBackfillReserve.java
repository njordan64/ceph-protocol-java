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
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.types.PlacementGroupId;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MBackfillReserve.h#L22
 */
@CephType
@CephMessagePayload(MessageType.MSG_OSD_BACKFILL_RESERVE)
public class MBackfillReserve extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private PlacementGroupId pgId;

    @Getter
    @Setter
    @CephField(order = 2)
    private int queryEpoch;

    @Getter
    @Setter
    @CephField(order = 3)
    private int type;

    @Getter
    @Setter
    @CephField(order = 4)
    private int priority;

    @Getter
    @Setter
    @CephField(order = 5)
    private byte shardId;

    @Getter
    @Setter
    @CephField(order = 6)
    private long primaryNumBytes;

    @Getter
    @Setter
    @CephField(order = 7)
    private long shardNumBytes;

    @Override
    public short getHeadVersion() {
        return 5;
    }

    @Override
    public short getHeadCompatVersion() {
        return 4;
    }
}
