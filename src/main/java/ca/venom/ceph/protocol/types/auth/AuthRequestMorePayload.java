package ca.venom.ceph.protocol.types.auth;

import io.netty.buffer.ByteBuf;

public class AuthRequestMorePayload extends CephDataContainer {
    private CephXRequestHeader requestHeader;
    private CephXAuthenticate authenticate;

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
    protected void encodePayload(ByteBuf byteBuf, boolean le) {
        requestHeader.encode(byteBuf, le);
        authenticate.encode(byteBuf, le);
    }

    @Override
    protected void decodePayload(ByteBuf byteBuf, boolean le) {
        requestHeader = new CephXRequestHeader();
        requestHeader.decode(byteBuf, le);

        authenticate = new CephXAuthenticate();
        authenticate.decode(byteBuf, le);
    }
}
