package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeVersion(version = 3)
public class CephXAuthenticate {
    @Getter
    @Setter
    @CephField
    @CephEncodingSize(8)
    private byte[] clientChallenge;

    @Getter
    @Setter
    @CephField(order = 2)
    @CephEncodingSize(8)
    private byte[] key;

    @Getter
    @Setter
    @CephField(order = 3)
    private CephXTicketBlob oldTicket;

    @Getter
    @Setter
    @CephField(order = 4)
    private int otherKeys;
}
