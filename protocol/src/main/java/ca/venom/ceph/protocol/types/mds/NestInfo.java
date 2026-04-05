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
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import ca.venom.ceph.protocol.types.UTime;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/mds/mdstypes.h#L214
 */
@CephType
@CephTypeVersionConstant(version = 3, compatVersion = 2)
public class NestInfo {
    @Getter
    @Setter
    @CephField(minVersion = 2, maxVersion = 3)
    private long version;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 2, maxVersion = 3)
    private long rBytes = 0;

    @Getter
    @Setter
    @CephField(order = 3, minVersion = 2, maxVersion = 3)
    private long rFiles = 0;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 2, maxVersion = 3)
    private long rSubdirs = 0;

    @Getter
    @Setter
    @CephField(order = 5, minVersion = 2, maxVersion = 3)
    private long ranchors = 0;

    @Getter
    @Setter
    @CephField(order = 6, minVersion = 2, maxVersion = 3)
    private long rSnaps = 0;

    @Getter
    @Setter
    @CephField(order = 7, minVersion = 2, maxVersion = 3)
    private UTime rcTime;

    @CephFieldEncode(order = 5, minVersion = 2, maxVersion = 3)
    public void encodeRanchors(ByteBuf byteBuf, boolean le, BitSet features) {
        byteBuf.writeZero(8);
    }
}
