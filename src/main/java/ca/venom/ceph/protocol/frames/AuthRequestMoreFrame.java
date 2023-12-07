package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.types.EncodingException;
import ca.venom.ceph.protocol.types.auth.AuthRequestMorePayload;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class AuthRequestMoreFrame extends AuthFrameBase {
    @Getter
    @Setter
    private AuthRequestMorePayload payload;

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        CephEncoder.encode(payload, byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        payload = CephDecoder.decode(byteBuf, le, AuthRequestMorePayload.class);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_REQUEST_MORE;
    }
}
