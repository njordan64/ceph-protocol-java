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
import ca.venom.ceph.protocol.types.mds.CapItem;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;
import java.util.List;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/MClientCapRelease.h#L22
 */
@CephType
@CephMessagePayload(MessageType.MSG_CLIENT_CAPRELEASE)
public class MClientCapRelease extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private List<CapItem> caps;

    @Getter
    @Setter
    @CephField(order = 2)
    private int osdEpochBarrier = 0;

    @Override
    public short getHeadVersion(BitSet features) {
        return 2;
    }

    @Override
    public short getHeadCompatVersion(BitSet features) {
        return 1;
    }
}
