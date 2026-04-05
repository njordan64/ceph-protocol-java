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
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@CephType
@CephMessagePayload(MessageType.MSG_MON_COMMAND)
public class MMonCommand extends PaxosMessage {
    @Getter
    @Setter
    @CephField(order = 4)
    private CephUUID fsid;

    @Getter
    @Setter
    @CephField(order = 5)
    private List<String> cmd;
}
