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
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.types.EnumWithIntValue;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MMonPing.h#L21
 */
@CephType
@CephMessagePayload(MessageType.MSG_MON_PING)
public class MMonPing extends MessagePayload {
    public enum PingOp implements EnumWithIntValue {
        PING(1, "ping"),
        PING_REPLY(2, "ping_reply");

        private final int valueInt;
        private final String name;

        PingOp(int valueInt, String name) {
            this.valueInt = valueInt;
            this.name = name;
        }

        public int getValueInt() {
            return valueInt;
        }

        public String getName() {
            return name;
        }

        public static PingOp getFromValueInt(int valueInt) {
            for (PingOp pingOp : values()) {
                if (pingOp.valueInt == valueInt) {
                    return pingOp;
                }
            }

            return null;
        }
    }

    @Getter
    @Setter
    @CephField
    private PingOp op;

    @Getter
    @Setter
    @CephField(order = 2)
    private UTime stamp;

    @Getter
    @Setter
    @CephField(order = 3, includeSize = true)
    private byte[] tracker;

    @Getter
    @Setter
    @CephField(order = 4, includeSize = true)
    private byte[] zeroes;
}
