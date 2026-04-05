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
import ca.venom.ceph.encoding.annotations.CephFieldDecode;
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephPostDecode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeCompatVersionDecider;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersionGenerator;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.CephRelease;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.types.AddrVec;
import ca.venom.ceph.protocol.types.CephAddr;
import ca.venom.ceph.protocol.types.CephEntityInst;
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.protocol.types.EntityName;
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.types.EnumWithIntValue;
import ca.venom.ceph.types.VersionWithCompat;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/3b600d625b30c5b8f7864c13307e67bba2ed815e/src/mon/MonMap.h#L98
 */
@CephType
@CephTypeSize
public class MonMap {
    public enum ElectionStrategy implements EnumWithIntValue {
        CLASSIC(1),
        DISALLOW(2),
        CONNECTIVITY(3);

        private int value;

        ElectionStrategy(int value) {
            this.value = value;
        }

        public static ElectionStrategy getFromValueInt(int value) {
            for (ElectionStrategy electionStrategy : values()) {
                if (electionStrategy.value == value) {
                    return electionStrategy;
                }
            }

            return null;
        }


        @Override
        public int getValueInt() {
            return value;
        }
    }

    @Getter
    @Setter
    @CephField(minVersion = 1, maxVersion = 1)
    @CephField(minVersion = 2, maxVersion = 2)
    @CephField(minVersion = 3, maxVersion = 3)
    @CephField(minVersion = 4, maxVersion = 4)
    @CephField(minVersion = 5, maxVersion = 5)
    @CephField(minVersion = 6, maxVersion = 6)
    @CephField(minVersion = 7, maxVersion = 7)
    @CephField(minVersion = 8, maxVersion = 8)
    @CephField(minVersion = 9, maxVersion = 9)
    private CephUUID fsid;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 1, maxVersion = 1)
    @CephField(order = 2, minVersion = 2, maxVersion = 2)
    @CephField(order = 2, minVersion = 3, maxVersion = 3)
    @CephField(order = 2, minVersion = 4, maxVersion = 4)
    @CephField(order = 2, minVersion = 5, maxVersion = 5)
    @CephField(order = 2, minVersion = 6, maxVersion = 6)
    @CephField(order = 2, minVersion = 7, maxVersion = 7)
    @CephField(order = 2, minVersion = 8, maxVersion = 8)
    @CephField(order = 2, minVersion = 9, maxVersion = 9)
    private int epoch;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 1, maxVersion = 1)
    @CephField(order = 4, minVersion = 2, maxVersion = 2)
    @CephField(order = 4, minVersion = 3, maxVersion = 3)
    @CephField(order = 4, minVersion = 4, maxVersion = 4)
    @CephField(order = 4, minVersion = 5, maxVersion = 5)
    @CephField(order = 3, minVersion = 6, maxVersion = 6)
    @CephField(order = 3, minVersion = 7, maxVersion = 7)
    @CephField(order = 3, minVersion = 8, maxVersion = 8)
    @CephField(order = 3, minVersion = 9, maxVersion = 9)
    private UTime lastChanged;

    @Getter
    @Setter
    @CephField(order = 5, minVersion = 1, maxVersion = 1)
    @CephField(order = 5, minVersion = 2, maxVersion = 2)
    @CephField(order = 5, minVersion = 3, maxVersion = 3)
    @CephField(order = 5, minVersion = 4, maxVersion = 4)
    @CephField(order = 5, minVersion = 5, maxVersion = 5)
    @CephField(order = 4, minVersion = 6, maxVersion = 6)
    @CephField(order = 4, minVersion = 7, maxVersion = 7)
    @CephField(order = 4, minVersion = 8, maxVersion = 8)
    @CephField(order = 4, minVersion = 9, maxVersion = 9)
    private UTime created;

    @Getter
    @Setter
    @CephField(order = 6, minVersion = 4, maxVersion = 4)
    @CephField(order = 6, minVersion = 5, maxVersion = 5)
    @CephField(order = 5, minVersion = 6, maxVersion = 6)
    @CephField(order = 5, minVersion = 7, maxVersion = 7)
    @CephField(order = 5, minVersion = 8, maxVersion = 8)
    @CephField(order = 5, minVersion = 9, maxVersion = 9)
    private MonFeature persistentFeatures;

    @Getter
    @Setter
    @CephField(order = 7, minVersion = 4, maxVersion = 4)
    @CephField(order = 7, minVersion = 5, maxVersion = 5)
    @CephField(order = 6, minVersion = 6, maxVersion = 6)
    @CephField(order = 6, minVersion = 7, maxVersion = 7)
    @CephField(order = 6, minVersion = 8, maxVersion = 8)
    @CephField(order = 6, minVersion = 9, maxVersion = 9)
    private MonFeature optionalFeatures;

    @Getter
    @Setter
    @CephField(order = 8, minVersion = 5, maxVersion = 5)
    @CephField(order = 7, minVersion = 6, maxVersion = 6)
    @CephField(order = 7, minVersion = 7, maxVersion = 7)
    @CephField(order = 7, minVersion = 8, maxVersion = 8)
    @CephField(order = 7, minVersion = 9, maxVersion = 9)
    private Map<String, MonInfo> monInfo;

    @Getter
    @Setter
    @CephField(order = 8, minVersion = 6, maxVersion = 6)
    @CephField(order = 8, minVersion = 7, maxVersion = 7)
    @CephField(order = 8, minVersion = 8, maxVersion = 8)
    @CephField(order = 8, minVersion = 9, maxVersion = 9)
    private List<String> ranks;

    @Getter
    @Setter
    @CephField(order = 9, minVersion = 7, maxVersion = 7)
    @CephField(order = 9, minVersion = 8, maxVersion = 8)
    @CephField(order = 9, minVersion = 9, maxVersion = 9)
    @CephEncodingSize
    private CephRelease minMonRelease;

    @Getter
    @Setter
    @CephField(order = 10, minVersion = 8, maxVersion = 8)
    @CephField(order = 10, minVersion = 9, maxVersion = 9)
    private Set<Integer> removedRanks;

    @Getter
    @Setter
    @CephField(order = 11, minVersion = 8, maxVersion = 8)
    @CephField(order = 11, minVersion = 9, maxVersion = 9)
    private ElectionStrategy strategy;

    @Getter
    @Setter
    @CephField(order = 12, minVersion = 8, maxVersion = 8)
    @CephField(order = 12, minVersion = 9, maxVersion = 9)
    private String disallowedLeaders;

    @Getter
    @Setter
    @CephField(order = 13, minVersion = 9, maxVersion = 9)
    private boolean stretchModeEnabled;

    @Getter
    @Setter
    @CephField(order = 14, minVersion = 9, maxVersion = 9)
    private String tieBreakerMon;

    @Getter
    @Setter
    @CephField(order = 15, minVersion = 9, maxVersion = 9)
    private Set<String> stretchMarkedDownMons;

    @Getter
    private Map<CephAddr, String> addrMons;

    @CephTypeVersionGenerator
    public VersionWithCompat getVersion(BitSet features) {
        if (!CephFeatures.MONNAMES.isEnabled(features)) {
            return new VersionWithCompat((byte) 1, null);
        }

        if (!CephFeatures.MONENC.isEnabled(features)) {
            return new VersionWithCompat((byte) 2, null);
        }

        if (!CephFeatures.SERVER_NAUTILUS.isEnabled(features)) {
            return new VersionWithCompat((byte) 5, (byte) 3);
        }

        return new VersionWithCompat((byte) 9, (byte) 6);
    }

    @CephTypeCompatVersionDecider
    public boolean haveCompatVersion(byte version) {
        return version >= 3;
    }

    @CephFieldEncode(order = 3, minVersion = 1, maxVersion = 1)
    public void encodeMonInfoVersion1(ByteBuf byteBuf, boolean le, BitSet features) {
        CephEncoder.encode(ranks.size(), byteBuf, le, features);
        for (int i = 0; i < ranks.size(); i++) {
            CephEntityInst entityInst = new CephEntityInst();
            entityInst.setName(EntityName.MON(i));
            entityInst.setAddr(monInfo.get(ranks.get(i)).getPublicAddrs().legacyAddr());
            CephEncoder.encode(entityInst, byteBuf, le, features);
        }
    }

    @CephFieldEncode(order = 3, minVersion = 2, maxVersion = 2)
    @CephFieldEncode(order = 3, minVersion = 3, maxVersion = 3)
    @CephFieldEncode(order = 3, minVersion = 4, maxVersion = 4)
    @CephFieldEncode(order = 3, minVersion = 5, maxVersion = 5)
    public void encodeMonInfoVersion2To5(ByteBuf byteBuf, boolean le, BitSet features) {
        final Map<String, CephAddr> legacyMonAddr = new HashMap<>();
        for (Map.Entry<String, MonInfo> info : monInfo.entrySet()) {
            legacyMonAddr.put(info.getKey(), info.getValue().getPublicAddrs().legacyAddr());
        }

        CephEncoder.encodeMap(legacyMonAddr, byteBuf, le, features, String.class, CephAddr.class);
    }

    @CephFieldDecode(order = 3, minVersion = 1, maxVersion = 1)
    public void decodeMonInfoVersion1(ByteBuf byteBuf, boolean le, BitSet features) {
        monInfo = new HashMap<>();
        try {
            final int size = CephDecoder.decodeInt(byteBuf, le);
            for (int i = 0; i < size; i++) {
                final CephEntityInst entityInst = CephDecoder.decode(byteBuf, le, features, CephEntityInst.class);
                final MonInfo monInfoEntry = new MonInfo();
                monInfoEntry.setName(i + "0");
                final AddrVec addrVec = new AddrVec();
                addrVec.setAddrList(Collections.singletonList(entityInst.getAddr()));

                monInfo.put(monInfoEntry.getName(), monInfoEntry);
            }
        } catch (DecodingException de) {
            throw new RuntimeException(de);
        }
    }

    @CephFieldDecode(order = 3, minVersion = 2, maxVersion = 2)
    @CephFieldDecode(order = 3, minVersion = 3, maxVersion = 3)
    @CephFieldDecode(order = 3, minVersion = 4, maxVersion = 4)
    @CephFieldDecode(order = 3, minVersion = 5, maxVersion = 5)
    public void decodeMonInfoVersion2To5(ByteBuf byteBuf, boolean le, BitSet features) {
        monInfo = new HashMap<>();
        try {
            final Map<String, CephAddr> legacyMonAddr = CephDecoder.decodeMap(
                    byteBuf,
                    le,
                    features,
                    String.class,
                    CephAddr.class
            );

            for (Map.Entry<String, CephAddr> entry : legacyMonAddr.entrySet()) {
                final MonInfo monInfoEntry = new MonInfo();
                monInfoEntry.setName(entry.getKey());
                final AddrVec addrVec = new AddrVec();
                addrVec.setAddrList(Collections.singletonList(entry.getValue()));
                monInfoEntry.setPublicAddrs(addrVec);
                monInfo.put(entry.getKey(), monInfoEntry);
            }
        } catch (DecodingException de) {
            throw new RuntimeException(de);
        }
    }

    @CephFieldEncode(order = 6, minVersion = 1, maxVersion = 1)
    @CephFieldEncode(order = 6, minVersion = 2, maxVersion = 2)
    @CephFieldEncode(order = 6, minVersion = 3, maxVersion = 3)
    @CephFieldEncode(order = 8, minVersion = 4, maxVersion = 4)
    @CephFieldEncode(order = 9, minVersion = 5, maxVersion = 5)
    public void encodeRanksVersion1To5(ByteBuf byteBuf, boolean le, BitSet features) {
    }

    @CephFieldDecode(order = 6, minVersion = 1, maxVersion = 1)
    @CephFieldDecode(order = 6, minVersion = 2, maxVersion = 2)
    @CephFieldDecode(order = 6, minVersion = 3, maxVersion = 3)
    @CephFieldDecode(order = 8, minVersion = 4, maxVersion = 4)
    @CephFieldDecode(order = 9, minVersion = 5, maxVersion = 5)
    public void decodeRanksVersion1To5(ByteBuf byteBuf, boolean le, BitSet features) {
        ranks = new ArrayList<>(monInfo.size());

        final List<MonInfo> sortedMonInfos = new ArrayList<>(monInfo.values());
        sortedMonInfos.sort((o1, o2) -> {
            final int compared = o1
                    .getPublicAddrs()
                    .legacyOrFrontAddr()
                    .compareTo(o2.getPublicAddrs().legacyOrFrontAddr());
            if (compared == 0) {
                return o1.getName().compareTo(o2.getName());
            } else {
                return compared;
            }
        });

        for (MonInfo monInfoEntry : sortedMonInfos) {
            ranks.add(monInfoEntry.getName());
        }
    }

    @CephPostDecode
    public void postDecode(byte version) {
        if (version < 7) {
            minMonRelease = MonFeatureValues.inferFromMonFeatures(persistentFeatures.getFeatures());
        }

        if (version < 9) {
            stretchModeEnabled = false;
            tieBreakerMon = "";
            stretchMarkedDownMons = new HashSet<>();
        }

        addrMons = new HashMap<>();
        for (Map.Entry<String, MonInfo> monInfoEntry : monInfo.entrySet()) {
            for (CephAddr addr : monInfoEntry.getValue().getPublicAddrs().getAddrList()) {
                addrMons.put(addr, monInfoEntry.getKey());
            }
        }
    }
}
