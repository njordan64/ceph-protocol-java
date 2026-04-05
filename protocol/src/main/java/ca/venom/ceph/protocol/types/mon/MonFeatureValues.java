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

import ca.venom.ceph.protocol.CephRelease;

import java.util.BitSet;

public enum MonFeatureValues {
    KRAKEN(1L << 0),
    LUMINOUS(1L << 1),
    MIMIC(1L << 2),
    OSDMAP_PRUNE(1L << 3),
    NAUTILUS(1L << 4),
    OCTOPUS(1L << 5),
    PACIFIC(1L << 6),
    PINGING(1L << 7),
    QUINCY(1L << 8),
    REEF(1L << 9),
    SQUID(1L << 10),
    TENTACLE(1L << 11),
    RESERVED(1L << 63),
    NONE(0L);

    private final BitSet value;

    MonFeatureValues(long value) {
        this.value = BitSet.valueOf(new long[] {value});
    }

    public static CephRelease inferFromMonFeatures(BitSet features) {
        MonFeatureValues matchedItem = null;

        for (MonFeatureValues item : values()) {
            final BitSet temp = item.value.get(0, 64);
            temp.and(features);
            if (temp.equals(item.value)) {
                matchedItem = item;
            }
        }

        if (matchedItem == null) {
            return null;
        }

        return switch (matchedItem) {
            case TENTACLE -> CephRelease.TENTACLE;
            case SQUID -> CephRelease.SQUID;
            case REEF -> CephRelease.REEF;
            case QUINCY -> CephRelease.QINCY;
            case PACIFIC -> CephRelease.PACIFIC;
            case OCTOPUS -> CephRelease.OCTOPUS;
            case NAUTILUS -> CephRelease.NAUTILUS;
            case MIMIC -> CephRelease.MIMIC;
            case LUMINOUS -> CephRelease.LUMINOUS;
            case KRAKEN -> CephRelease.KRAKEN;
            default -> CephRelease.UNKNOWN;
        };
    }
}
