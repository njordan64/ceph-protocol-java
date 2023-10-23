package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawByte;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;

public class CephXTicketInfo implements CephDataType {
    private Int32 serviceId;
    private CephRawByte version = new CephRawByte((byte) 1);
    private CephBytes serviceTicket;
    private CephBoolean encrypted;
    private CephBytes ticket;

    public Int32 getServiceId() {
        return serviceId;
    }

    public void setServiceId(Int32 serviceId) {
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
    public void encode(ByteBuf byteBuf, boolean le) {
        serviceId.encode(byteBuf, le);
        version.encode(byteBuf, le);
        serviceTicket.encode(byteBuf, le);
        encrypted.encode(byteBuf, le);
        ticket.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        serviceId = new Int32();
        serviceId.decode(byteBuf, le);

        byte versionValue = byteBuf.readByte();
        if (versionValue != version.getValue()) {
            throw new IllegalArgumentException("Unsupported version (" + versionValue + ") only 1 is supported");
        }

        serviceTicket = new CephBytes();
        serviceTicket.decode(byteBuf, le);

        encrypted = new CephBoolean();
        encrypted.decode(byteBuf, le);

        ticket = new CephBytes();
        ticket.decode(byteBuf, le);
    }
}
