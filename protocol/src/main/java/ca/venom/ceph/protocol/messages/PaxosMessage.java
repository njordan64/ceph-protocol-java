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
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/PaxosServiceMessage.h#L14
 */
public class PaxosMessage extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private long version;

    @Getter
    @Setter
    @CephField(order = 2)
    private short deprecatedSessionMon = -1;

    @Getter
    @Setter
    @CephField(order = 3)
    private long deprecatedSessionMonTid;
}
