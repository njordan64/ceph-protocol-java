/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.mds;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephZeroPadToSizeOf;
import ca.venom.ceph.protocol.types.CephFileLayout;
import ca.venom.ceph.protocol.types.TimeSpec;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/ceph_fs.h#L894
 */
@CephType
@CephZeroPadToSizeOf(CapsExportBody.class)
public class CapsNonExportBody extends CapsBody {
    @Getter
    @Setter
    @CephField
    private long size;

    @Getter
    @Setter
    @CephField(order = 2)
    private long maxSize;

    @Getter
    @Setter
    @CephField(order = 3)
    private long truncateSize;

    @Getter
    @Setter
    @CephField(order = 4)
    private int truncateSeq;

    @Getter
    @Setter
    @CephField(order = 5)
    private TimeSpec mTime;

    @Getter
    @Setter
    @CephField(order = 6)
    private TimeSpec aTime;

    @Getter
    @Setter
    @CephField(order = 7)
    private TimeSpec cTime;

    @Getter
    @Setter
    @CephField(order = 8)
    private CephFileLayout layout;

    @Getter
    @Setter
    @CephField(order = 9)
    private int timeWarpSeq;
}
