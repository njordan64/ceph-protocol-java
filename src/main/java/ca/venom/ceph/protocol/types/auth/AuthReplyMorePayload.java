package ca.venom.ceph.protocol.types.auth;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthReplyMorePayload extends CephDataContainer {
    private CephXServerChallenge serverChallenge;

    public AuthReplyMorePayload() {
        super();
    }

    public AuthReplyMorePayload(CephXServerChallenge serverChallenge) {
        super();
        this.serverChallenge = serverChallenge;
    }

    public AuthReplyMorePayload(ByteBuffer byteBuffer) {
        super(byteBuffer);
        serverChallenge = CephXServerChallenge.read(byteBuffer);
    }

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
    protected void encodePayload(ByteArrayOutputStream stream) {
        serverChallenge.encode(stream);
    }

    @Override
    protected void encodePayload(ByteBuffer byteBuffer) {
        serverChallenge.encode(byteBuffer);
    }
}
