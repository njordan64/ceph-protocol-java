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

import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeVersion(version = 1)
public class CephXServerChallenge {
    @Getter
    @Setter
    @CephField
    @CephEncodingSize(8)
    private byte[] serverChallenge;
}
