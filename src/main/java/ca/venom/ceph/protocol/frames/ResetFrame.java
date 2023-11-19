package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.CephBoolean;
import io.netty.buffer.ByteBuf;

public class ResetFrame extends ControlFrame {
    private CephBoolean fullReset;

    public boolean isFullReset() {
        return fullReset.getValue();
    }

    public void setFullReset(boolean fullReset) {
        this.fullReset = new CephBoolean(fullReset);
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        fullReset.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        fullReset = new CephBoolean();
        fullReset.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.SESSION_RESET;
    }
}
