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

public enum MessageType {
    CEPH_MSG_SHUTDOWN(1, null),
    CEPH_MSG_PING(2, null),

    CEPH_MSG_MON_MAP(4, null),
    CEPH_MSG_MON_GET_MAP(5, null),
    CEPH_MSG_MON_GET_OSDMAP(6, null),
    CEPH_MSG_MON_METADATA(7, null),
    CEPH_MSG_STATFS(13, null),
    CEPH_MSG_STATFS_REPLY(14, null),
    CEPH_MSG_MON_SUBSCRIBE(15, null),
    CEPH_MSG_MON_SUBSCRIBE_ACK(16, null),
    CEPH_MSG_AUTH(17, null),
    CEPH_MSG_AUTH_REPLY(18, null),
    CEPH_MSG_MON_GET_VERSION(19, null),
    CEPH_MSG_MON_GET_VERSION_REPLY(20, null),

    // client <-> mds
    CEPH_MSG_MDS_MAP(21, null),

    CEPH_MSG_CLIENT_SESSION(22, null),
    CEPH_MSG_CLIENT_RECONNECT(23, null),

    CEPH_MSG_CLIENT_REQUEST(24, null),
    CEPH_MSG_CLIENT_REQUEST_FORWARD(25, null),
    CEPH_MSG_CLIENT_REPLY(26, null),
    CEPH_MSG_CLIENT_RECLAIM(27, null),
    CEPH_MSG_CLIENT_RECLAIM_REPLY(28, null),
    CEPH_MSG_CLIENT_METRICS(29, null),
    CEPH_MSG_CLIENT_CAPS(0x310, null),
    CEPH_MSG_CLIENT_LEASE(0x311, null),
    CEPH_MSG_CLIENT_SNAP(0x312, null),
    CEPH_MSG_CLIENT_CAPRELEASE(0x313, null),
    CEPH_MSG_CLIENT_QUOTA(0x314, null),

    // pool ops
    CEPH_MSG_POOLOP_REPLY(48, null),
    CEPH_MSG_POOLOP(49, null),

    // osd
    CEPH_MSG_OSD_MAP(41, null),
    CEPH_MSG_OSD_OP(42, null),
    CEPH_MSG_OSD_OPREPLY(43, null),
    CEPH_MSG_WATCH_NOTIFY(44, null),
    CEPH_MSG_OSD_BACKOFF(61, null),

    // FSMap subscribers (see all MDS clusters at once)
    CEPH_MSG_FS_MAP(45, null),
    // FSMapUser subscribers (get MDS clusters name->ID mapping)
    CEPH_MSG_FS_MAP_USER(103, null);

    private final int tagNum;
    private final Class<? extends MessageBase> clazz;

    private MessageType(int tagNum, Class<? extends MessageBase> clazz) {
        this.tagNum = tagNum;
        this.clazz = clazz;
    }

    public static MessageType getFromTagNum(int tagNum) {
        for (MessageType controlFrameType : MessageType.values()) {
            if (controlFrameType.tagNum == tagNum) {
                return controlFrameType;
            }
        }

        return null;
    }

    public int getTagNum() {
        return tagNum;
    }

    public MessageBase getInstance() {
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception ex) {
            return null;
        }
    }
}
