/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.types.UTime;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

@CephType
@CephTypeVersionConstant(version = 2)
public class AuthTicket {
    @Getter
    @Setter
    @CephField(minVersion = 1, maxVersion = 1)
    @CephField(minVersion = 2, maxVersion = 2)
    private EntityName name;

    @Getter
    @Setter
    @CephField(order = 2, minVersion = 1, maxVersion = 1)
    @CephField(order = 2, minVersion = 2, maxVersion = 2)
    private long globalId;

    @CephField(order = 3, minVersion = 2, maxVersion = 2)
    @Setter
    private long oldAuid = -1L;

    @Getter
    @Setter
    @CephField(order = 3, minVersion = 1, maxVersion = 1)
    @CephField(order = 4, minVersion = 2, maxVersion = 2)
    private UTime created;

    @Getter
    @Setter
    @CephField(order = 4, minVersion = 1, maxVersion = 1)
    @CephField(order = 5, minVersion = 2, maxVersion = 2)
    private UTime expires;

    @Getter
    @Setter
    @CephField(order = 5, minVersion = 1, maxVersion = 1)
    @CephField(order = 6, minVersion = 2, maxVersion = 2)
    private AuthCapsInfo caps;

    @Getter
    @Setter
    @CephField(order = 6, minVersion = 1, maxVersion = 1)
    @CephField(order = 7, minVersion = 2, maxVersion = 2)
    private int flags;

    @CephFieldEncode(order = 3, minVersion = 2, maxVersion = 2)
    public void encodeOldAuid(ByteBuf byteBuf, boolean le, BitSet features) {
        CephEncoder.encode(-1L, byteBuf, le, features);
    }
}
