package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import io.netty.buffer.ByteBuf;

public class WaitFrame extends ControlFrame {
    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.WAIT;
    }
}
