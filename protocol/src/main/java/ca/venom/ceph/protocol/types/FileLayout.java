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
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/include/fs_types.h#L131
 */
@CephType
@CephTypeVersion(version = 2, compatVersion = 2)
public class FileLayout {
    @Getter
    @Setter
    @CephField
    private int stripeUnit;

    @Getter
    @Setter
    @CephField(order = 2)
    private int stripeCount;

    @Getter
    @Setter
    @CephField(order = 3)
    private int objectSize;

    @Getter
    @Setter
    @CephField(order = 4)
    private long poolId;

    @Getter
    @Setter
    @CephField(order = 5)
    private String poolNs;

    public void fromLegacy(CephFileLayout layout) {
        stripeUnit = layout.getFlStripeUnit();
        stripeCount = layout.getFlStripeCount();
        objectSize = layout.getFlObjectSize();
        poolId = layout.getFlPgPool();
        if (poolId == 0 && stripeUnit == 0 && stripeCount == 0 && objectSize == 0) {
            poolId = -1;
        }
        poolNs = null;
    }

    public void toLegacy(CephFileLayout layout) {
        layout.setFlStripeUnit(stripeUnit);
        layout.setFlStripeCount(stripeCount);
        layout.setFlObjectSize(objectSize);
        layout.setFlCasHash(0);
        layout.setFlObjectStripeUnit(0);
        if (poolId >= 0) {
            layout.setFlPgPool((int) poolId);
        } else {
            layout.setFlPgPool(0);
        }
    }
}
