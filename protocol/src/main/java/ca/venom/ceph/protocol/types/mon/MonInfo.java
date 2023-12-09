package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import ca.venom.ceph.protocol.types.AddrVec;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@CephType
@CephTypeVersion(version = 5, compatVersion = 1)
@CephTypeSize
public class MonInfo {
    @Getter
    @Setter
    @CephField
    private String name;

    @Getter
    @Setter
    @CephField(order = 2)
    private AddrVec addrs;

    @Getter
    @Setter
    @CephField(order = 3)
    private short priority;

    @Getter
    @Setter
    @CephField(order = 4)
    private short weight;

    @Getter
    @Setter
    @CephField(order = 5)
    private Map<String, String> crushLoc;
}
