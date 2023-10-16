package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.UInt8;
import ca.venom.ceph.protocol.types.UTime;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephXServiceTicket implements CephDataType {
    private UInt8 version = new UInt8(1);
    private CryptoKey sessionKey;
    private UTime validity;

    public CephXServiceTicket(CryptoKey sessionKey, UTime validity) {
        this.sessionKey = sessionKey;
        this.validity = validity;
    }

    public static CephXServiceTicket read(ByteBuffer byteBuffer) {
        UInt8 version = UInt8.read(byteBuffer);
        CryptoKey sessionKey = CryptoKey.read(byteBuffer);
        UTime validity = UTime.read(byteBuffer);

        return new CephXServiceTicket(sessionKey, validity);
    }

    public CryptoKey getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(CryptoKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    public UTime getValidity() {
        return validity;
    }

    public void setValidity(UTime validity) {
        this.validity = validity;
    }

    @Override
    public int getSize() {
        return 1 + sessionKey.getSize() + validity.getSize();
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        version.encode(outputStream);
        sessionKey.encode(outputStream);
        validity.encode(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        version.encode(byteBuffer);
        sessionKey.encode(byteBuffer);
        validity.encode(byteBuffer);
    }
}
