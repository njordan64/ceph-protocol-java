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
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import ca.venom.ceph.protocol.types.UTime;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeVersion(version = 2)
public class AuthTicket {
    @Getter
    @Setter
    @CephField
    private EntityName name;

    @Getter
    @Setter
    @CephField(order = 2)
    private long globalId;

    @Getter
    @Setter
    @CephField(order = 3)
    private long authUid = -1;

    @Getter
    @Setter
    @CephField(order = 4)
    private UTime created;

    @Getter
    @Setter
    @CephField(order = 5)
    private UTime expires;

    @Getter
    @Setter
    @CephField(order = 6)
    private AuthCapsInfo caps;

    @Getter
    @Setter
    @CephField(order = 7)
    private int flags;
}
