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

import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

@CephType
public class CryptoKey {
    @Getter
    @Setter
    @CephField
    private short type;

    @Getter
    @Setter
    @CephField(order = 2)
    private UTime created;

    @Getter
    @Setter
    @CephField(order = 3, includeSize = true, sizeLength = 2)
    private byte[] secret;
}
