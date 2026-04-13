/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephFieldDecode;
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.types.mds.Dirfrag;
import ca.venom.ceph.protocol.types.mds.Vinodeno;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Map;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/MCacheExpire.h#L23
 */
@CephType
@CephMessagePayload(MessageType.MSG_MDS_CACHEEXPIRE)
public class MCacheExpire extends MessagePayload {
    public static final long CEPH_NOSNAP = -2L;

    /**
     * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/mds/mdstypes.h#L460
     */
    @EqualsAndHashCode
    @CephType
    public static class DentryKey {
        @Getter
        @Setter
        private String name;

        @Getter
        @Setter
        private long snapId;

        @CephFieldEncode
        public void encodeData(ByteBuf byteBuf, boolean le, BitSet features) {
            if (snapId != CEPH_NOSNAP) {
                BigInteger unsignedVal = BigInteger.valueOf(snapId);
                if (unsignedVal.signum() < 0) {
                    unsignedVal = unsignedVal.add(BigInteger.ONE.shiftLeft(64));
                }
                CephEncoder.encodeString(name + "_" + unsignedVal, byteBuf, le);
            } else {
                CephEncoder.encodeString(name + "_head", byteBuf, le);
            }
        }

        @CephFieldDecode
        public void decodeData(ByteBuf byteBuf, boolean le, BitSet features) {
            final String key = CephDecoder.decodeString(byteBuf, le);
            final int i = key.indexOf('_');
            assert i >= 0;

            name = key.substring(0, i);
            final String val = key.substring(i + 1);
            if ("head".equals(val)) {
                snapId = CEPH_NOSNAP;
            } else {
                BigInteger valBigInt = new BigInteger(val);
                if (valBigInt.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                    valBigInt = valBigInt.subtract(BigInteger.ONE.shiftLeft(64));
                }
                snapId = valBigInt.longValue();
            }
        }
    }

    /**
     * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/MCacheExpire.h#L32
     */
    @CephType
    public static class Realm {
        @Getter
        @Setter
        @CephField
        private Map<Vinodeno, Integer> inodes;

        @Getter
        @Setter
        @CephField(order = 2)
        private Map<Dirfrag, Integer> dirs;

        @Getter
        @Setter
        @CephField(order = 3)
        private Map<Dirfrag, Map<DentryKey, Integer>> dentries;
    }

    @Getter
    @Setter
    @CephField
    private short from;

    @Getter
    @Setter
    @CephField(order = 2)
    private Map<Dirfrag, Realm> realms;
}
