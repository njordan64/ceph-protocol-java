package ca.venom.ceph.protocol.types;

import ca.venom.ceph.protocol.types.annotations.CephChildType;
import ca.venom.ceph.protocol.types.annotations.CephMarker;
import ca.venom.ceph.protocol.types.annotations.CephParentType;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;

@CephType
@CephMarker(1)
@CephTypeVersion(version = 1, compatVersion = 1)
@CephTypeSize
@CephParentType(typeOffset = 19, typeSize = 2)
@CephChildType(typeValue = 2, typeClass = AddrIPv4.class)
@CephChildType(typeValue = 10, typeClass = AddrIPv6.class)
public abstract class Addr {
}
