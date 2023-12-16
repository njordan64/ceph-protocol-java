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

import ca.venom.ceph.protocol.AuthMode;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeSize
public class AuthRequestPayload {
    @Getter
    @Setter
    @CephField
    @CephEncodingSize
    private AuthMode authMode;

    @Getter
    @Setter
    @CephField(order = 2)
    private EntityName entityName;

    @Getter
    @Setter
    @CephField(order = 3)
    private long globalId;
}
