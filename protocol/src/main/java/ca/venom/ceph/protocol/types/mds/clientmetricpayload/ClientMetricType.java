/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.mds.clientmetricpayload;

import ca.venom.ceph.types.EnumWithIntValue;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/include/cephfs/metrics/Types.h#L20
 */
public enum ClientMetricType implements EnumWithIntValue {
    CAP_INFO(0),
    READ_LATENCY(1),
    WRITE_LATENCY(2),
    METADATA_LATENCY(3),
    DENTRY_LEASE(4),
    OPENED_FILES(5),
    PINNED_ICAPS(6),
    OPENED_INODES(7),
    READ_IO_SIZES(8),
    WRITE_IO_SIZES(9),
    AVG_READ_LATENCY(10),
    STDEV_READ_LATENCY(11),
    AVG_WRITE_LATENCY(12),
    STDEV_WRITE_LATENCY(13),
    AVG_METADATA_LATENCY(14),
    STDEV_METADATA_LATENCY(15),
    SUBVOLUME_METRICS(16),
    UNKNOWN(-1);

    private int valueInt;

    ClientMetricType(int valueInt) {
        this.valueInt = valueInt;
    }

    public static ClientMetricType getFromValueInt(int value) {
        for (ClientMetricType clientMetricType : values()) {
            if (clientMetricType.valueInt == value) {
                return clientMetricType;
            }
        }

        return null;
    }

    @Override
    public int getValueInt() {
        return valueInt;
    }
}
