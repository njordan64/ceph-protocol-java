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
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/cephfs/metrics/Types.h#L95
 */
@Getter
@Setter
@CephType
@CephTypeVersion(version = 1, compatVersion = 1)
@CephTypeSize
public class CapInfoPayload extends ClientMetricPayload {
    @CephField(order = 2)
    private long capHits = 0;

    @CephField(order = 3)
    private long capMisses = 0;

    @CephField(order = 4)
    private long nrCaps = 0;

    public CapInfoPayload() {
        super(ClientMetricType.CAP_INFO);
    }
}
