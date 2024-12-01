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
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import ca.venom.ceph.protocol.types.UTime;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/mds/mdstypes.h#L214
 */
@CephType
@CephTypeVersion(version = 3, compatVersion = 2)
public class NestInfo {
    @Getter
    @Setter
    @CephField
    private long version;

    @Getter
    @Setter
    @CephField(order = 2)
    private long rBytes = 0;

    @Getter
    @Setter
    @CephField(order = 3)
    private long rFiles = 0;

    @Getter
    @Setter
    @CephField(order = 4)
    private long rSubdirs = 0;

    @Getter
    @Setter
    @CephField(order = 5)
    private long ranchors = 0;

    @Getter
    @Setter
    @CephField(order = 6)
    private long rSnaps = 0;

    @Getter
    @Setter
    @CephField(order = 7)
    private UTime rcTime;
}
