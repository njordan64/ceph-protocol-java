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
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersionGenerator;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.types.AddrVec;
import ca.venom.ceph.protocol.types.CephAddr;
import ca.venom.ceph.protocol.types.TimeSpec;
import ca.venom.ceph.types.VersionWithCompat;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/mon/MonMap.h#L48
 */
@CephType
@CephTypeSize
public class MonInfo {
    @Getter
    @Setter
    @CephField(minVersion = 1, maxVersion = 1)
    @CephField(minVersion = 2, maxVersion = 2)
    @CephField(minVersion = 3, maxVersion = 3)
    @CephField(minVersion = 4, maxVersion = 4)
    @CephField(minVersion = 5, maxVersion = 5)
    @CephField(minVersion = 6, maxVersion = 6)
    private String name;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 1, maxVersion = 1)
    @CephField(order = 2, minVersion = 2, maxVersion = 2)
    @CephField(order = 2, minVersion = 3, maxVersion = 3)
    @CephField(order = 2, minVersion = 4, maxVersion = 4)
    @CephField(order = 2, minVersion = 5, maxVersion = 5)
    @CephField(order = 2, minVersion = 6, maxVersion = 6)
    private AddrVec publicAddrs;

    @Getter
    @Setter
    @CephField(order = 3, minVersion = 2, maxVersion = 2)
    @CephField(order = 3, minVersion = 3, maxVersion = 3)
    @CephField(order = 3, minVersion = 4, maxVersion = 4)
    @CephField(order = 3, minVersion = 5, maxVersion = 5)
    @CephField(order = 3, minVersion = 6, maxVersion = 6)
    private short priority;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 4, maxVersion = 4)
    @CephField(order = 4, minVersion = 5, maxVersion = 5)
    @CephField(order = 4, minVersion = 6, maxVersion = 6)
    private short weight;

    @Getter
    @Setter
    @CephField(order = 5, minVersion = 5, maxVersion = 5)
    @CephField(order = 5, minVersion = 6, maxVersion = 6)
    private Map<String, String> crushLoc;

    @Getter
    @Setter
    @CephField(order = 6, minVersion = 6, maxVersion = 6)
    private TimeSpec timeAdded = new TimeSpec();

    @CephTypeVersionGenerator
    public VersionWithCompat getVersion(BitSet features) {
        if (CephFeatures.SERVER_NAUTILUS.isEnabled(features)) {
            return new VersionWithCompat((byte) 2, (byte) 1);
        }

        return new VersionWithCompat((byte) 6, (byte) 1);
    }

    @CephTypeVersionGenerator
    public VersionWithCompat generateVersion(BitSet features) {
        byte version = 6;
        byte minVersion = 1;

        if (!crushLoc.isEmpty()) {
            minVersion = 1;
        }

        if (!CephFeatures.SERVER_NAUTILUS.isEnabled(features)) {
            version = 2;
        }

        return new VersionWithCompat(version, minVersion);
    }

    @CephFieldEncode(order = 2, minVersion = 1, maxVersion = 1)
    @CephFieldEncode(order = 2, minVersion = 2, maxVersion = 2)
    public void encodePublicAddrs(ByteBuf byteBuf, boolean le, BitSet features) {
        final CephAddr legacyAddr = publicAddrs.legacyAddr();
        final byte[] addrBytes = legacyAddr.getSocketAddress().getAddress().getAddress();
        final byte[] blankIPv4 = {0, 0, 0, 0};
        final byte[] blankIPv6 = {0, 0, 0, 0, 0, 0, 0, 0};
        if (((addrBytes.length == 4 && Arrays.equals(blankIPv4, addrBytes)) ||
                addrBytes.length == 8 && Arrays.equals(blankIPv6, addrBytes)) &&
                legacyAddr.getSocketAddress().getPort() == 0) {
            CephEncoder.encode(legacyAddr, byteBuf, le, features);
        } else {
            CephEncoder.encode(publicAddrs.asLegacyAddr(), byteBuf, le, features);
        }
    }
}
