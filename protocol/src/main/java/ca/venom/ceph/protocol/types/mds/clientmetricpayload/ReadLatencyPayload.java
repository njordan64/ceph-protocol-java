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
import ca.venom.ceph.protocol.types.UTime;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/include/cephfs/metrics/Types.h#L158
 */
@CephType
@CephTypeVersionConstant(version = 2, compatVersion = 1)
@CephTypeSize
public class ReadLatencyPayload extends ClientMetricPayload {
    @Getter
    @Setter
    @CephField(minVersion = 1, maxVersion = 1)
    @CephField(minVersion = 2, maxVersion = 2)
    private UTime lat;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 2, maxVersion = 2)
    private UTime mean;

    @Getter
    @Setter
    @CephField(order = 3, minVersion = 2, maxVersion = 2)
    private long sqSum;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 2, maxVersion = 2)
    private long count;

    public ReadLatencyPayload() {
        super(ClientMetricType.READ_LATENCY);
    }
}
