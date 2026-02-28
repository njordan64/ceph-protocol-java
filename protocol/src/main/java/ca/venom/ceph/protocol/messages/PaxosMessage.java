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
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/3b600d625b30c5b8f7864c13307e67bba2ed815e/src/messages/MAuth.h#L26
 */
public class PaxosMessage extends MessagePayload {
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
}
