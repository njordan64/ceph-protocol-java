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

import ca.venom.ceph.encoding.annotations.ByteOrderPreference;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/include/msgr.h#L205
 */

@CephType
public class CephMsgHeader2 {
    @Getter
    @Setter
    @CephField(byteOrderPreference = ByteOrderPreference.LE)
    private long seq;

    @Getter
    @Setter
    @CephField(order = 2, byteOrderPreference = ByteOrderPreference.LE)
    private long tid;

    @Getter
    @Setter
    @CephField(order = 3, byteOrderPreference = ByteOrderPreference.LE)
    private short type;

    @Getter
    @Setter
    @CephField(order = 4, byteOrderPreference = ByteOrderPreference.LE)
    private short priority;

    @Getter
    @Setter
    @CephField(order = 5, byteOrderPreference = ByteOrderPreference.LE)
    private short version;

    @Getter
    @Setter
    @CephField(order = 6, byteOrderPreference = ByteOrderPreference.LE)
    private int dataPrePaddingLen;

    @Getter
    @Setter
    @CephField(order = 7, byteOrderPreference = ByteOrderPreference.LE)
    private short dataOff;

    @Getter
    @Setter
    @CephField(order = 8, byteOrderPreference = ByteOrderPreference.LE)
    private long ackSeq;

    @Getter
    @Setter
    @CephField(order = 9)
    private byte flags;

    @Getter
    @Setter
    @CephField(order = 10, byteOrderPreference = ByteOrderPreference.LE)
    private short compatVersion;

    @Getter
    @Setter
    @CephField(order = 11, byteOrderPreference = ByteOrderPreference.LE)
    private short reserved;
}
