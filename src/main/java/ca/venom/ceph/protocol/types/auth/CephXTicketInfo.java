package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawByte;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephXTicketInfo implements CephDataType {
    private UInt32 serviceId;
    private CephRawByte version = new CephRawByte((byte) 1);
    private CephBytes serviceTicket;
    private CephBoolean encrypted;
    private CephBytes ticket;

    public CephXTicketInfo(UInt32 serviceId, CephBytes serviceTicket, CephBoolean encrypted, CephBytes ticket) {
        this.serviceId = serviceId;
        this.serviceTicket = serviceTicket;
        this.encrypted = encrypted;
        this.ticket = ticket;
    }

    public static CephXTicketInfo read(ByteBuffer byteBuffer) {
        UInt32 serviceId = UInt32.read(byteBuffer);
        CephRawByte version = CephRawByte.read(byteBuffer);
        CephBytes serviceTicket = CephBytes.read(byteBuffer);
        CephBoolean encrypted = CephBoolean.read(byteBuffer);
        CephBytes ticket = CephBytes.read(byteBuffer);

        return new CephXTicketInfo(serviceId, serviceTicket, encrypted, ticket);
    }

    public UInt32 getServiceId() {
        return serviceId;
    }

    public void setServiceId(UInt32 serviceId) {
        this.serviceId = serviceId;
    }

    public CephBytes getServiceTicket() {
        return serviceTicket;
    }

    public void setServiceTicket(CephBytes serviceTicket) {
        this.serviceTicket = serviceTicket;
    }

    public CephBoolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(CephBoolean encrypted) {
        this.encrypted = encrypted;
    }

    public CephBytes getTicket() {
        return ticket;
    }

    public void setTicket(CephBytes ticket) {
        this.ticket = ticket;
    }

    @Override
    public int getSize() {
        return 6 + serviceTicket.getSize() + ticket.getSize();
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        serviceId.encode(outputStream);
        version.encode(outputStream);
        serviceTicket.encode(outputStream);
        encrypted.encode(outputStream);
        ticket.encode(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        serviceId.encode(byteBuffer);
        version.encode(byteBuffer);
        serviceTicket.encode(byteBuffer);
        encrypted.encode(byteBuffer);
        ticket.encode(byteBuffer);
    }
}
