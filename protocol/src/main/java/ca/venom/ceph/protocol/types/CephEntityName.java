package ca.venom.ceph.protocol.types;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

@CephType
public class CephEntityName {
    @Getter
    @Setter
    @CephField(order = 1)
    private byte type;

    @Getter
    @Setter
    @CephField(order = 2)
    private long num;
}
