package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawByte;
import ca.venom.ceph.protocol.types.CephRawBytes;
import io.netty.buffer.ByteBuf;

public class CephXServerChallenge implements CephDataType {
    private CephRawByte version = new CephRawByte((byte) 1);
    private CephRawBytes serverChallenge;

    public CephRawBytes getServerChallenge() {
        return serverChallenge;
    }

    public void setServerChallenge(CephRawBytes serverChallenge) {
        this.serverChallenge = serverChallenge;
    }

    @Override
    public int getSize() {
        return 9;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        version.encode(byteBuf, le);
        serverChallenge.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte versionValue = byteBuf.readByte();
        if (versionValue != version.getValue()) {
            throw new IllegalArgumentException("Unsupported version (" + versionValue + ") only 1 is supported");
        }

        serverChallenge = new CephRawBytes(8);
        serverChallenge.decode(byteBuf, le);
    }
}
