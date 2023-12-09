package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeVersion(version = 1)
public class CephXTicketBlob {
    @Getter
    @Setter
    @CephField
    private long secretId;

    @Getter
    @Setter
    @CephField(order = 2, includeSize = true)
    private byte[] blob;
}
