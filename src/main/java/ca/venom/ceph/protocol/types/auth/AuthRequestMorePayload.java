package ca.venom.ceph.protocol.types.auth;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthRequestMorePayload extends CephDataContainer {
    private CephXRequestHeader requestHeader;
    private CephXAuthenticate authenticate;

    public AuthRequestMorePayload() {
        super();
    }

    public AuthRequestMorePayload(CephXRequestHeader requestHeader, CephXAuthenticate authenticate) {
        super();
        this.requestHeader = requestHeader;
        this.authenticate = authenticate;
    }

    public AuthRequestMorePayload(ByteBuffer byteBuffer) {
        super(byteBuffer);
        this.requestHeader = CephXRequestHeader.read(byteBuffer);
        this.authenticate = CephXAuthenticate.read(byteBuffer);
    }

    public CephXRequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(CephXRequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    public CephXAuthenticate getAuthenticate() {
        return authenticate;
    }

    public void setAuthenticate(CephXAuthenticate authenticate) {
        this.authenticate = authenticate;
    }

    @Override
    protected int getPayloadSize() {
        return requestHeader.getSize() + authenticate.getSize();
    }

    @Override
    protected void encodePayload(ByteArrayOutputStream stream) {
        requestHeader.encode(stream);
        authenticate.encode(stream);
    }

    @Override
    protected void encodePayload(ByteBuffer byteBuffer) {
        requestHeader.encode(byteBuffer);
        authenticate.encode(byteBuffer);
    }
}
