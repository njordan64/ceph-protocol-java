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
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/3b600d625b30c5b8f7864c13307e67bba2ed815e/src/messages/MAuth.h#L26
 */
@CephType
@CephMessagePayload(MessageType.MSG_AUTH)
public class MAuth extends PaxosMessage {
    @Getter
    @Setter
    @CephField(order = 4)
    private int protocol;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true)
    private byte[] authPayload;

    @Getter
    @Setter
    @CephField(order = 6, optional = true)
    private int monMapEpoch;
}
