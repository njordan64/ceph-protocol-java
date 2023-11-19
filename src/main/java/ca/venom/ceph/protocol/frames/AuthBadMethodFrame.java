package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class AuthBadMethodFrame extends AuthFrameBase {
    private Int32 method;
    private Int32 result;
    private CephList<Int32> allowedMethods;
    private CephList<Int32> allowedModes;

    public Int32 getMethod() {
        return method;
    }

    public void setMethod(Int32 method) {
        this.method = method;
    }

    public int getResult() {
        return result.getValue();
    }

    public void setResult(int result) {
        this.result = new Int32(result);
    }

    public List<Int32> getAllowedMethods() {
        return allowedMethods.getValues();
    }

    public void setAllowedMethods(List<Int32> allowedMethods) {
        this.allowedMethods = new CephList<>(allowedMethods, Int32.class);
    }

    public List<Int32> getAllowedModes() {
        return allowedModes.getValues();
    }

    public void setAllowedModes(List<Int32> allowedModes) {
        this.allowedModes = new CephList<>(allowedModes, Int32.class);
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        method.encode(byteBuf, le);
        result.encode(byteBuf, le);
        allowedMethods.encode(byteBuf, le);
        allowedModes.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        method = new Int32();
        method.decode(byteBuf, le);

        result = new Int32();
        result.decode(byteBuf, le);

        allowedMethods = new CephList<>(Int32.class);
        allowedMethods.decode(byteBuf, le);

        allowedModes = new CephList<>(Int32.class);
        allowedModes.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.AUTH_BAD_METHOD;
    }
}
