/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@CephType
@CephTypeVersionConstant(version = 2)
public class AddrVec {
    @Getter
    @Setter
    @CephField
    private List<CephAddr> addrList;

    public CephAddr legacyAddr() {
        if (addrList != null) {
            for (CephAddr addr : addrList) {
                if (addr.getType() == CephAddr.AddrType.LEGACY) {
                    return addr;
                }
            }
        }

        final CephAddr blankAddr = new CephAddr();
        blankAddr.setSocketAddress(null);

        return blankAddr;
    }

    public CephAddr legacyOrFrontAddr() {
        if (addrList == null || addrList.isEmpty()) {
            return null;
        }

        for (CephAddr addr : addrList) {
            if (addr.getType() == CephAddr.AddrType.LEGACY) {
                return addr;
            }
        }

        return addrList.get(0);
    }
}
