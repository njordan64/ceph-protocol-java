package ca.venom.ceph.protocol.types.auth;

import io.netty.buffer.ByteBuf;

public class AuthReplyMorePayload extends CephDataContainer {
    private CephXServerChallenge serverChallenge;

    public CephXServerChallenge getServerChallenge() {
        return serverChallenge;
    }

    public void setServerChallenge(CephXServerChallenge serverChallenge) {
        this.serverChallenge = serverChallenge;
    }

    @Override
    protected int getPayloadSize() {
        return serverChallenge.getSize();
    }

    @Override
    protected void encodePayload(ByteBuf byteBuf, boolean le) {
        serverChallenge.encode(byteBuf, le);
    }

    @Override
    protected void decodePayload(ByteBuf byteBuf, boolean le) {
        serverChallenge = new CephXServerChallenge();
        serverChallenge.decode(byteBuf, le);
    }
}
