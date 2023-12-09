package ca.venom.ceph.protocol.types;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@CephType
@CephTypeVersion(version = 2)
public class AddrVec {
    @Getter
    @Setter
    @CephField
    private List<Addr> addrList;
}
