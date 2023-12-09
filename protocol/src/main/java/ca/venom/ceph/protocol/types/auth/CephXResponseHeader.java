package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

@CephType
public class CephXResponseHeader {
    @Getter
    @Setter
    @CephField
    private short responseType;

    @Getter
    @Setter
    @CephField(order = 2)
    private int status;
}
