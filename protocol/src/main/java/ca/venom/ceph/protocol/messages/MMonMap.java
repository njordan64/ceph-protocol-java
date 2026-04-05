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
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import ca.venom.ceph.protocol.CephRelease;
import ca.venom.ceph.protocol.types.mon.MonFeature;
import ca.venom.ceph.protocol.types.mon.MonInfo;
import ca.venom.ceph.protocol.types.mon.MonMap;
import ca.venom.ceph.types.EnumWithIntValue;
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/3b600d625b30c5b8f7864c13307e67bba2ed815e/src/messages/MMonMap.h#L25
 */
@CephType
@CephTypeSize
@CephMessagePayload(MessageType.MSG_MON_MAP)
public class MMonMap extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private MonMap monMap;
}
