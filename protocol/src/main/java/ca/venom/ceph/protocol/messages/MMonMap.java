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
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import ca.venom.ceph.protocol.CephRelease;
import ca.venom.ceph.protocol.types.mon.MonFeature;
import ca.venom.ceph.protocol.types.mon.MonInfo;
import ca.venom.ceph.types.EnumWithIntValue;
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/mon/MonMap.h#L96
 */
@CephType
@CephTypeVersion(version = 9, compatVersion = 6)
@CephTypeSize
@CephMessagePayload(MessageType.MSG_MON_MAP)
public class MMonMap extends MessagePayload {
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
    @CephField
    private int epoch;

    @Getter
    @Setter
    @CephField(order = 2)
    private CephUUID fsid;

    @Getter
    @Setter
    @CephField(order = 3)
    private UTime lastChanged;

    @Getter
    @Setter
    @CephField(order = 4)
    private UTime created;

    @Getter
    @Setter
    @CephField(order = 5)
    private MonFeature persistentFeatures;

    @Getter
    @Setter
    @CephField(order = 6)
    private MonFeature optionalFeatures;

    @Getter
    @Setter
    @CephField(order = 7)
    private Map<String, MonInfo> monInfo;

    @Getter
    @Setter
    @CephField(order = 8)
    private List<String> ranks;

    @Getter
    @Setter
    @CephField(order = 9)
    private CephRelease minMonRelease;

    @Getter
    @Setter
    @CephField(order = 10)
    private Set<Integer> removedRanks;

    @Getter
    @Setter
    @CephField(order = 11)
    private ElectionStrategy strategy;

    @Getter
    @Setter
    @CephField(order = 12)
    private String disallowedLeaders;

    @Getter
    @Setter
    @CephField(order = 13)
    private boolean stretchModeEnabled;

    @Getter
    @Setter
    @CephField(order = 14)
    private String tieBreakerMon;

    @Getter
    @Setter
    @CephField(order = 15)
    private Set<String> stretchMarkedDownMons;
}
