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

public enum AuthMode implements EnumWithIntValue {
    NONE(0),
    AUTHORIZER(1),
    AUTHORIZER_MAX(9),
    MON(10),
    MON_MAX(19);

    private final int valueInt;

    private AuthMode(int valueInt) {
        this.valueInt = valueInt;
    }

    public int getValueInt() {
        return valueInt;
    }

    public static AuthMode getFromTypeNum(int valueInt) {
        for (AuthMode authMode : values()) {
            if (authMode.valueInt == valueInt) {
                return authMode;
            }
        }

        return null;
    }
}
