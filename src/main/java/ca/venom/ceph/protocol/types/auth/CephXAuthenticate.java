package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawByte;
import ca.venom.ceph.protocol.types.CephRawBytes;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;

public class CephXAuthenticate implements CephDataType {
    private CephRawByte version = new CephRawByte((byte) 3);
    private CephRawBytes clientChallenge;
    private CephRawBytes key;
    private CephXTicketBlob oldTicket;
    private Int32 otherKeys;

    public CephRawBytes getClientChallenge() {
        return clientChallenge;
    }

    public void setClientChallenge(CephRawBytes clientChallenge) {
        this.clientChallenge = clientChallenge;
    }

    public CephRawBytes getKey() {
        return key;
    }

    public void setKey(CephRawBytes key) {
        this.key = key;
    }

    public CephXTicketBlob getOldTicket() {
        return oldTicket;
    }

    public void setOldTicket(CephXTicketBlob oldTicket) {
        this.oldTicket = oldTicket;
    }

    public Int32 getOtherKeys() {
        return otherKeys;
    }

    public void setOtherKeys(Int32 otherKeys) {
        this.otherKeys = otherKeys;
    }

    @Override
    public int getSize() {
        return 21 + oldTicket.getSize();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        version.encode(byteBuf, le);
        clientChallenge.encode(byteBuf, le);
        key.encode(byteBuf, le);
        oldTicket.encode(byteBuf, le);
        otherKeys.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte versionValue = byteBuf.readByte();
        if (versionValue != version.getValue()) {
            throw new IllegalArgumentException("Unsupported version (" + versionValue + ") only 3 is supported");
        }

        clientChallenge = new CephRawBytes(8);
        clientChallenge.decode(byteBuf, le);

        key = new CephRawBytes(8);
        key.decode(byteBuf, le);

        oldTicket = new CephXTicketBlob();
        oldTicket.decode(byteBuf, le);

        otherKeys = new Int32();
        otherKeys.decode(byteBuf, le);
    }
}
