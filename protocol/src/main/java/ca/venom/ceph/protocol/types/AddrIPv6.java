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

import ca.venom.ceph.protocol.types.annotations.ByteOrderPreference;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

@Getter
@CephType
public class AddrIPv6 extends Addr {
    @CephField
    private final int type = 2;

    @Setter
    @CephField(order = 2)
    private int nonce;

    @CephField(order = 3)
    private final int innerSize = 18;

    @CephField(order = 4)
    private final short innerType = 10;

    @Setter
    @CephField(order = 5, byteOrderPreference = ByteOrderPreference.BE)
    private short port;

    @Setter
    @CephField(order = 6)
    private int flowInfo;

    @Setter
    @CephField(order = 7)
    @CephEncodingSize(6)
    private byte[] addrBytes;

    @Setter
    @CephField(order = 8)
    private int scopeId;

    public void setType(int type) {
    }

    public void setInnerSize(int innerSize) {
    }

    public void setInnerType(short innerType) {
    }
}
