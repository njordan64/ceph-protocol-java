package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.CephRawByte;
import io.netty.buffer.ByteBuf;

public class AuthDonePayload extends CephDataContainer {
    private CephXResponseHeader responseHeader;
    private CephRawByte version = new CephRawByte((byte) 1);
    private CephList<CephXTicketInfo> ticketInfos;
    private CephBytes encryptedSecret;
    private CephBytes extra;

    public CephXResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(CephXResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public CephList<CephXTicketInfo> getTicketInfos() {
        return ticketInfos;
    }

    public void setTicketInfos(CephList<CephXTicketInfo> ticketInfos) {
        this.ticketInfos = ticketInfos;
    }

    public CephBytes getEncryptedSecret() {
        return encryptedSecret;
    }

    public void setEncryptedSecret(CephBytes encryptedSecret) {
        this.encryptedSecret = encryptedSecret;
    }

    public CephBytes getExtra() {
        return extra;
    }

    public void setExtra(CephBytes extra) {
        this.extra = extra;
    }

    @Override
    protected int getPayloadSize() {
        return 1 + responseHeader.getSize() + ticketInfos.getSize() + encryptedSecret.getSize() + extra.getSize();
    }

    @Override
    public void encodePayload(ByteBuf byteBuf, boolean le) {
        responseHeader.encode(byteBuf, le);
        version.encode(byteBuf, le);
        ticketInfos.encode(byteBuf, le);
        encryptedSecret.encode(byteBuf, le);
        extra.encode(byteBuf, le);
    }

    @Override
    public void decodePayload(ByteBuf byteBuf, boolean le) {
        responseHeader = new CephXResponseHeader();
        responseHeader.decode(byteBuf, le);

        byte versionValue = byteBuf.readByte();
        if (versionValue != version.getValue()) {
            throw new IllegalArgumentException("Unsupported version (" + versionValue + ") only 1 is supported");
        }

        ticketInfos = new CephList<>(CephXTicketInfo.class);
        ticketInfos.decode(byteBuf, le);

        encryptedSecret = new CephBytes();
        encryptedSecret.decode(byteBuf, le);
        extra = new CephBytes();
        extra.decode(byteBuf, le);
    }
}
