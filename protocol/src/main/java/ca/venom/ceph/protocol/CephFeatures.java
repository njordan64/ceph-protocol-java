/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol;

import java.util.BitSet;

/*
17, 31, 34, 38, 44, 45, 49, 50, 51, 52, 53, 54, 55, 62, 63
 */
public enum CephFeatures {
    UID(0),
    NOSRCADDR(1),
    SERVER_NAUTILUS(2),
    FLOCK(3),
    SUBSCRIBE2(4),
    MONNAMES(5),
    RECONNECT_SEQ(6),
    DIRLAYOUTHASH(7),
    OBJECTLOCATOR(8),
    PGID64(9),
    INCSUBOSDMAP(10),
    PGPOOL3(11),
    OSDREPLYMUX(12),
    OSDENC(13),
    SERVER_KRAKEN(14),
    MONENC(15),
    SERVER_OCTOPUS(16),
    OSD_REPOP_MLCOD(16),
    OS_PERF_STAT_NS(17),
    CRUSH_TUNABLES(18),
    OSD_PGLOG_HARDLIMIT(19),
    SERVER_PACIFIC(20),
    SERVER_LUMINOUS(21),
    RESEND_ON_SPLIT(21),
    RADOS_BACKOFF(21),
    OSDMAP_PG_UPMAP(21),
    CRUSH_CHOOSE_ARGS(21),
    OSD_FIXED_COLLECTION_LIST(22),
    MSG_AUTH(23),
    RECOVERY_RESERVATION_2(24),
    CRUSH_TUNABLES2(25),
    CREATEPOOLID(26),
    REPLY_CREATE_INODE(27),
    SERVER_MIMIC(28),
    MDSENC(29),
    OSDHASHPSPOOL(30),
    MON_SINGLE_PAXOS(31),
    STRETCH_MODE(32),
    SERVER_QUINCY(33),
    OSD_PACKED_RECOVERY(34),
    OSD_CACHEPOOL(35),
    CRUSH_V2(36),
    EXPORT_PEER(37),
    OSD_ERASURE_CODES(38),
    OSDMAP_ENC(39),
    MDS_INLINE_DATA(40),
    CRUSH_TUNABLES3(41),
    OSD_PRIMARY_AFFINITY(41),
    MSGR_KEEPALIVE2(42),
    OSD_POOLRESEND(43),
    ERASURE_CODE_PLUGINS_V2(44),
    OSD_SET_ALLOC_HINT(45),
    OSD_FADVISE_FLAGS(46),
    MDS_QUOTA(47),
    CRUSH_V4(48),
    OSD_MIN_SIZE_RECOVERY(49),
    OSD_PROXY_FEATURES(49),
    MON_METADATA(50),
    OSD_BITWISE_HOBJ_SORT(51),
    OSD_PROXY_WRITE_FEATURES(52),
    ERASURE_CODE_PLUGINS_V3(53),
    OSD_HITSET_GMT(54),
    HAMMER_0_94_4(55),
    NEW_OSDOP_ENCODING(56),
    MON_STATEFUL_SUB(57),
    SERVER_JEWEL(57),
    CRUSH_TUNABLES5(58),
    NEW_OSDOPREPLY_ENCODING(58),
    FS_FILE_LAYOUT_V2(58),
    FS_BTIME(59),
    FS_CHANGE_ATTR(59),
    MSG_ADDR2(59),
    OSD_RECOVERY_DELETES(60),
    CEPHX_V2(61),
    RESERVED(62),
    RESERVED_BROKEN(63);

    public static final BitSet ALL = new BitSet(64);
    static {
        UID.enable(ALL);
        NOSRCADDR.enable(ALL);
        FLOCK.enable(ALL);
        SUBSCRIBE2.enable(ALL);
        MONNAMES.enable(ALL);
        RECONNECT_SEQ.enable(ALL);
        DIRLAYOUTHASH.enable(ALL);
        OBJECTLOCATOR.enable(ALL);
        PGID64.enable(ALL);
        INCSUBOSDMAP.enable(ALL);
        PGPOOL3.enable(ALL);
        OSDREPLYMUX.enable(ALL);
        OSDENC.enable(ALL);
        MONENC.enable(ALL);
        CRUSH_TUNABLES.enable(ALL);
        MSG_AUTH.enable(ALL);
        CRUSH_TUNABLES2.enable(ALL);
        CREATEPOOLID.enable(ALL);
        REPLY_CREATE_INODE.enable(ALL);
        MDSENC.enable(ALL);
        OSDHASHPSPOOL.enable(ALL);
        NEW_OSDOP_ENCODING.enable(ALL);
        NEW_OSDOPREPLY_ENCODING.enable(ALL);
        OSD_CACHEPOOL.enable(ALL);
        CRUSH_V2.enable(ALL);
        EXPORT_PEER.enable(ALL);
        OSDMAP_ENC.enable(ALL);
        MDS_INLINE_DATA.enable(ALL);
        CRUSH_TUNABLES3.enable(ALL);
        OSD_PRIMARY_AFFINITY.enable(ALL);
        MSGR_KEEPALIVE2.enable(ALL);
        OSD_POOLRESEND.enable(ALL);
        OSD_FADVISE_FLAGS.enable(ALL);
        MDS_QUOTA.enable(ALL);
        CRUSH_V4.enable(ALL);
        MON_STATEFUL_SUB.enable(ALL);
        CRUSH_TUNABLES5.enable(ALL);
        SERVER_JEWEL.enable(ALL);
        FS_FILE_LAYOUT_V2.enable(ALL);
        SERVER_KRAKEN.enable(ALL);
        FS_BTIME.enable(ALL);
        FS_CHANGE_ATTR.enable(ALL);
        MSG_ADDR2.enable(ALL);
        SERVER_LUMINOUS.enable(ALL);
        RESEND_ON_SPLIT.enable(ALL);
        RADOS_BACKOFF.enable(ALL);
        OSD_RECOVERY_DELETES.enable(ALL);
        SERVER_MIMIC.enable(ALL);
        RECOVERY_RESERVATION_2.enable(ALL);
        SERVER_NAUTILUS.enable(ALL);
        CEPHX_V2.enable(ALL);
        OSD_PGLOG_HARDLIMIT.enable(ALL);
        SERVER_OCTOPUS.enable(ALL);
        SERVER_MIMIC.enable(ALL);
        SERVER_JEWEL.enable(ALL);
        STRETCH_MODE.enable(ALL);
        OSD_REPOP_MLCOD.enable(ALL);
        SERVER_PACIFIC.enable(ALL);
        OSD_FIXED_COLLECTION_LIST.enable(ALL);
        SERVER_QUINCY.enable(ALL);
        ALL.set(34, true); // Shows up in ClientIdent???
    }

    private int bitPosition;

    private CephFeatures(int bitPosition) {
        this.bitPosition = bitPosition;
    }

    public boolean isEnabled(BitSet bitSet) {
        return bitSet.get(bitPosition);
    }

    public void enable(BitSet bitSet) {
        bitSet.set(bitPosition, true);
    }

    public void disable(BitSet bitSet) {
        bitSet.set(bitPosition, false);
    }
}
