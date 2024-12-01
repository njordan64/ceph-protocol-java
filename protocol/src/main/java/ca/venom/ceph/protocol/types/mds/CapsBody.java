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

import ca.venom.ceph.encoding.annotations.CephChildType;
import ca.venom.ceph.encoding.annotations.CephMarker;
import ca.venom.ceph.encoding.annotations.CephParentType;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersion;

@CephType
@CephMarker(1)
@CephTypeVersion(version = 1, compatVersion = 1)
@CephTypeSize
@CephParentType(useParameter = true)
@CephChildType(typeValue = 3, typeClass = CapsExportBody.class)
@CephChildType(isDefault = true, typeValue = -1, typeClass = CapsNonExportBody.class)
public class CapsBody {
}
