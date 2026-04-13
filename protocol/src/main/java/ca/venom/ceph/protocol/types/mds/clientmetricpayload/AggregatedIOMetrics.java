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

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/include/cephfs/metrics/Types.h#L643
 */
@CephType
@CephTypeVersionConstant(version = 1, compatVersion = 1)
@CephTypeSize
public class AggregatedIOMetrics {
    @Getter
    @Setter
    @CephField
    private long subvolumeId;

    @Getter
    @Setter
    @CephField(order = 2)
    private int readCount;

    @Getter
    @Setter
    @CephField(order = 3)
    private int writeCount;

    @Getter
    @Setter
    @CephField(order = 4)
    private long readBytes;

    @Getter
    @Setter
    @CephField(order = 5)
    private long writeBytes;

    @Getter
    @Setter
    @CephField(order = 6)
    private long readLatencyUs;

    @Getter
    @Setter
    @CephField(order = 7)
    private long writeLatencyUs;

    @Getter
    @Setter
    @CephField(order = 8)
    private long timeStamp;
}
