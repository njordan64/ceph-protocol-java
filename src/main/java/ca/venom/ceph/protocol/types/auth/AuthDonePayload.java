package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.CephRawByte;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthDonePayload extends CephDataContainer {
    private CephXResponseHeader responseHeader;
    private CephRawByte version = new CephRawByte((byte) 1);
    private CephList<CephXTicketInfo> ticketInfos;
    private CephBytes encryptedSecret;
    private CephBytes extra;

    public AuthDonePayload() {
        super();
    }

    public AuthDonePayload(CephXResponseHeader responseHeader,
                           CephList<CephXTicketInfo> ticketInfos,
                           CephBytes encryptedSecret,
                           CephBytes extra) {
        super();
        this.responseHeader = responseHeader;
        this.ticketInfos = ticketInfos;
        this.encryptedSecret = encryptedSecret;
        this.extra = extra;
    }

    public AuthDonePayload(ByteBuffer byteBuffer) {
        super(byteBuffer);
        responseHeader = CephXResponseHeader.read(byteBuffer);
        version = CephRawByte.read(byteBuffer);
        ticketInfos = CephList.read(byteBuffer, CephXTicketInfo.class);
        encryptedSecret = CephBytes.read(byteBuffer);
        extra = CephBytes.read(byteBuffer);
    }

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
    protected void encodePayload(ByteArrayOutputStream stream) {
        responseHeader.encode(stream);
        version.encode(stream);
        ticketInfos.encode(stream);
        encryptedSecret.encode(stream);
        extra.encode(stream);
    }

    @Override
    protected void encodePayload(ByteBuffer byteBuffer) {
        responseHeader.encode(byteBuffer);
        version.encode(byteBuffer);
        ticketInfos.encode(byteBuffer);
        encryptedSecret.encode(byteBuffer);
        extra.encode(byteBuffer);
    }
}
