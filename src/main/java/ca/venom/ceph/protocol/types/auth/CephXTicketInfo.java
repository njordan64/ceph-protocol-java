package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

@CephType
public class CephXTicketInfo {
    @Getter
    @Setter
    @CephField
    private int serviceId;

    @Getter
    @CephField(order = 2)
    private byte version = (byte) 1;

    @Getter
    @Setter
    @CephField(order = 3, includeSize = true)
    private byte[] serviceTicket;

    @Getter
    @Setter
    @CephField(order = 4)
    private boolean encrypted;

    @Getter
    @Setter
    @CephField(order = 5, includeSize = true)
    private byte[] ticket;

    public void setVersion(byte version) {
    }
}
