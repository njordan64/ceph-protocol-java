package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;

public class AckFrame extends ControlFrame {
    private Int64 messageSequence;

    public Int64 getMessageSequence() {
        return messageSequence;
    }

    public void setMessageSequence(Int64 messageSequence) {
        this.messageSequence = messageSequence;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        messageSequence.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        messageSequence = new Int64();
        messageSequence.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.ACK;
    }
}
