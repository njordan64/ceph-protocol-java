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
import ca.venom.ceph.encoding.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

@CephType
public class CephMsgHeader2 {
    @Getter
    @Setter
    @CephField
    private long seq;

    @Getter
    @Setter
    @CephField(order = 2)
    private long tid;

    @Getter
    @Setter
    @CephField(order = 3)
    private short type;

    @Getter
    @Setter
    @CephField(order = 4)
    private short priority;

    @Getter
    @Setter
    @CephField(order = 5)
    private short version;

    @Getter
    @Setter
    @CephField(order = 6)
    private int dataPrePaddingLen;

    @Getter
    @Setter
    @CephField(order = 7)
    private short dataOff;

    @Getter
    @Setter
    @CephField(order = 8)
    private long ackSeq;

    @Getter
    @Setter
    @CephField(order = 9)
    private byte flags;

    @Getter
    @Setter
    @CephField(order = 10)
    private short compatVersion;

    @Getter
    @Setter
    @CephField(order = 11)
    private short reserved;
}
