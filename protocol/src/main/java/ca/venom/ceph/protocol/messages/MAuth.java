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
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MAuth.h#L26
 */
@CephType
@CephMessagePayload(MessageType.MSG_AUTH)
public class MAuth extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private long version;

    @Getter
    @Setter
    @CephField(order = 2)
    private short deprecatedSessionMon;

    @Getter
    @Setter
    @CephField(order = 3)
    private long deprecatedSessionMonTid;

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
    @CephField(order = 6)
    private int monMapEpoch;
}
