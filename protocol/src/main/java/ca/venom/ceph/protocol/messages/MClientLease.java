/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.types.mds.Lease;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MClientLease.h#L23
 */
@CephType
@CephMessagePayload(MessageType.MSG_CLIENT_LEASE)
public class MClientLease extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private Lease h;

    @Getter
    @Setter
    @CephField(order = 2)
    private String dName;
}
