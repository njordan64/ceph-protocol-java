/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol;

import ca.venom.ceph.types.EnumWithIntValue;

public enum NodeType implements EnumWithIntValue {
    MON(0x01),
    MDS(0x02),
    OSD(0x04),
    CLIENT(0x08),
    MGR(0x10),
    AUTH(0x20),
    ANY(0xFF);

    private final int valueInt;

    private NodeType(int valueInt) {
        this.valueInt = valueInt;
    }

    public int getValueInt() {
        return valueInt;
    }

    public static NodeType getFromValueInt(int valueInt) {
        for (NodeType nodeType : values()) {
            if (nodeType.valueInt == valueInt) {
                return nodeType;
            }
        }

        return null;
    }
}
