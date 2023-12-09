package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeSize
public class AuthRequestMorePayload {
    @Getter
    @Setter
    @CephField
    private CephXRequestHeader requestHeader;

    @Getter
    @Setter
    @CephField(order = 2)
    private CephXAuthenticate authenticate;
}
