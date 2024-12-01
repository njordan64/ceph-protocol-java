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
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/ceph_fs.h#L928
 */
@CephType
public class Lease {
    @Getter
    @Setter
    @CephField
    private byte action;

    @Getter
    @Setter
    @CephField(order = 2)
    private short mask;

    @Getter
    @Setter
    @CephField(order = 3)
    private long ino;

    @Getter
    @Setter
    @CephField(order = 4)
    private long first;

    @Getter
    @Setter
    @CephField(order = 5)
    private long last;

    @Getter
    @Setter
    @CephField(order = 6)
    private int seq;

    @Getter
    @Setter
    @CephField(order = 7)
    private int durationMs;
}
