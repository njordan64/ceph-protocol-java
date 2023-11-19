package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.auth.AuthReplyMorePayload;
import io.netty.buffer.ByteBuf;

public class AuthReplyMoreFrame extends AuthFrameBase {
    private AuthReplyMorePayload payload;

    public AuthReplyMorePayload getPayload() {
        return payload;
    }

    public void setPayload(AuthReplyMorePayload payload) {
        this.payload = payload;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        payload.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        payload = new AuthReplyMorePayload();
        payload.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_REPLY_MORE;
    }
}
