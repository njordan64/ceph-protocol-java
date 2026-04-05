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

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import ca.venom.ceph.encoding.annotations.CephTypeVersionGenerator;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.types.AddrVec;
import ca.venom.ceph.types.VersionWithCompat;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;
import java.util.Map;

@CephType
@CephTypeSize
@CephTypeVersionConstant(version = 5, compatVersion = 1)
public class MonInfo {
    @Getter
    @Setter
    @CephField(minVersion = 1, maxVersion = 1)
    @CephField(minVersion = 2, maxVersion = 3)
    @CephField(minVersion = 4, maxVersion = 4)
    @CephField(minVersion = 5, maxVersion = 5)
    private String name;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 1, maxVersion = 1)
    @CephField(order = 2, minVersion = 2, maxVersion = 3)
    @CephField(order = 2, minVersion = 4, maxVersion = 4)
    @CephField(order = 2, minVersion = 5, maxVersion = 5)
    private AddrVec publicAddrs;

    @Getter
    @Setter
    @CephField(order = 3, minVersion = 2, maxVersion = 3)
    @CephField(order = 3, minVersion = 4, maxVersion = 4)
    @CephField(order = 3, minVersion = 5, maxVersion = 5)
    private short priority;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 4, maxVersion = 4)
    @CephField(order = 4, minVersion = 5, maxVersion = 5)
    private short weight;

    @Getter
    @Setter
    @CephField(order = 5, minVersion = 5, maxVersion = 5)
    private Map<String, String> crushLoc;

    @CephTypeVersionGenerator
    public VersionWithCompat getVersion(BitSet features) {
        if (CephFeatures.SERVER_NAUTILUS.isEnabled(features)) {
            return new VersionWithCompat((byte) 2, (byte) 1);
        }

        return new VersionWithCompat((byte) 5, (byte) 1);
    }
}
