/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/ceph_fs.h#L268
 */
@CephType
public class MonSubscribeItem {
    @Getter
    @Setter
    @CephField
    private long start;

    @Getter
    @Setter
    @CephField(order = 2)
    @CephEncodingSize
    private BitSet flags;
}
