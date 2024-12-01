/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.mds;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.NodeType;
import ca.venom.ceph.types.EnumWithIntValue;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/ceph_fs.h#L873
 */
@CephType
public class CapsHead {
    public enum OpValue implements EnumWithIntValue {
        GRANT(0),
        REVOKE(1),
        TRUNC(2),
        EXPORT(3),
        IMPORT(4),
        UPDATE(5),
        DROP(6),
        FLUSH(7),
        FLUSH_ACK(8),
        FLUSHSNAP(9),
        FLUSHSNAP_ACK(10),
        RELEASE(11),
        RENEW(12);

        private final int valueInt;

        OpValue(int valueInt) {
            this.valueInt = valueInt;
        }

        @Override
        public int getValueInt() {
            return valueInt;
        }

        public static OpValue getFromValueInt(int valueInt) {
            for (OpValue opValue : values()) {
                if (opValue.valueInt == valueInt) {
                    return opValue;
                }
            }

            return null;
        }
    }

    @Getter
    @Setter
    @CephField
    private OpValue op;

    @Getter
    @Setter
    @CephField(order = 2)
    private long ino;

    @Getter
    @Setter
    @CephField(order = 3)
    private long realm;

    @Getter
    @Setter
    @CephField(order = 4)
    private long capId;

    @Getter
    @Setter
    @CephField(order = 5)
    private int seq;

    @Getter
    @Setter
    @CephField(order = 6)
    private int issueSeq;

    @Getter
    @Setter
    @CephField(order = 7)
    private int caps;

    @Getter
    @Setter
    @CephField(order = 8)
    private int wanted;

    @Getter
    @Setter
    @CephField(order = 9)
    private int dirty;

    @Getter
    @Setter
    @CephField(order = 10)
    private int migrateSeq;

    @Getter
    @Setter
    @CephField(order = 11)
    private long snapFollows;

    @Getter
    @Setter
    @CephField(order = 12)
    private int snapTraceLen;

    @Getter
    @Setter
    @CephField(order = 13)
    private int uid;

    @Getter
    @Setter
    @CephField(order = 14)
    private int gid;

    @Getter
    @Setter
    @CephField(order = 15)
    private int mode;

    @Getter
    @Setter
    @CephField(order = 16)
    private int nlink;

    @Getter
    @Setter
    @CephField(order = 17)
    private int xattrLen;

    @Getter
    @Setter
    @CephField(order = 18)
    private long xattrVersion;
}
