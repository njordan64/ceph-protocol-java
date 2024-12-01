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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/mds/mdstypes.h#L275C8-L275C13
 */
@EqualsAndHashCode
@CephType
public class Vinodeno {
    @Getter
    @Setter
    @CephField
    private long inodeNo;

    @Getter
    @Setter
    @CephField(order = 2)
    private long snapId;
}
