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
import lombok.Getter;
import lombok.Setter;

@CephType
public class CephXTicketInfo {
    @Getter
    @Setter
    @CephField
    private int serviceId;

    @Getter
    @CephField(order = 2)
    private byte version = (byte) 1;

    @Getter
    @Setter
    @CephField(order = 3, includeSize = true)
    private byte[] serviceTicket;

    @Getter
    @Setter
    @CephField(order = 4)
    private boolean encrypted;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true)
    private byte[] ticket;

    public void setVersion(byte version) {
    }
}
