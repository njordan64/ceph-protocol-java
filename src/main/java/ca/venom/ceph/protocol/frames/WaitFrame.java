package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import io.netty.buffer.ByteBuf;

public class WaitFrame extends ControlFrame {
    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
    }

    @Override
    public MessageType getTag() {
        return MessageType.WAIT;
    }
}
