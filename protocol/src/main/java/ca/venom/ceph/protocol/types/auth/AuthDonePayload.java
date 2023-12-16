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

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@CephType
@CephTypeSize
public class AuthDonePayload {
    @Getter
    @Setter
    @CephField
    private CephXResponseHeader responseHeader;

    @Getter
    @CephField(order = 2)
    private byte version = 1;

    @Getter
    @Setter
    @CephField(order = 3)
    private List<CephXTicketInfo> ticketInfos;

    @Getter
    @Setter
    @CephField(order = 4, includeSize = true)
    private byte[] encryptedSecret;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true)
    private byte[] extra;

    public void setVersion(byte version) {
    }
}
