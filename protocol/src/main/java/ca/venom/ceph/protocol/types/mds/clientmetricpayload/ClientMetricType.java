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

public enum ClientMetricType implements EnumWithIntValue {
    CAP_INFO(0),
    READ_LATENCY(1),
    WRITE_LATENCY(2),
    METADATA_LATENCY(3),
    DENTRY_LEASE(4),
    OPENED_FILES(5),
    PINNED_ICAPS(6),
    OPENED_INODES(7);

    private int valueInt;

    ClientMetricType(int valueInt) {
        this.valueInt = valueInt;
    }

    @Override
    public int getValueInt() {
        return valueInt;
    }
}
