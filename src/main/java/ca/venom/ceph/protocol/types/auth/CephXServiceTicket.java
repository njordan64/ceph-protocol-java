package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.Int8;
import ca.venom.ceph.protocol.types.UTime;
import io.netty.buffer.ByteBuf;

public class CephXServiceTicket implements CephDataType {
    private Int8 version = new Int8((byte) 1);
    private CryptoKey sessionKey;
    private UTime validity;

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
    public void encode(ByteBuf byteBuf, boolean le) {
        version.encode(byteBuf, le);
        sessionKey.encode(byteBuf, le);
        validity.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte versionValue = byteBuf.readByte();
        if (versionValue != version.getValue()) {
            throw new IllegalArgumentException("Unsupported version (" + versionValue + ") only 1 is supported");
        }

        sessionKey = new CryptoKey();
        sessionKey.decode(byteBuf, le);

        validity = new UTime();
        validity.decode(byteBuf, le);
    }
}
