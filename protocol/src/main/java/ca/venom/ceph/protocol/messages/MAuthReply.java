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
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MAuthReply.h#L21
 */
@CephType
@CephMessagePayload(MessageType.MSG_AUTH_REPLY)
public class MAuthReply extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private int protocol;

    @Getter
    @Setter
    @CephField(order = 2)
    private int result;

    @Getter
    @Setter
    @CephField(order = 3)
    private long globalId;

    @Getter
    @Setter
    @CephField(order = 4, includeSize = true)
    private byte[] resultBl;

    @Getter
    @Setter
    @CephField(order = 5)
    private String resultMsg;
}
