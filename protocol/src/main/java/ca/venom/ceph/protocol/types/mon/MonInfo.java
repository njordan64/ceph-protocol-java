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
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import ca.venom.ceph.protocol.types.AddrVec;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@CephType
@CephTypeVersion(version = 5, compatVersion = 1)
@CephTypeSize
public class MonInfo {
    @Getter
    @Setter
    @CephField
    private String name;

    @Getter
    @Setter
    @CephField(order = 2)
    private AddrVec addrs;

    @Getter
    @Setter
    @CephField(order = 3)
    private short priority;

    @Getter
    @Setter
    @CephField(order = 4)
    private short weight;

    @Getter
    @Setter
    @CephField(order = 5)
    private Map<String, String> crushLoc;
}
