/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.types;

import lombok.Getter;

public enum MessageType implements EnumWithIntValue {
    CEPH_MSG_SHUTDOWN(1),
    CEPH_MSG_PING(2),

    CEPH_MSG_MON_MAP(4),
    CEPH_MSG_MON_GET_MAP(5),
    CEPH_MSG_MON_GET_OSDMAP(6),
    CEPH_MSG_MON_METADATA(7),
    CEPH_MSG_STATFS(13),
    CEPH_MSG_STATFS_REPLY(14),
    CEPH_MSG_MON_SUBSCRIBE(15),
    CEPH_MSG_MON_SUBSCRIBE_ACK(16),
    CEPH_MSG_AUTH(17),
    CEPH_MSG_AUTH_REPLY(18),
    CEPH_MSG_MON_GET_VERSION(19),
    CEPH_MSG_MON_GET_VERSION_REPLY(20),

    // client <-> mds
    CEPH_MSG_MDS_MAP(21),

    CEPH_MSG_CLIENT_SESSION(22),
    CEPH_MSG_CLIENT_RECONNECT(23),

    CEPH_MSG_CLIENT_REQUEST(24),
    CEPH_MSG_CLIENT_REQUEST_FORWARD(25),
    CEPH_MSG_CLIENT_REPLY(26),
    CEPH_MSG_CLIENT_RECLAIM(27),
    CEPH_MSG_CLIENT_RECLAIM_REPLY(28),
    CEPH_MSG_CLIENT_METRICS(29),
    CEPH_MSG_CLIENT_CAPS(0x310),
    CEPH_MSG_CLIENT_LEASE(0x311),
    CEPH_MSG_CLIENT_SNAP(0x312),
    CEPH_MSG_CLIENT_CAPRELEASE(0x313),
    CEPH_MSG_CLIENT_QUOTA(0x314),

    // pool ops
    CEPH_MSG_POOLOP_REPLY(48),
    CEPH_MSG_POOLOP(49),

    // osd
    CEPH_MSG_OSD_MAP(41),
    CEPH_MSG_OSD_OP(42),
    CEPH_MSG_OSD_OPREPLY(43),
    CEPH_MSG_WATCH_NOTIFY(44),
    CEPH_MSG_OSD_BACKOFF(61),

    // FSMap subscribers (see all MDS clusters at once)
    CEPH_MSG_FS_MAP(45),
    // FSMapUser subscribers (get MDS clusters name->ID mapping)
    CEPH_MSG_FS_MAP_USER(103);

    @Getter
    private final int valueInt;

    private MessageType(int valueInt) {
        this.valueInt = valueInt;
    }

    public static MessageType getFromCode(int valueInt) {
        for (MessageType controlFrameType : MessageType.values()) {
            if (controlFrameType.valueInt == valueInt) {
                return controlFrameType;
            }
        }

        return null;
    }
}
