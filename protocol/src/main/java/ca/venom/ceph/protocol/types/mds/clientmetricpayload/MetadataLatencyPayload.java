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
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import ca.venom.ceph.protocol.types.UTime;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/cephfs/metrics/Types.h#L255
 */
@Getter
@Setter
@CephType
@CephTypeVersion(version = 1, compatVersion = 1)
@CephTypeSize
public class MetadataLatencyPayload extends ClientMetricPayload {
    @CephField(order = 2)
    private UTime lat;

    public MetadataLatencyPayload() {
        super(ClientMetricType.METADATA_LATENCY);
    }
}
