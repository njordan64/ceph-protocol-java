package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

@CephType
@CephTypeVersion(version = 1, compatVersion = 1)
@CephTypeSize
public class MonFeature {
    @Getter
    @Setter
    @CephField
    @CephEncodingSize(8)
    private BitSet features;
}
