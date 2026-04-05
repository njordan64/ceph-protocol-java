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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@CephType
public class EntityName {
    private static final int TYPE_MON = 0x01;
    private static final int TYPE_MDS = 0x02;
    private static final int TYPE_OSD = 0x04;
    private static final int TYPE_CLIENT = 0x08;
    private static final int TYPE_MGR = 0x10;
    private static final int TYPE_AUTH = 0x20;

    private static final int NEW = -1;

    @Getter
    @Setter
    @CephField
    private byte type;

    @Getter
    @Setter
    @CephField(order = 2)
    private long num;

    public static EntityName MON() {
        return new EntityName((byte) TYPE_MON, NEW);
    }

    public static EntityName MON(long i) {
        return new EntityName((byte) TYPE_MON, i);
    }

    public static EntityName MDS() {
        return new EntityName((byte) TYPE_MDS, NEW);
    }

    public static EntityName MDS(long i) {
        return new EntityName((byte) TYPE_MDS, i);
    }

    public static EntityName OSD() {
        return new EntityName((byte) TYPE_OSD, NEW);
    }

    public static EntityName OSD(long i) {
        return new EntityName((byte) TYPE_OSD, i);
    }

    public static EntityName CLIENT() {
        return new EntityName((byte) TYPE_CLIENT, NEW);
    }

    public static EntityName CLIENT(long i) {
        return new EntityName((byte) TYPE_CLIENT, i);
    }

    public static EntityName MGR() {
        return new EntityName((byte) TYPE_MGR, NEW);
    }

    public static EntityName MGR(long i) {
        return new EntityName((byte) TYPE_MGR, i);
    }

    public static EntityName AUTH() {
        return new EntityName((byte) TYPE_AUTH, NEW);
    }

    public static EntityName AUTH(long i) {
        return new EntityName((byte) TYPE_AUTH, i);
    }

    public boolean isMon() {
        return TYPE_MON == type;
    }

    public boolean isMds() {
        return TYPE_MDS == type;
    }

    public boolean isOsd() {
        return TYPE_OSD == type;
    }

    public boolean isClient() {
        return TYPE_CLIENT == type;
    }

    public boolean isMgr() {
        return TYPE_MGR == type;
    }

    public boolean isAuth() {
        return TYPE_AUTH == type;
    }
}
