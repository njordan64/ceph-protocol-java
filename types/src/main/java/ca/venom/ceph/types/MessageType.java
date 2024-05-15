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
    // monitor internal
    MSG_MON_SCRUB(64),
    MSG_MON_ELECTION(65),
    MSG_MON_PAXOS(66),
    MSG_MON_PROBE(67),
    MSG_MON_JOIN(68),
    MSG_MON_SYNC(69),
    MSG_MON_PING(140),

    // monitor <-> mon admin tool
    MSG_MON_COMMAND(50),
    MSG_MON_COMMAND_ACK(51),
    MSG_LOG(52),
    MSG_LOGACK(53),

    MSG_GETPOOLSTATS(58),
    MSG_GETPOOLSTATSREPLY(59),

    MSG_MON_GLOBAL_ID(60),

    MSG_ROUTE(47),
    MSG_FORWARD(46),

    MSG_PAXOS(40),

    MSG_CONFIG(62),
    MSG_GET_CONFIG(63),

    MSG_KV_DATA(54),

    MSG_MON_GET_PURGED_SNAPS(76),
    MSG_MON_GET_PURGED_SNAPS_REPLY(77),

    // osd internal
    MSG_OSD_PING(70),
    MSG_OSD_BOOT(71),
    MSG_OSD_FAILURE(72),
    MSG_OSD_ALIVE(73),
    MSG_OSD_MARK_ME_DOWN(74),
    MSG_OSD_FULL(75),
    MSG_OSD_MARK_ME_DEAD(123),

    MSG_OSD_PGTEMP(78),

    MSG_OSD_BEACON(79),

    MSG_OSD_PG_NOTIFY(80),
    MSG_OSD_PG_NOTIFY2(130),
    MSG_OSD_PG_QUERY(81),
    MSG_OSD_PG_QUERY2(131),
    MSG_OSD_PG_LOG(83),
    MSG_OSD_PG_REMOVE(84),
    MSG_OSD_PG_INFO(85),
    MSG_OSD_PG_INFO2(132),
    MSG_OSD_PG_TRIM(86),

    MSG_PGSTATS(87),
    MSG_PGSTATSACK(88),

    MSG_OSD_PG_CREATE(89),
    MSG_REMOVE_SNAPS(90),

    MSG_OSD_SCRUB(91),
    MSG_OSD_SCRUB_RESERVE(92),
    MSG_OSD_REP_SCRUB(93),

    MSG_OSD_PG_SCAN(94),
    MSG_OSD_PG_BACKFILL(95),
    MSG_OSD_PG_BACKILL_REMOVE(96),

    MSG_COMMAND(97),
    MSG_COMMAND_REPLY(98),

    MSG_OSD_BACKFILL_RESERVE(99),
    MSG_OSD_RECOVERY_RESERVE(150),
    MSG_OSD_FORCE_RESERVE(151),

    MSG_OSD_PG_PUSH(105),
    MSG_OSD_PG_PULL(106),
    MSG_OSD_PG_PUSH_REPLY(107),

    MSG_OSD_EC_WRITE(108),
    MSG_OSD_EC_WRITE_REPLY(109),
    MSG_OSD_EC_READ(110),
    MSG_OSD_EC_READ_REPLY(111),

    MSG_OSD_REPOP(112),
    MSG_OSD_REPOPREPLY(113),
    MSG_OSD_PG_UPDATE_LOG_MISSING(114),
    MSG_OSD_PG_UPDATE_LOG_MISSING_REPLY(115),

    MSG_OSD_PG_CREATED(116),
    MSG_OSD_REP_SCRUBMAP(117),
    MSG_OSD_PG_RECOVERY_DELETE(118),
    MSG_OSD_PG_RECOVERY_DELETE_REPLY(119),
    MSG_OSD_PG_CREATE2(120),
    MSG_OSD_SCRUB2(121),

    MSG_OSD_PG_READY_TO_MERGE(122),

    MSG_OSD_PG_LEASE(133),
    MSG_OSD_PG_LEASE_ACK(134),

    // *** MDS ***

    MSG_MDS_BEACON(100),
    MSG_MDS_PEER_REQUEST(101),
    MSG_MDS_TABLE_REQUEST(102),
    MSG_MDS_SCRUB(135),

    MSG_MDS_RESOLVE(0x200),
    MSG_MDS_RESOLVEACK(0x201),
    MSG_MDS_CACHEREJOIN(0x202),
    MSG_MDS_DISCOVER(0x203),
    MSG_MDS_DISCOVERREPLY(0x204),
    MSG_MDS_INODEUPDATE(0x205),
    MSG_MDS_DIRUPDATE(0x206),
    MSG_MDS_CACHEEXPIRE(0x207),
    MSG_MDS_DENTRYUNLINK(0x208),
    MSG_MDS_FRAGMENTNOTIFY(0x209),
    MSG_MDS_OFFLOAD_TARGETS(0x20a),
    MSG_MDS_DENTRYLINK(0x20c),
    MSG_MDS_FINDINO(0x20d),
    MSG_MDS_FINDINOREPLY(0x20e),
    MSG_MDS_OPENINO(0x20f),
    MSG_MDS_OPENINFOREPLY(0x210),
    MSG_MDS_SNAPUPDATE(0x211),
    MSG_MDS_FRAGMENTNOTIFYACK(0x212),
    MSG_MDS_LOCK(0x300),
    MSG_MDS_INODEFILECAPS(0x301),

    MSG_MDS_EXPORTDIRDISCOVER(0x449),
    MSG_MDS_EXPORTDIRDISCOVERACK(0x450),
    MSG_MDS_EXPORTDIRCANCEL(0x451),
    MSG_MDS_EXPORTDIRPREP(0x452),
    MSG_MDS_EXPORTDIRPREPACK(0x453),
    MSG_MDS_EXPORTDIRWARNING(0x454),
    MSG_MDS_EXPORTDIRWARNINGACK(0x455),
    MSG_MDS_EXPORTDIR(0x456),
    MSG_MDS_EXPORTDIRACK(0x457),
    MSG_MDS_EXPORTDIRNOTIFY(0x458),
    MSG_MDS_EXPORTDIRNOTIFYACK(0x459),
    MSG_MDS_EXPORTDIRFINISH(0x460),

    MSG_MDS_EXPORTCAPS(0x470),
    MSG_MDS_EXPORTCAPSACK(0x471),
    MSG_MDS_GATHERCAPS(0x472),

    MSG_MDS_HEARTBEAT(0x500),
    MSG_MDS_METRICS(0x501),
    MSG_MDS_PING(0x502),
    MSG_MDS_SCRUB_STATS(0x503),

    // *** genric ***
    MSG_TIMECHECK(0x600),
    MSG_MON_HEALTH(0x601),

    // *** Message::encode() crcflags bits ***
    MSG_CRC_DATA(1 << 0),
    MSG_CRC_HEADER(1 << 1),
    MSG_CRC_ALL(MSG_CRC_DATA.valueInt | MSG_CRC_HEADER.valueInt),

    // Special
    MSG_NOP(0x607),

    MSG_MON_HEALTH_CHECKS(0x608),
    MSG_TIMECHECK2(0x609),

    // *** ceph-mgr <-> OSD/MDS daemons ***
    MSG_MGR_OPEN(0x700),
    MSG_MGR_CONFIGURE(0x701),
    MSG_MGR_REPORT(0x702),

    // *** ceph-mgr <-> ceph-mon ***
    MSG_MGR_BEACON(0x703),

    // *** ceph-mon(MgrMonitor) -> OSD/MDS daemons ***
    MSG_MGR_MAP(0x704),

    // *** ceph-mon(MgrMonitor) -> ceph-mgr
    MSG_MGR_DIGEST(0x705),
    // *** cephmgr -> ceph-mon
    MSG_MON_MGR_REPORT(0x706),
    MSG_SERVICE_MAP(0x707),

    MSG_MGR_CLOSE(0x708),
    MSG_MGR_COMMAND(0x709),
    MSG_MGR_COMMAND_REPLY(0x70a),

    MSG_SHUTDOWN(1),
    MSG_PING(2),

    MSG_MON_MAP(4),
    MSG_MON_GET_MAP(5),
    MSG_MON_GET_OSDMAP(6),
    MSG_MON_METADATA(7),
    MSG_STATFS(13),
    MSG_STATFS_REPLY(14),
    MSG_MON_SUBSCRIBE(15),
    MSG_MON_SUBSCRIBE_ACK(16),
    MSG_AUTH(17),
    MSG_AUTH_REPLY(18),
    MSG_MON_GET_VERSION(19),
    MSG_MON_GET_VERSION_REPLY(20),

    // client <-> mds
    MSG_MDS_MAP(21),

    MSG_CLIENT_SESSION(22),
    MSG_CLIENT_RECONNECT(23),

    MSG_CLIENT_REQUEST(24),
    MSG_CLIENT_REQUEST_FORWARD(25),
    MSG_CLIENT_REPLY(26),
    MSG_CLIENT_RECLAIM(27),
    MSG_CLIENT_RECLAIM_REPLY(28),
    MSG_CLIENT_METRICS(29),
    MSG_CLIENT_CAPS(0x310),
    MSG_CLIENT_LEASE(0x311),
    MSG_CLIENT_SNAP(0x312),
    MSG_CLIENT_CAPRELEASE(0x313),
    MSG_CLIENT_QUOTA(0x314),

    // pool ops
    MSG_POOLOP_REPLY(48),
    MSG_POOLOP(49),

    // osd
    MSG_OSD_MAP(41),
    MSG_OSD_OP(42),
    MSG_OSD_OPREPLY(43),
    MSG_WATCH_NOTIFY(44),
    MSG_OSD_BACKOFF(61),

    // FSMap subscribers (see all MDS clusters at once)
    MSG_FS_MAP(45),
    // FSMapUser subscribers (get MDS clusters name->ID mapping)
    MSG_FS_MAP_USER(103);

    @Getter
    private final int valueInt;

    MessageType(int valueInt) {
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
