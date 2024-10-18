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
import ca.venom.ceph.protocol.types.mon.MonSubscribeItem;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MMonSubscribe.h#L32
 */
@CephType
@CephMessagePayload(MessageType.MSG_MON_GET_MAP)
public class MonGetMap implements MessagePayload {
    @Getter
    @Setter
    @CephField
    private Map<String, MonSubscribeItem> what = new HashMap<>();

    @Getter
    @Setter
    @CephField(order = 2)
    private String hostname;
}
