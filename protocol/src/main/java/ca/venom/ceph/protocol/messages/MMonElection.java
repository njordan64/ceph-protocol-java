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
import ca.venom.ceph.protocol.CephRelease;
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.protocol.types.mon.MonFeature;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MMonElection.h#L24
 */
@CephType
@CephMessagePayload(MessageType.MSG_MON_ELECTION)
public class MMonElection extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private CephUUID fsid;

    @Getter
    @Setter
    @CephField(order = 2)
    private int op;

    @Getter
    @Setter
    @CephField(order = 2)
    private int epoch;

    @Getter
    @Setter
    @CephField(order = 3, includeSize = true)
    private byte[] monMap;

    @Getter
    @Setter
    @CephField(order = 4)
    private List<Integer> quorum;

    @Getter
    @Setter
    @CephField(order = 5)
    private long quorumFeatures;

    @Getter
    @Setter
    @CephField(order = 6)
    private long defunctField1;

    @Getter
    @Setter
    @CephField(order = 7)
    private long defunctField2;

    @Getter
    @Setter
    @CephField(order = 8, includeSize = true)
    private byte[] sharing;

    @Getter
    @Setter
    @CephField(order = 9)
    private MonFeature monFeatures;

    @Getter
    @Setter
    @CephField(order = 10)
    private Map<String, String> metadata;

    @Getter
    @Setter
    @CephField(order = 11)
    private CephRelease monRelease = CephRelease.UNKNOWN;

    @Getter
    @Setter
    @CephField(order = 12, includeSize = true)
    private byte[] scoring;

    @Getter
    @Setter
    @CephField(order = 13)
    private byte strategy;
}
