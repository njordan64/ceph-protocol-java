package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.Int64;
import ca.venom.ceph.protocol.types.auth.AuthDonePayload;
import io.netty.buffer.ByteBuf;

public class AuthDoneFrame extends AuthFrameBase {
    private Int64 globalId;
    private Int32 connectionMode;
    private AuthDonePayload payload;

    public Int64 getGlobalId() {
        return globalId;
    }

    public void setGlobalId(Int64 globalId) {
        this.globalId = globalId;
    }

    public Int32 getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(Int32 connectionMode) {
        this.connectionMode = connectionMode;
    }

    public AuthDonePayload getPayload() {
        return payload;
    }

    public void setPayload(AuthDonePayload payload) {
        this.payload = payload;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        globalId.encode(byteBuf, le);
        connectionMode.encode(byteBuf, le);
        payload.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        globalId = new Int64();
        globalId.decode(byteBuf, le);

        connectionMode = new Int32();
        connectionMode.decode(byteBuf, le);

        payload = new AuthDonePayload();
        payload.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_DONE;
    }
}
