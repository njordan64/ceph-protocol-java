package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

@CephType
public class MonSubscribeItem {
    @Getter
    @Setter
    @CephField
    private long start;

    @Getter
    @Setter
    @CephField(order = 2)
    @CephEncodingSize
    private BitSet flags;
}
