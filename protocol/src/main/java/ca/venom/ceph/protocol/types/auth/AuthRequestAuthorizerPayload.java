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

import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.AuthMode;
import lombok.Getter;
import lombok.Setter;

@CephType
public class AuthRequestAuthorizerPayload extends AuthRequestPayload {
    @Getter
    @Setter
    @CephField
    @CephEncodingSize
    private AuthMode authMode = AuthMode.AUTHORIZER;

    @Getter
    @Setter
    @CephField(order = 2)
    private long globalId;

    @Getter
    @Setter
    @CephField(order = 3)
    private int serviceId;

    @Getter
    @Setter
    @CephField(order = 4)
    private CephXTicketBlob ticket;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true)
    private byte[] encryptedAuthorize;
}
