package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.AuthMode;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeSize
public class AuthRequestPayload {
    @Getter
    @Setter
    @CephField
    @CephEncodingSize
    private AuthMode authMode;

    @Getter
    @Setter
    @CephField(order = 2)
    private EntityName entityName;

    @Getter
    @Setter
    @CephField(order = 3)
    private long globalId;
}
