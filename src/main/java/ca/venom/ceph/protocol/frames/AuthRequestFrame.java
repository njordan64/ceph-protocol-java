package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.auth.AuthRequestPayload;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class AuthRequestFrame extends AuthFrameBase {
    private Int32 authMethod;
    private CephList<Int32> preferredModes = new CephList<>(new ArrayList<>(), Int32.class);
    private AuthRequestPayload payload;

    public Int32 getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(Int32 authMethod) {
        this.authMethod = authMethod;
    }

    public List<Int32> getPreferredModes() {
        return preferredModes.getValues();
    }

    public void setPreferredModes(List<Int32> preferredModes) {
        this.preferredModes = new CephList<>(preferredModes, Int32.class);
    }

    public AuthRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(AuthRequestPayload payload) {
        this.payload = payload;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        authMethod.encode(byteBuf, le);
        preferredModes.encode(byteBuf, le);
        payload.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        authMethod = new Int32();
        authMethod.decode(byteBuf, le);

        preferredModes = new CephList<>(Int32.class);
        preferredModes.decode(byteBuf, le);

        payload = new AuthRequestPayload();
        payload.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_REQUEST;
    }
}
