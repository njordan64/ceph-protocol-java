/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/rados.h#L42
 */
@CephType
public class TimeSpec {
    @Getter
    @Setter
    @CephField
    private int tvSec;

    @Getter
    @Setter
    @CephField(order = 2)
    private int tvNSec;

    public UTime toUTime() {
        final UTime utime = new UTime();
        utime.setTvSec(tvSec);
        utime.setTvNSec(tvNSec);
        return utime;
    }
}
