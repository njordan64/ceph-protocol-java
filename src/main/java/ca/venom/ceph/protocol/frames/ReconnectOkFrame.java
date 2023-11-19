package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;

public class ReconnectOkFrame extends ControlFrame {
    private Int64 messageSeq;

    public Int64 getMessageSeq() {
        return messageSeq;
    }

    public void setMessageSeq(Int64 messageSeq) {
        this.messageSeq = messageSeq;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        messageSeq.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        messageSeq = new Int64();
        messageSeq.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.SESSION_RECONNECT_OK;
    }
}
