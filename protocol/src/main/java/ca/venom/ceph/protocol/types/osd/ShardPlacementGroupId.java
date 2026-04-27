/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.osd;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/osd/osd_types.h#L522
 */
@CephType
@CephTypeVersionConstant(version = 1, compatVersion = 1)
@CephTypeSize
public class ShardPlacementGroupId {
    @Getter
    @Setter
    @CephField
    private PlacementGroupId pgid;

    @Getter
    @Setter
    @CephField(order = 2)
    private byte shard;
}
