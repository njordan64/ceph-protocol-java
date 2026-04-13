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

import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephRelease;
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.protocol.types.mon.MonMap;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;
import java.util.List;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/MMonProbe.h
 */
@CephType
@CephMessagePayload(MessageType.MSG_MON_PROBE)
public class MMonProbe extends MessagePayload {
    @Getter
    @Setter
    @CephField(minVersion = 5, maxVersion = 5)
    @CephField(minVersion = 6, maxVersion = 6)
    @CephField(minVersion = 7, maxVersion = 7)
    @CephField(minVersion = 8, maxVersion = 8)
    private CephUUID fsid;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 5, maxVersion = 5)
    @CephField(order = 2, minVersion = 6, maxVersion = 6)
    @CephField(order = 2, minVersion = 7, maxVersion = 7)
    @CephField(order = 2, minVersion = 8, maxVersion = 8)
    private int op;

    @Getter
    @Setter
    @CephField(order = 3, minVersion = 5, maxVersion = 5)
    @CephField(order = 3, minVersion = 6, maxVersion = 6)
    @CephField(order = 3, minVersion = 7, maxVersion = 7)
    @CephField(order = 3, minVersion = 8, maxVersion = 8)
    private String name;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 5, maxVersion = 5)
    @CephField(order = 4, minVersion = 6, maxVersion = 6)
    @CephField(order = 4, minVersion = 7, maxVersion = 7)
    @CephField(order = 4, minVersion = 8, maxVersion = 8)
    private List<Integer> quorum;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true, minVersion = 5, maxVersion = 5)
    @CephField(order = 5, includeSize = true, minVersion = 6, maxVersion = 6)
    @CephField(order = 5, includeSize = true, minVersion = 7, maxVersion = 7)
    @CephField(order = 5, includeSize = true, minVersion = 8, maxVersion = 8)
    private MonMap monMap;

    @Getter
    @Setter
    @CephField(order = 6, includeSize = true, minVersion = 5, maxVersion = 5)
    @CephField(order = 6, includeSize = true, minVersion = 6, maxVersion = 6)
    @CephField(order = 6, includeSize = true, minVersion = 7, maxVersion = 7)
    @CephField(order = 6, includeSize = true, minVersion = 8, maxVersion = 8)
    private boolean hasEverJoined;

    @Getter
    @Setter
    @CephField(order = 7, minVersion = 5, maxVersion = 5)
    @CephField(order = 7, minVersion = 6, maxVersion = 6)
    @CephField(order = 7, minVersion = 7, maxVersion = 7)
    @CephField(order = 7, minVersion = 8, maxVersion = 8)
    private long paxosFirstVersion;

    @Getter
    @Setter
    @CephField(order = 8, minVersion = 5, maxVersion = 5)
    @CephField(order = 8, minVersion = 6, maxVersion = 6)
    @CephField(order = 8, minVersion = 7, maxVersion = 7)
    @CephField(order = 8, minVersion = 8, maxVersion = 8)
    private long paxosLastVersion;

    @Getter
    @Setter
    @CephField(order = 9, minVersion = 6, maxVersion = 6)
    @CephField(order = 9, minVersion = 7, maxVersion = 7)
    @CephField(order = 9, minVersion = 8, maxVersion = 8)
    private long requiredFeatures;

    @Getter
    @Setter
    @CephField(order = 10, minVersion = 7, maxVersion = 7)
    @CephField(order = 10, minVersion = 8, maxVersion = 8)
    @CephEncodingSize
    private CephRelease monRelease = CephRelease.UNKNOWN;

    @Getter
    @Setter
    @CephField(order = 11, minVersion = 8, maxVersion = 8)
    private int leader;

    @Override
    public short getHeadVersion(BitSet features) {
        return 8;
    }

    @Override
    public short getHeadCompatVersion(BitSet features) {
        return 5;
    }

    @Override
    public void finishDecode(BitSet features, short version) {
        if (version < 6) {
            requiredFeatures = 0L;
        }

        if (version < 7) {
            monRelease = CephRelease.UNKNOWN;
        }

        if (version < 8 && !quorum.isEmpty()) {
            leader = quorum.get(0);
        }
    }
}
