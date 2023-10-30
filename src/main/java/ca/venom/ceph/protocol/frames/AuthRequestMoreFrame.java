package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.auth.AuthRequestMorePayload;
import io.netty.buffer.ByteBuf;

public class AuthRequestMoreFrame extends AuthFrameBase {
    private AuthRequestMorePayload payload;

    public AuthRequestMorePayload getPayload() {
        return payload;
    }

    public void setPayload(AuthRequestMorePayload payload) {
        this.payload = payload;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        payload.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        payload = new AuthRequestMorePayload();
        payload.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST_MORE;
    }
}
