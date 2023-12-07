package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeVersion(version = 1)
public class CephXServiceTicket {
    @Getter
    @Setter
    @CephField
    private CryptoKey sessionKey;

    @Getter
    @Setter
    @CephField(order = 2)
    private UTime validity;
}
