package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawByte;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephXAuthenticate implements CephDataType {
    private CephRawByte version = new CephRawByte((byte) 3);
    private UInt64 clientChallenge;
    private UInt64 key;
    private CephXTicketBlob oldTicket;
    private UInt32 otherKeys;

    public CephXAuthenticate(UInt64 clientChallenge, UInt64 key, CephXTicketBlob oldTicket, UInt32 otherKeys) {
        this.clientChallenge = clientChallenge;
        this.key = key;
        this.oldTicket = oldTicket;
        this.otherKeys = otherKeys;
    }

    public static CephXAuthenticate read(ByteBuffer byteBuffer) {
        CephRawByte version = CephRawByte.read(byteBuffer);
        UInt64 clientChallenge = UInt64.read(byteBuffer);
        UInt64 key = UInt64.read(byteBuffer);
        CephXTicketBlob oldTicket = CephXTicketBlob.read(byteBuffer);
        UInt32 otherKeys = UInt32.read(byteBuffer);

        return new CephXAuthenticate(clientChallenge, key, oldTicket, otherKeys);
    }

    public UInt64 getClientChallenge() {
        return clientChallenge;
    }

    public void setClientChallenge(UInt64 clientChallenge) {
        this.clientChallenge = clientChallenge;
    }

    public UInt64 getKey() {
        return key;
    }

    public void setKey(UInt64 key) {
        this.key = key;
    }

    public CephXTicketBlob getOldTicket() {
        return oldTicket;
    }

    public void setOldTicket(CephXTicketBlob oldTicket) {
        this.oldTicket = oldTicket;
    }

    public UInt32 getOtherKeys() {
        return otherKeys;
    }

    public void setOtherKeys(UInt32 otherKeys) {
        this.otherKeys = otherKeys;
    }

    @Override
    public int getSize() {
        return 21 + oldTicket.getSize();
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        version.encode(outputStream);
        clientChallenge.encode(outputStream);
        key.encode(outputStream);
        oldTicket.encode(outputStream);
        otherKeys.encode(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        version.encode(byteBuffer);
        clientChallenge.encode(byteBuffer);
        key.encode(byteBuffer);
        oldTicket.encode(byteBuffer);
        otherKeys.encode(byteBuffer);
    }
}
