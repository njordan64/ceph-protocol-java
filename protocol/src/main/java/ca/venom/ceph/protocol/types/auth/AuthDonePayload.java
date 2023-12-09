package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@CephType
@CephTypeSize
public class AuthDonePayload {
    @Getter
    @Setter
    @CephField
    private CephXResponseHeader responseHeader;

    @Getter
    @CephField(order = 2)
    private byte version = 1;

    @Getter
    @Setter
    @CephField(order = 3)
    private List<CephXTicketInfo> ticketInfos;

    @Getter
    @Setter
    @CephField(order = 4, includeSize = true)
    private byte[] encryptedSecret;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true)
    private byte[] extra;

    public void setVersion(byte version) {
    }
}
