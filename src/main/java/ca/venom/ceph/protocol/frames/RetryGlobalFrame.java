package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;

public class RetryGlobalFrame extends ControlFrame {
    private Int64 globalSeq;

    public Int64 getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(Int64 globalSeq) {
        this.globalSeq = globalSeq;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        globalSeq.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        globalSeq = new Int64();
        globalSeq.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RETRY_GLOBAL;
    }
}
