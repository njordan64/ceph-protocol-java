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
import ca.venom.ceph.protocol.types.mds.Dirfrag;
import ca.venom.ceph.protocol.types.mds.Vinodeno;
import ca.venom.ceph.types.MessageType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MCacheExpire.h#L22
 */
@CephType
@CephMessagePayload(MessageType.MSG_MDS_CACHEEXPIRE)
public class MCacheExpire extends MessagePayload {
    /**
     * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MCacheExpire.h#L34
     */
    @EqualsAndHashCode
    @CephType
    public static class DentryKey {
        @Getter
        @Setter
        @CephField
        private String name;

        @Getter
        @Setter
        @CephField(order = 2)
        private long snapId;
    }

    /**
     * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MCacheExpire.h#L31
     */
    @CephType
    public static class Realm {
        @Getter
        @Setter
        @CephField
        private Map<Vinodeno, Integer> inodes;

        @Getter
        @Setter
        @CephField(order = 2)
        private Map<Dirfrag, Integer> dirs;

        @Getter
        @Setter
        @CephField(order = 3)
        private Map<Dirfrag, Map<DentryKey, Integer>> dentries;
    }

    @Getter
    @Setter
    @CephField
    private short from;

    @Getter
    @Setter
    @CephField(order = 2)
    private Map<Dirfrag, Realm> realms;
}
