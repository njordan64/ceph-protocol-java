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

import ca.venom.ceph.encoding.annotations.CephChildType;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephParentType;
import ca.venom.ceph.encoding.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/cephfs/metrics/Types.h#L95
 */
@CephType
@CephParentType(typeSize = 6)
@CephChildType(typeValue = 0, typeClass = CapInfoPayload.class)
@CephChildType(typeValue = 1, typeClass = ReadLatencyPayload.class)
@CephChildType(typeValue = 2, typeClass = WriteLatencyPayload.class)
@CephChildType(typeValue = 3, typeClass = MetadataLatencyPayload.class)
@CephChildType(typeValue = 4, typeClass = OpenedFilesPayload.class)
@CephChildType(typeValue = 5, typeClass = PinnedIcapsPayload.class)
@CephChildType(typeValue = 6, typeClass = OpenedInodesPayload.class)
public abstract class ClientMetricPayload {
    @Getter
    @Setter
    @CephField(order = -1)
    private ClientMetricType type;

    protected ClientMetricPayload(ClientMetricType type) {
        this.type = type;
    }
}
